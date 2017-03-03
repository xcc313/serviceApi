package com.lzj.service;

import com.lzj.dao.Dao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/15.
 */
@Service
public class UserService {
    @Resource
    private Dao dao;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);


    //根据openId查询user
    public Map<String,Object> selectUserByOpenId(String openId){
        String sql = "select * from user where openid=?";
        return dao.findFirst(sql, openId);
    }
    //根据userNo查询user
    public Map<String,Object> selectUserByUserNo(String userNo){
        String sql = "select * from user where user_no=?";
        return dao.findFirst(sql, userNo);
    }

    /**
     * 根据操作类型更改用户是否关注状态
     * @param type unsubscribe取消关注    subscribe关注
     * @param openid
     * @throws SQLException
     */
    public void updateUnsubscribe(String type,String openid) throws SQLException {
        if("unsubscribe".equals(type)){
            String sql = "update user set is_unsubscribe=1,unsubscribe_time=? where openid=?";
            dao.update(sql,new Object[]{new Date(),openid});
        }else if("subscribe".equals(type)){
            String sql = "update user set is_unsubscribe=0  where openid=?";
            dao.update(sql,openid);
        }
    }

    /**
     * 根据openid更新用户信息
     * @param openid
     * @param nickname
     * @param sex
     * @param province
     * @param city
     * @param country
     * @param headimgurl
     * @throws SQLException
     */
    public void refreshUserInfo(String openid,String nickname,String sex,String province,String city,String country,String headimgurl) throws SQLException {
        String sql = "update user set nickname=?,sex=?,province=?,city=?,country=?,headimgurl=? where openid=?";
        dao.update(sql,new Object[]{nickname,sex,province,city,country,headimgurl,openid});
    }

    /**
     * 更新用户微币
     * @param userNo 用户编号
     * @param type 更新类型 less减少  more增加
     * @param operWeiCoin 更新微币数量
     * @return 受影响的行数
     * @throws SQLException
     */
    public int updateUserCoin(String userNo,String type,int operWeiCoin) throws SQLException {
        if("less".equals(type)){
            String sql = "update user set wei_coin=wei_coin-? where user_no=? and wei_coin>=?";
            return dao.update(sql,new Object[]{operWeiCoin,userNo,operWeiCoin});
        }else if("more".equals(type)){
            String sql = "update user set wei_coin=wei_coin+? where user_no=?";
            return dao.update(sql,new Object[]{operWeiCoin,userNo});
        }else{
            return 0;
        }
    }

    /**
     * 签到-更改用户的微币
     * @throws SQLException
     *
     */
    public void updateUserCoinBySignIn(String openId,int weiCoin) throws SQLException{
        String sql = "update user set wei_coin=wei_coin+?,day_have_sign_in=1 where openid=? and day_have_sign_in=0";
        dao.update(sql, new Object[]{weiCoin,openId});
    }

    /**
     * 充值-更改用户的微币
     * @throws SQLException
     *
     */
    public void updateUserCoinByRecharge(String userNo,int weiCoin) throws SQLException{
        String sql = "update user set wei_coin=wei_coin+?,is_first_recharge=1 where user_no=?";
        dao.update(sql, new Object[]{weiCoin,userNo});
    }

    /**
     * 每天凌晨刷新用户今日是否签到的定时任务
     * @throws SQLException
     */
    public void refreshSign() throws SQLException{
        String sql = "update user set day_have_sign_in=0 where day_have_sign_in=1";
        dao.update(sql);
    }

    public void checkSubArrive() throws SQLException {
        String updateUserSql = "update user set sub_joke = 0 where user_no=?";
        String updateSubListSql = "update sub_list set status=1 where user_no=?";
        String findSubListSql = "select * from sub_list where end_time<now() GROUP BY user_no";
        List<Map<String,Object>> subArriveList = dao.find(findSubListSql);
        for(Map<String,Object> subArriveMap:subArriveList){
            String userNo = String.valueOf(subArriveMap.get("user_no"));
            dao.update(updateUserSql,userNo);
            dao.update(updateSubListSql,userNo);
        }
    }

    public void insertOperationLog(String operator,String operatot_type,String operator_table,String operator_detail) throws SQLException {
        String sql = "insert into operator_log(operator,operatot_type,operator_table,operator_detail,operator_time) values(?,?,?,?,?)";
        dao.insertReturnId(sql, new Object[]{operator, operatot_type, operator_table, operator_detail, new Date()});
    }
}
