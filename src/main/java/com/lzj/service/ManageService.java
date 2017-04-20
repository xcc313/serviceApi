package com.lzj.service;

import com.lzj.dao.Dao;
import com.lzj.op.Manager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lzj on 2017/4/13.
 */
@Service
public class ManageService {
    private static final Logger log = LoggerFactory.getLogger(ManageService.class);
    @Resource
    private Dao dao;

    public Manager getByUserName(String userName) {
        String sql = "select * from manager where user_name=?";
        return dao.findFirst(Manager.class, sql, userName);
    }

    public Manager selectManagerToLogin(String userName,String pwd){
        String sql = "select * from manager where user_name=? and pwd=?";
        return dao.findFirst(Manager.class,sql, new Object[]{userName,pwd});
    }

    public void updateManagerLastLogin(int id) throws SQLException {
        String sql = "update manager set last_login_time=now() where id=?";
        dao.update(sql,id);
    }

    public Long selectAllUserNum(){
        return (Long)dao.findBy("select count(1) allUserNum from user","allUserNum");
    }
    public Long selectTodayAddUserNum(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        String sql = "select count(1) addUserNum from user where is_unsubscribe='0' and create_time>?";
        return (Long)dao.findBy(sql,"addUserNum",today);
    }

    public List<Map<String,Object>> selectReturnExtraction(){
        //String sql = "select * from purse_cash where cash_status='2' and is_back='1' and back_status is null";
        String sql = "select re.* from return_extraction re,purse_cash pc where re.cash_id=pc.id and pc.is_back='1' and (pc.cash_status is null or pc.cash_status<>'1')";
        return dao.find(sql);
    }
    public Map<String,Object> selectReturnExtraction(String id){
        String sql = "select * from return_extraction where id=?";
        return dao.findFirst(sql, id);
    }
    public Map<String,Object> selectReturnExtractionByCashId(String cashId){
        String sql = "select * from return_extraction where cash_id=?";
        return dao.findFirst(sql, cashId);
    }
    public Map<String,Object> selectPurseCash(String id){
        String sql = "select * from purse_cash where id=?";
        return dao.findFirst(sql,id);
    }


    /**
     * 冲正审核不通过
     * @param cashId
     * @param operator
     * @param checkMsg
     * @return
     * @throws SQLException
     */
    public int returnExtractionCheckFail(String cashId,String operator,String checkMsg) throws SQLException {
        String sql = "update return_extraction set check_person=?,check_status='2',check_msg=?,check_time=? where cash_id=?";
        return dao.update(sql, new Object[]{operator, checkMsg, new Date(), cashId});
    }

    /**
     * 冲正审核通过
     * @param cashId
     * @param operator
     * @return
     */
    public Map<String,Object> returnExtractionCheckPass(String cashId,String operator){
        Map<String,Object> extractionResultMap = new HashMap<>();
        Connection conn = dao.getConnection();
        try {
            conn.setAutoCommit(false);
            String searchOrderSql = "select * from purse_cash where id=?";
            Map<String,Object> purseOrderMap = dao.findFirst(searchOrderSql,cashId);
            if(purseOrderMap==null || purseOrderMap.isEmpty()){
                log.info("无此订单,cashId="+cashId);
                extractionResultMap.put("success",false);
                extractionResultMap.put("msg", "无此订单");
                return extractionResultMap;
            }
            if ("3".equals(String.valueOf(purseOrderMap.get("order_status"))) && "1".equals(String.valueOf(purseOrderMap.get("cash_status")))) {
                log.info("该订单上游已回调且已提现成功,cashId=" + cashId);
                extractionResultMap.put("success",true);
                extractionResultMap.put("msg", "无需冲正或已冲正");
                return extractionResultMap;
            }
            BigDecimal purseCashAmount = new BigDecimal(String.valueOf(purseOrderMap.get("amount")));
            String searchReturnEctractionSql = "select * from return_extraction where cash_id=? for update";
            Map<String,Object> returnExtractionMap = dao.findFirst(searchReturnEctractionSql, cashId);
            if(returnExtractionMap==null || returnExtractionMap.isEmpty()){
                log.info("无此冲正记录,cashId="+cashId);
                extractionResultMap.put("success",false);
                extractionResultMap.put("msg", "无此冲正记录");
                return extractionResultMap;
            }
            String checkStatus = String.valueOf(returnExtractionMap.get("check_status"));
            if(!"0".equals(checkStatus)){
                log.info("此冲正信息已审核,cashId="+cashId);
                extractionResultMap.put("success",false);
                extractionResultMap.put("msg", "此冲正信息已审核");
                return extractionResultMap;
            }
            BigDecimal returnExtractionAmount = new BigDecimal(String.valueOf(returnExtractionMap.get("amount")));
            if(purseCashAmount.compareTo(returnExtractionAmount)!=0){
                log.info("冲正金额与提现金额不一致,cashId="+cashId);
                extractionResultMap.put("success",false);
                extractionResultMap.put("msg", "冲正信息异常");
                return extractionResultMap;
            }
            String userNo = String.valueOf(returnExtractionMap.get("user_no"));
            String updateBalanceSql = "update user set balance=balance+? where user_no=?";
            int updateBalanceRow = dao.updateByTranscation(updateBalanceSql, new Object[]{returnExtractionAmount, userNo}, conn);
            String updateReturnExtractionSql = "update return_extraction set check_person=?,check_status='1',check_msg='审核通过',check_time=? where cash_id=? and check_status='0'";
            int updateReturnExtractionRow = dao.updateByTranscation(updateReturnExtractionSql, new Object[]{operator, new Date(), cashId}, conn);
            String insertBalanceHistorySql = "insert into balance_history(user_no,method,amount,create_time,channel,channel_id) values(?,?,?,?,?,?)";
            int insertHistoryRow = dao.updateByTranscation(insertBalanceHistorySql, new Object[]{userNo, "IN", returnExtractionAmount, new Date(), 7, returnExtractionMap.get("id")}, conn);
            log.info("cashId:{},returnExtraction,sql结果，updateBalanceRow:{},updateReturnExtractionRow:{},insertHistoryRow:{}",new Object[]{cashId,updateBalanceRow,updateReturnExtractionRow,insertHistoryRow});
            if(updateBalanceRow==1 && updateReturnExtractionRow==1 && insertHistoryRow==1){
                log.info("冲正审核sql提交,cashId="+cashId);
                conn.commit();
                extractionResultMap.put("success", true);
                extractionResultMap.put("msg", "冲正成功");
                return extractionResultMap;
            }else{
                log.info("冲正审核sql回滚,cashId="+cashId);
                conn.rollback();
                extractionResultMap.put("success", false);
                extractionResultMap.put("msg", "冲正失败");
                return extractionResultMap;
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                log.info("冲正审核sql回滚,cashId="+cashId);
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

}
