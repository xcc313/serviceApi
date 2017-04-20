package com.lzj.service;

import com.lzj.dao.Dao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
        return dao.findFirst(sql, orderNo);
    }

    public String getParamValue(String PARAM_KEY){
        String sql = "select PARAM_VALUE from sys_config where PARAM_KEY=?";
        return String.valueOf(dao.findBy(sql, "PARAM_VALUE", PARAM_KEY));
    }

    public void updatePayOrder(String orderStatus,String orderMsg,String transStatus,String orderNo) throws SQLException {
        String sql = "update pay_order set order_status=?,order_msg=?,trans_status=? where order_no=?";
        dao.update(sql, new Object[]{orderStatus, orderMsg, transStatus, orderNo});
    }

    public void updatePurseOrder(String orderStatus,String orderMsg,String cashStatus,String orderNo) throws SQLException {
        String sql = "update purse_cash set order_status=?,order_msg=?,cash_status=? where order_no=?";
        dao.update(sql,new Object[]{orderStatus,orderMsg,cashStatus,orderNo});
    }
    public void updatePurseOrder(String orderStatus,String orderMsg,String cashStatus,Date cashTime,String orderNo) throws SQLException {
        String sql = "update purse_cash set order_status=?,order_msg=?,cash_status=?,cash_time=? where order_no=?";
        dao.update(sql,new Object[]{orderStatus,orderMsg,cashStatus,cashTime,orderNo});
    }

    public List<Map<String,Object>> selectFastpayCardByUserNo(String userNo){
        String sql = "select * from fastpay_card where user_no=?";
        return dao.find(sql, userNo);
    }

    public Map<String,Object> selectFastpayCardById(String id){
        String sql = "select * from fastpay_card where id=?";
        return dao.findFirst(sql, id);
    }

    public List<Map<String,Object>> selectUnipayCard(String userNo){
        String sql = "select * from unipay_card where user_no=? and status='0'";
        return dao.find(sql, userNo);
    }

    public Map<String,Object> selectUnipayCard(String accountNo,String userNo){
        String sql = "select * from unipay_card where account_no=? and user_no=? and status='0'";
        return dao.findFirst(sql, new Object[]{accountNo, userNo});
    }

    public void insertUnipayCard(String user_no,String account_no) throws SQLException {
        String sql = "insert into unipay_card(user_no,account_no,status,create_time) values(?,?,?,?)";
        dao.insertReturnId(sql,new Object[]{user_no,account_no,"0",new Date()});
    }

    public void insertFastpayCard(String user_no,String account_name,String account_no,String id_card_no,String mobile_no,String bank_name) throws SQLException {
        String sql = "insert into fastpay_card(user_no,account_name,account_no,id_card_no,mobile_no,bank_name,create_time) values(?,?,?,?,?,?,?)";
        dao.insertReturnId(sql,new Object[]{user_no,account_name,account_no,id_card_no,mobile_no,bank_name,new Date()});
    }

    //根据手机号得到最新短信发送记录
    public Map<String,Object> getSmsByMobile(String mobileNo){
        String sql = "select * from sms_log where mobile_no=? order by id desc";
        return dao.findFirst(sql, mobileNo);
    }

    public void updateSmsCodeStatus(String mobileNo,String code) throws SQLException {
        String sql = "update sms_log set code_status='1' where mobile_no=? and sms_code=?";
        dao.update(sql,new Object[]{mobileNo,code});
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
                    String updateOrderSql = "update pay_order set order_status=?,order_msg=?,trans_status=?,trans_time=? where order_no=? and order_status='1' and trans_status='0'";
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
                log.info("入账sql回滚,orderNo=" + orderNo);
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
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
        }finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return extractionResultMap;
    }

    //分润  channel：5为一级分润  6为二级分润
    public Map<String,Object> parentProfit(String parentNo,String orderNo,BigDecimal parentProfitAmount,int channel){
        Map<String,Object> profitResultMap = new HashMap<>();
        Connection conn = dao.getConnection();
        try {
            conn.setAutoCommit(false);
            String searchOrderSql = "select * from pay_order where order_no=? for update";
            Map<String,Object> payOrderMap = dao.findFirst(searchOrderSql, orderNo);
            if(payOrderMap==null || payOrderMap.isEmpty()){
                log.info("无此订单,orderNo="+orderNo);
                profitResultMap.put("success",false);
                profitResultMap.put("msg", "无此订单");
                return profitResultMap;
            }
            if(!"1".equals(String.valueOf(payOrderMap.get("trans_status")))){
                log.info("订单非成功状态,orderNo="+orderNo);
                profitResultMap.put("success", false);
                profitResultMap.put("msg", "订单非成功状态");
                return profitResultMap;
            }
            String insertBalanceHistorySql = "insert into balance_history(user_no,method,amount,create_time,channel,channel_id) values(?,?,?,?,?,?)";
            int insertResultRow = dao.updateByTranscation(insertBalanceHistorySql, new Object[]{parentNo, "IN", parentProfitAmount, new Date(), channel, payOrderMap.get("id")}, conn);
            String addBalanceSql = "update user set balance=balance+? where user_no=?";
            int updateResultRow = dao.updateByTranscation(addBalanceSql, new Object[]{parentProfitAmount, parentNo}, conn);
            log.info("orderNo:{},parentProfit,sql结果，insertResultRow:{},updateResultRow:{}",new Object[]{orderNo,insertResultRow,updateResultRow});
            if(insertResultRow==1 && updateResultRow==1){
                log.info("分润sql提交,orderNo="+orderNo);
                conn.commit();
                profitResultMap.put("success", true);
                profitResultMap.put("msg", "分润成功");
                return profitResultMap;
            }else{
                log.info("分润sql回滚,orderNo="+orderNo);
                conn.rollback();
                profitResultMap.put("success", false);
                profitResultMap.put("msg", "分润失败");
                return profitResultMap;
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                log.info("提现sql回滚,orderNo="+orderNo);
                conn.rollback();
                profitResultMap.put("success", false);
                profitResultMap.put("msg", "分润异常");
                return profitResultMap;
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return profitResultMap;
    }

    /**
     * 写入待审核冲正信息
     * @param orderNo
     * @return
     */
    public Map<String,Object> insertRetuanExtraction(String orderNo){
        Map<String,Object> insertReturnExtractionResultMap = new HashMap<>();
        Connection conn = dao.getConnection();
        try {
            conn.setAutoCommit(false);
            String updatePurseSql = "update purse_cash set is_back='1' where order_no=?";
            int updatePurseRow = dao.updateByTranscation(updatePurseSql, new Object[]{orderNo}, conn);
            String searchOrderSql = "select * from purse_cash where order_no=?";
            Map<String,Object> purseOrderMap = dao.findFirst(searchOrderSql, orderNo);
            String cashId = String.valueOf(purseOrderMap.get("id"));
            String userNo = String.valueOf(purseOrderMap.get("user_no"));
            String amount = String.valueOf(purseOrderMap.get("amount"));
            String insertReturnExtractionSql = "insert into return_extraction(user_no,cash_order_no,cash_id,amount,create_time) values(?,?,?,?,?)";
            int insertReturnExtractionRow = dao.updateByTranscation(insertReturnExtractionSql, new Object[]{userNo, orderNo, cashId,amount, new Date()}, conn);
            log.info("orderNo:{},insertRetuanExtraction,sql结果，updatePurseRow:{},insertReturnExtractionRow:{}",new Object[]{orderNo,updatePurseRow,insertReturnExtractionRow});
            if(updatePurseRow==1 && insertReturnExtractionRow==1){
                log.info("写入冲正信息sql提交,orderNo="+orderNo);
                conn.commit();
                insertReturnExtractionResultMap.put("success", true);
                insertReturnExtractionResultMap.put("msg", "写入冲正成功");
                return insertReturnExtractionResultMap;
            }else{
                log.info("写入冲正信息sql回滚,orderNo="+orderNo);
                conn.rollback();
                insertReturnExtractionResultMap.put("success", false);
                insertReturnExtractionResultMap.put("msg", "写入冲正失败");
                return insertReturnExtractionResultMap;
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                log.info("写入冲正sql回滚,orderNo="+orderNo);
                conn.rollback();
                insertReturnExtractionResultMap.put("success", false);
                insertReturnExtractionResultMap.put("msg", "写入冲正异常");
                return insertReturnExtractionResultMap;
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return insertReturnExtractionResultMap;
    }


    /**
     * 不审核直接冲正
     * @param orderNo
     * @param backRemark
     * @return
     */
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
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
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
            String sql = "select order_no,case when cash_status is null then '未知' when cash_status='0' then '提现中' when cash_status='1' then '提现成功' when cash_status='2' then '提现失败' when cash_status='3' then '提现未知' end trans_status,order_status,order_msg from purse_cash where id=?";
            Map<String,Object> map = dao.findFirst(sql,channelId);
            String trans_status = String.valueOf(map.get("trans_status"));
            String order_status = String.valueOf(map.get("order_status"));
            if("未知".equals(trans_status) && "2".equals(order_status)){
                map.put("trans_status",map.get("order_msg"));
            }
            return map;
        }else if("3".equals(channel)){
            String sql = "select order_no,case when back_status='0' then '未冲正' when back_status='1' then '冲正成功' when back_status='2' then '冲正失败' when back_status='3' then '冲正未知' end trans_status from purse_cash where id=?";
            return dao.findFirst(sql,channelId);
        }else if("4".equals(channel)){
            Map<String,Object> map = new HashMap<>();
            map.put("order_no","调账");
            map.put("trans_status","调账成功");
            return map;
        }else if("5".equals(channel)){
            String sql = "select order_no,'分润成功' as trans_status from pay_order where id=?";
            return dao.findFirst(sql,channelId);
        }else if("6".equals(channel)){
            String sql = "select order_no,'二级分润成功' as trans_status from pay_order where id=?";
            return dao.findFirst(sql,channelId);
        }else if("7".equals(channel)){
            String sql = "select cash_order_no as order_no,case when check_status='0' then '冲正未审核' when check_status='1' then '冲正成功' when check_status='2' then '冲正审核不通过' end trans_status from return_extraction where id=?";
            return dao.findFirst(sql,channelId);
        }else{
            return null;
        }
    }

    //商户编号、日期区间查询交易订单总金额
    public Object findOrderByUserNo(String userNo,String startDate,String endDate) {
        String sql = "select sum(o.trans_amount) totalAmount from pay_order o where o.user_no =? and o.trans_status = '1' and create_time between ? and ?";
        return dao.findBy(sql,"totalAmount",new Object[]{userNo,startDate,endDate});
    }

    public Map<String,Object> cardBin(String accountNo) throws SQLException {
        String sql = "select * from pos_card_bin c  where  c.card_length = length(?) AND c.verify_code = left(?,  c.verify_length)";
        return dao.findFirst(sql, new Object[]{accountNo, accountNo});
    }

    public List<Map<String,Object>> selectSendMerchantName(){
        String sql = "select * from send_merchant_name";
        return dao.find(sql);
    }

    public Map<String,Object> findBlackByTypeAndValue(Integer black_type, String black_value){
        String sql = " select id, black_type, black_value from risk_black_user where black_type = ? and black_value = ? and black_status = 1 ";
        return dao.findFirst(sql, new Object[]{black_type, black_value});
    }

    public List<Map<String,Object>> selectRiskPayOrder(String userNo,BigDecimal amount){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String todayStr = sdf.format(new Date());
        String sql = "select * from pay_order where user_no=? and create_time>? and order_status='3' and trans_amount=? order by id desc";
        return dao.find(sql, new Object[]{userNo, todayStr, String.valueOf(amount)});
    }

    public void lockUser(String userNo) throws SQLException {
        String sql = "update user set status='LOCK' where user_no=?";
        dao.update(sql,userNo);
    }
}
