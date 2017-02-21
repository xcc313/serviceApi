package com.lzj.service;

import com.lzj.dao.Dao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Administrator on 2016/10/15.
 */
@Service
public class PayService {
    @Resource
    private Dao dao;

    private static final Logger log = LoggerFactory.getLogger(PayService.class);


    //根据userNo查询user
    public Map<String,Object> selectUserByUserNo(String userNo){
        String sql = "select * from user where user_no=?";
        return dao.findFirst(sql, userNo);
    }

    /**
     * 统一插入数据方法
     * @param tableName  插入的表名
     * @param paramsMap  插入参数
     * @return 所插入的id
     * @throws SQLException
     * @author lzj
     */
    public int insertMethod(String tableName,Map<String,Object> paramsMap) throws SQLException {
        StringBuffer sbSql = new StringBuffer("insert into ");
        StringBuffer valueSql = new StringBuffer("");
        sbSql.append(tableName).append("(");
        List<Object> list = new ArrayList<Object>();
        Set<Map.Entry<String, Object>> entries = paramsMap.entrySet();
        for (Map.Entry<String, Object> entry:entries) {
            sbSql.append(entry.getKey()).append(",");
            valueSql.append("?,");
            list.add(entry.getValue());
        }
        sbSql = sbSql.deleteCharAt(sbSql.length()-1);
        sbSql.append(") values(");
        valueSql = valueSql.deleteCharAt(valueSql.length()-1);
        sbSql.append(valueSql).append(")");
        return (int)dao.insertReturnId(sbSql.toString(),list.toArray());
    }

    public void insertOperationLog(String operator,String operatot_type,String operator_table,String operator_detail) throws SQLException {
        String sql = "insert into operator_log(operator,operatot_type,operator_table,operator_detail,operator_time) values(?,?,?,?,?)";
        dao.insertReturnId(sql, new Object[]{operator, operatot_type, operator_table, operator_detail, new Date()});
    }

    public Map<String,Object> selectPayOrder(String orderNo){
        String sql = "select * from pay_order where order_no=?";
        return dao.findFirst(sql,orderNo);
    }

    public String getParamValue(String PARAM_KEY){
        String sql = "select PARAM_VALUE from sys_config where PARAM_KEY=?";
        return String.valueOf(dao.findBy(sql, "PARAM_VALUE", PARAM_KEY));
    }

    public void updatePayOrder(String orderStatus,String orderMsg,String transStatus,String orderNo) throws SQLException {
        String sql = "update pay_order set order_status=?,order_msg=?,trans_status=? where order_no=?";
        dao.update(sql,new Object[]{orderStatus,orderMsg,transStatus,orderNo});
    }

    public void recharge(String orderNo){
        Connection conn = dao.getConnection();
        try {
            conn.setAutoCommit(false);
            String searchOrderSql = "select * from pay_order where order_no=? for update";
            Map<String,Object> payOrderMap = dao.findFirst(searchOrderSql,orderNo);
            if(payOrderMap==null || payOrderMap.isEmpty()){
               log.info("无此订单,orderNo="+orderNo);
            }else{
                if("3".equals(String.valueOf(payOrderMap.get("order_status"))) || !"0".equals(String.valueOf(payOrderMap.get("trans_status")))){
                    log.info("该订单上游已回调，或不是交易中状态,orderNo="+orderNo);
                }else{
                    BigDecimal transAmount = new BigDecimal(String.valueOf(payOrderMap.get("trans_amount")));
                    BigDecimal transFee = new BigDecimal(String.valueOf(payOrderMap.get("trans_fee")));
                    BigDecimal rechargeAmount = transAmount.subtract(transFee);
                    String updateOrderSql = "update pay_order set order_status=?,order_msg=?,trans_status=?,trans_time=? where order_no=?";
                    int updateOrderResultRow = dao.updateByTranscation(updateOrderSql,new Object[]{"3","订单支付成功","1",new Date(),orderNo},conn);
                    String insertBalanceHistorySql = "insert into balance_history(user_no,method,amount,create_time,channel,channel_id) values(?,?,?,?,?,?)";
                    int insertResultRow = dao.updateByTranscation(insertBalanceHistorySql,new Object[]{payOrderMap.get("user_no"),"IN",rechargeAmount,new Date(),1,payOrderMap.get("id")},conn);
                    String addBalanceSql = "update user set balance=balance+? where user_no=?";
                    int updateResultRow = dao.updateByTranscation(addBalanceSql,new Object[]{rechargeAmount,payOrderMap.get("user_no")},conn);
                    log.info("orderNo:{},sql结果，updateOrderResultRow:{},insertResultRow:{},updateResultRow:{}",new Object[]{orderNo,updateOrderResultRow,insertResultRow,updateResultRow});
                    if(updateOrderResultRow==1 && insertResultRow==1 && updateResultRow==1){
                        log.info("sql提交,orderNo="+orderNo);
                        conn.commit();
                    }else{
                        log.info("sql回滚,orderNo="+orderNo);
                        conn.rollback();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                log.info("sql回滚,orderNo="+orderNo);
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }
}
