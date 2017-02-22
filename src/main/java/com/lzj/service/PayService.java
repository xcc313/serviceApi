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

    public Map<String,Object> selectPurseOrder(String orderNo){
        String sql = "select * from purse_cash where order_no=?";
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

    public void updatePurseOrder(String orderStatus,String orderMsg,String cashStatus,String orderNo) throws SQLException {
        String sql = "update purse_cash set order_status=?,order_msg=?,cash_status=? where order_no=?";
        dao.update(sql,new Object[]{orderStatus,orderMsg,cashStatus,orderNo});
    }
    public void updatePurseOrder(String orderStatus,String orderMsg,String cashStatus,Date cashTime,String orderNo) throws SQLException {
        String sql = "update purse_cash set order_status=?,order_msg=?,cash_status=?,cash_time=? where order_no=?";
        dao.update(sql,new Object[]{orderStatus,orderMsg,cashStatus,cashTime,orderNo});
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
                    log.info("orderNo:{},recharge,sql结果，updateOrderResultRow:{},insertResultRow:{},updateResultRow:{}",new Object[]{orderNo,updateOrderResultRow,insertResultRow,updateResultRow});
                    if(updateOrderResultRow==1 && insertResultRow==1 && updateResultRow==1){
                        log.info("入账sql提交,orderNo="+orderNo);
                        conn.commit();
                    }else{
                        log.info("入账sql回滚,orderNo="+orderNo);
                        conn.rollback();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                log.info("入账sql回滚,orderNo="+orderNo);
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    public Map<String,Object> extraction(String userNo,String orderNo){
        Map<String,Object> extractionResultMap = new HashMap<>();
        Connection conn = dao.getConnection();
        try {
            conn.setAutoCommit(false);
            String selectUserSql = "select * from user where user_no=? for update";
            Map<String,Object> userMap = dao.findFirst(selectUserSql,userNo);
            BigDecimal balance = new BigDecimal(String.valueOf(userMap.get("balance")));
            if(balance.compareTo(new BigDecimal("0"))!=1){
                extractionResultMap.put("success",false);
                extractionResultMap.put("msg", "无需提现");
                return extractionResultMap;
            }
            String searchOrderSql = "select * from purse_cash where order_no=? for update";
            Map<String,Object> purseOrderMap = dao.findFirst(searchOrderSql,orderNo);
            if(purseOrderMap!=null && !purseOrderMap.isEmpty()){
                log.info("已有此订单,orderNo=" + orderNo);
                extractionResultMap.put("success",false);
                extractionResultMap.put("msg", "已有此订单");
                return extractionResultMap;
            }
            BigDecimal extractionFee = new BigDecimal(String.valueOf(userMap.get("extraction_fee")));
            String accountName = String.valueOf(userMap.get("real_name"));
            String accountNo = String.valueOf(userMap.get("bank_no"));
            String remark = "速查服务";
            String updateBalanceSql = "update user set balance=balance-? where user_no=? and balance>=?";
            int updateBalanceRow = dao.updateByTranscation(updateBalanceSql,new Object[]{balance,userNo,balance},conn);
            String insertPurseCashSql = "insert into purse_cash(order_no,user_no,amount,fee,create_time,settle_account_no,settle_account_name,remark,order_status,order_msg) values(?,?,?,?,?,?,?,?,?,?)";
            long insertPurseId = dao.insertReturnIdByTranscation(conn, insertPurseCashSql, new Object[]{orderNo, userNo, balance, extractionFee, new Date(), accountNo, accountName, remark, "0", "初始化"});
            String insertBalanceHistorySql = "insert into balance_history(user_no,method,amount,create_time,channel,channel_id) values(?,?,?,?,?,?)";
            int insertHistoryRow = dao.updateByTranscation(insertBalanceHistorySql, new Object[]{userNo, "OUT", balance, new Date(), 2, insertPurseId}, conn);
            log.info("orderNo:{},extraction,sql结果，updateBalanceRow:{},insertPurseId:{},insertHistoryRow:{}",new Object[]{orderNo,updateBalanceRow,insertPurseId,insertHistoryRow});
            if(updateBalanceRow==1 && insertPurseId>0 && insertHistoryRow==1){
                log.info("提现sql提交,orderNo="+orderNo);
                conn.commit();
                extractionResultMap.put("success", true);
                extractionResultMap.put("msg", "提现成功");
                return extractionResultMap;
            }else{
                log.info("提现sql回滚,orderNo="+orderNo);
                conn.rollback();
                extractionResultMap.put("success", false);
                extractionResultMap.put("msg", "提现失败");
                return extractionResultMap;
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                log.info("提现sql回滚,orderNo="+orderNo);
                conn.rollback();
                extractionResultMap.put("success", false);
                extractionResultMap.put("msg", "提现异常");
                return extractionResultMap;
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
        return extractionResultMap;
    }


    public Map<String,Object> returnExtraction(String orderNo,String backRemark){
        Map<String,Object> extractionResultMap = new HashMap<>();
        Connection conn = dao.getConnection();
        try {
            conn.setAutoCommit(false);
            String searchOrderSql = "select * from purse_cash where order_no=? for update";
            Map<String,Object> purseOrderMap = dao.findFirst(searchOrderSql,orderNo);
            if(purseOrderMap==null || purseOrderMap.isEmpty()){
                log.info("无此订单,orderNo="+orderNo);
                extractionResultMap.put("success",false);
                extractionResultMap.put("msg", "无此订单");
                return extractionResultMap;
            }
            if ("3".equals(String.valueOf(purseOrderMap.get("order_status"))) && "1".equals(String.valueOf(purseOrderMap.get("cash_status")))) {
                log.info("该订单上游已回调且已提现成功,orderNo=" + orderNo);
                extractionResultMap.put("success",true);
                extractionResultMap.put("msg", "无需冲正或已冲正");
                return extractionResultMap;
            }
            if ("1".equals(String.valueOf(purseOrderMap.get("back_status")))) {
                log.info("该订单已冲正成功,orderNo=" + orderNo);
                extractionResultMap.put("success",true);
                extractionResultMap.put("msg", "无需冲正或已冲正");
                return extractionResultMap;
            }
            String userNo = String.valueOf(purseOrderMap.get("user_no"));
            String amount = String.valueOf(purseOrderMap.get("amount"));
            String updateBalanceSql = "update user set balance=balance+? where user_no=?";
            int updateBalanceRow = dao.updateByTranscation(updateBalanceSql, new Object[]{amount, userNo}, conn);
            String updatePurseSql = "update purse_cash set back_status='1',back_remark=?,back_time=? where order_no=? and (cash_status<>'1' or cash_status is null) and (back_status<>'1' or back_status is null)";
            int updatePurseRow = dao.updateByTranscation(updatePurseSql, new Object[]{backRemark, new Date(), orderNo}, conn);
            String insertBalanceHistorySql = "insert into balance_history(user_no,method,amount,create_time,channel,channel_id) values(?,?,?,?,?,?)";
            int insertHistoryRow = dao.updateByTranscation(insertBalanceHistorySql, new Object[]{userNo, "IN", amount, new Date(), 3, purseOrderMap.get("id")}, conn);
            log.info("orderNo:{},returnExtraction,sql结果，updateBalanceRow:{},updatePurseRow:{},insertHistoryRow:{}",new Object[]{orderNo,updateBalanceRow,updatePurseRow,insertHistoryRow});
            if(updateBalanceRow==1 && updatePurseRow==1 && insertHistoryRow==1){
                log.info("冲正sql提交,orderNo="+orderNo);
                conn.commit();
                extractionResultMap.put("success", true);
                extractionResultMap.put("msg", "冲正成功");
                return extractionResultMap;
            }else{
                log.info("冲正sql回滚,orderNo="+orderNo);
                conn.rollback();
                extractionResultMap.put("success", false);
                extractionResultMap.put("msg", "冲正失败");
                return extractionResultMap;
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                log.info("冲正sql回滚,orderNo="+orderNo);
                conn.rollback();
                extractionResultMap.put("success", false);
                extractionResultMap.put("msg", "冲正异常");
                return extractionResultMap;
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
        return extractionResultMap;
    }

    //根据channel,channelId查询相关
    public Map<String,Object> selectOrderByHistory(String channel,String channelId){
        if("1".equals(channel)){
            String sql = "select order_no,case when trans_status='0' then '交易中' when trans_status='1' then '交易成功' when trans_status='2' then '交易失败' when trans_status='3' then '交易未知' end trans_status from pay_order where id=?";
            return dao.findFirst(sql,channelId);
        }else if("2".equals(channel)){
            String sql = "select order_no,case when cash_status='0' then '提现中' when cash_status='1' then '提现成功' when cash_status='2' then '提现失败' when cash_status='3' then '提现未知' end trans_status from purse_cash where id=?";
            return dao.findFirst(sql,channelId);
        }else if("3".equals(channel)){
            String sql = "select order_no,case when back_status='0' then '未冲正' when back_status='1' then '冲正成功' when back_status='2' then '冲正失败' when back_status='3' then '冲正未知' end trans_status from purse_cash where id=?";
            return dao.findFirst(sql,channelId);
        }else if("4".equals(channel)){
            return null;
        }else{
            return null;
        }
    }
}
