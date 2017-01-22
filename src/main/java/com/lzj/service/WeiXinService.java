package com.lzj.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import com.lzj.op.WeiXinPayOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lzj.dao.Dao;
import com.lzj.op.AccessToken;

@Service
public class WeiXinService {
	
	@Resource
	private Dao dao;
	
	private static final Logger log = LoggerFactory
			.getLogger(WeiXinService.class);
	
	//查询accessToken
	public AccessToken getAccessToken(){
		String sql = "select * from access_token order by id desc";
		return dao.findFirst(AccessToken.class, sql);
	}
	
	//插入accessToken
	public void insertAccessToken(String accessToken) throws SQLException{
		String sql = "insert into access_token(access_token,last_time) values(?,?)";
		dao.update(sql, new Object[]{accessToken,new Date()});
	}

	//根据openId查询user
	public Map<String,Object> selectUserByOpenId(String openId){
		String sql = "select * from user where openid=?";
		return dao.findFirst( sql, openId);
	}

	public String getParamValue(String PARAM_KEY){
		String sql = "select PARAM_VALUE from sys_config where PARAM_KEY=?";
		return String.valueOf(dao.findBy(sql, "PARAM_VALUE", PARAM_KEY));
	}

	/**
	 * 插入微信支付统一下单信息
	 * @param weixinPayOrder
	 * @return
	 */
	public int insertWeiXinPerOrder(WeiXinPayOrder weixinPayOrder,String userNo,int channel){
		String sql = "insert into weixin_pay_order(user_no,open_id,channel,out_trade_no,body,total_fee,trade_type,spbill_create_ip,create_time) values(?,?,?,?,?,?,?,?,?)";
		try {
			return (int)dao.insertReturnId(sql, new Object[]{userNo,weixinPayOrder.getOpenid(),channel,weixinPayOrder.getOut_trade_no(),weixinPayOrder.getBody(),weixinPayOrder.getTotal_fee(),weixinPayOrder.getTrade_type(),weixinPayOrder.getSpbill_create_ip(),new Date()});
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 根据系统内部订单号查询微信支付订单信息
	 * @param out_trade_no
	 * @return
	 */
	public Map<String,Object> selectWeiXinOrder(String out_trade_no){
		String sql = "select * from weixin_pay_order where out_trade_no=?";
		return dao.findFirst(sql, out_trade_no);
	}


}
