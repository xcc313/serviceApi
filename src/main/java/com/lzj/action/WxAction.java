package com.lzj.action;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.*;
import java.text.NumberFormat;
import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lzj.op.ResultMsg;
import com.lzj.op.WeiXinPayOrder;
import com.lzj.service.UserService;
import com.lzj.utils.*;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.lzj.op.AccessToken;
import com.lzj.op.TextMessage;
import com.lzj.service.ApiService;
import com.lzj.service.WeiXinService;

@Controller
@RequestMapping(value = "/wx")
public class WxAction extends BaseController{
	private static final Logger log = LoggerFactory
			.getLogger(WxAction.class);
	
	@Resource
	private WeiXinService weixinService;
	@Resource
	private UserService userService;
	@Resource
	private ApiService apiService;

	public WxAction(){}
	public WxAction(WeiXinService weixinService){ this.weixinService = weixinService;}
	public WxAction(ApiService apiService,WeiXinService weixinService){ this.apiService = apiService;this.weixinService = weixinService;}

	@RequestMapping(value="/wxReceive",method = RequestMethod.GET)
	public void wxReceiveGet(HttpServletRequest request ,HttpServletResponse response) {
		log.info("进入wxReceive Get方法");
		// 微信加密签名
		String signature = request.getParameter("signature");
		// 时间戳
		String timestamp = request.getParameter("timestamp");
		// 随机数
		String nonce = request.getParameter("nonce");
		// 随机字符串
		String echostr = request.getParameter("echostr");
		PrintWriter out =  null;
		try {
			out = response.getWriter();
			if(StringUtils.isEmpty(signature) || StringUtils.isEmpty(timestamp) || StringUtils.isEmpty(nonce)){
				log.info("-----配置失败，相关信息为空------");
				out.print("请检查信息");
			}
			// 通过检验signature对请求进行校验,若校验成功则原样返回echostr,表示接入成功,否则接入失败
			if (SignUtil.checkSignature(signature, timestamp, nonce)) {
				log.info("-----配置成功------");
				out.print(echostr);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			out.close();
		}
	}

	@RequestMapping(value="/wxReceive",method = RequestMethod.POST)
	public void wxReceivePost(HttpServletRequest request ,HttpServletResponse response) {
		log.info("进入wxReceive Post方法");
		response.setCharacterEncoding("UTF-8");
		// xml请求解析  调用消息工具类MessageUtil解析微信发来的xml格式的消息,解析的结果放在HashMap里;
		MessageUtil messageUtil = new MessageUtil();
		Map<String, String> requestMap = null;
		try {
			requestMap = messageUtil.parseXml(request);
			log.info("----------requestMap---------"+requestMap);
			String userNameOpenId = requestMap.get("FromUserName");
			// 公众帐号
			String publicAccount = requestMap.get("ToUserName");
			// 消息类型
			String msgType = requestMap.get("MsgType");
			String respContent = null;
			//点击事件--事件推送
			if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)) {
				// 事件类型
				String eventType = requestMap.get("Event");
				if (eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {
					log.info("订阅");
					// 订阅
					respContent = "速查服务，您的便利生活专家";
					TextMessage textMessage = new TextMessage();
					textMessage.setToUserName(userNameOpenId);
					textMessage.setFromUserName(publicAccount);
					textMessage.setCreateTime(new Date().getTime());
					textMessage.setContent(respContent);
					textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
					//将Object转换成XML格式
					String respContentXml = MessageUtil.textMessageToXml(textMessage);
					outXml(respContentXml, response);
					Map<String,Object> userMap = userService.selectUserByOpenId(userNameOpenId);
					if(userMap==null || userMap.isEmpty()){
						userMap = new UserAction(userService,apiService).insertUser(getUserInfoByOpenid(userNameOpenId));
						new ApiAction(apiService).freeSubJoke("3",String.valueOf(userMap.get("user_no")));
						Map<String,Object> kfParamsMap = new HashMap<String, Object>();
						kfParamsMap.put("accessToken",getAccessToken());
						kfParamsMap.put("toUserOpenId",userNameOpenId);
						kfParamsMap.put("content","首次关注赠送您5微币，感谢您的使用 | 为了更便利的生活而努力");
						Thread thread = new Thread(new KFNotifyNotice("kefuMessage_text",kfParamsMap));
						thread.start();
						//发送赠送笑话的模板消息
						String appId = "wx4ee57072d531b1a9";
						String redirect_uri = java.net.URLEncoder.encode("http://www.qrcodevip.com/wx/auth.do");
						String jokeUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+appId+"&redirect_uri="+redirect_uri+"&response_type=code&scope=snsapi_base&state=joke&connect_redirect=1#wechat_redirect";
						Map<String,String> modelMap = new HashMap<String, String>();
						modelMap.put("first","每日一笑业务赠送通知");
						modelMap.put("keyword1","笑话订阅");
						modelMap.put("keyword2","已赠送");
						modelMap.put("keyword3","赠送时长一周");
						modelMap.put("remark","如需变更/取消请点击详情查看");
						modelMap.put("descUrl",jokeUrl);
						sendWXModelMsg(userNameOpenId, "JnFiGYhw6xgEa8lA-ViYoCrUb5QSwxpFXIXQIpWI7o0", modelMap);
					}else{
						userService.updateUnsubscribe("subscribe",userNameOpenId);
					}
					return;
				}else if (eventType.equals(MessageUtil.EVENT_TYPE_UNSUBSCRIBE)) {
					// 取消订阅
					outText("success", response);
					userService.updateUnsubscribe("unsubscribe", userNameOpenId);
					return;
				}else if(eventType.equals("TEMPLATESENDJOBFINISH")){
					//模板消息是否成功送达通知
					String msgID = requestMap.get("MsgID");
					String status = requestMap.get("Status");
					Map<String,Object> updateMap = new HashMap<String,Object>();
					updateMap.put("status",status);
					Map<String,Object> updateWhereMap = new HashMap<String,Object>();
					updateWhereMap.put("msgid",msgID);
					updateWhereMap.put("open_id",userNameOpenId);
					apiService.updateMethod("model_msg", updateMap, updateWhereMap);
					outText("success", response);
					return;
				} else if (eventType.equals(MessageUtil.EVENT_TYPE_CLICK)) {
					//自定义菜单点击事件
					log.info("自定义菜单点击事件");
					// 事件KEY值，与创建自定义菜单时指定的KEY值对应
					String key = requestMap.get("EventKey");
					log.info("--------key----------"+key);
					if(StringUtils.isNotEmpty(key) && "signIn".equals(key)){
						log.info("--------签到----------");
						Map<String,Object> userMap = weixinService.selectUserByOpenId(userNameOpenId);
						if(userMap==null || userMap.isEmpty()) {
							new UserAction(userService,apiService).insertUser(getUserInfoByOpenid(userNameOpenId));
							userMap = weixinService.selectUserByOpenId(userNameOpenId);
						}
						if(userMap!=null && !userMap.isEmpty()){
							int dayHaveSignIn = (Integer)userMap.get("day_have_sign_in");
							if(dayHaveSignIn==1){
								respContent = "今日已签到，请明日签到。当前剩余"+userMap.get("wei_coin")+"微币";
							}else{
								userService.updateUserCoinBySignIn(userNameOpenId, 1);
								userMap = weixinService.selectUserByOpenId(userNameOpenId);
								respContent = "签到成功，微币+1，当前剩余"+userMap.get("wei_coin")+"微币";
							}
						}
						TextMessage textMessage = new TextMessage();
						textMessage.setToUserName(userNameOpenId);
						textMessage.setFromUserName(publicAccount);
						textMessage.setCreateTime(new Date().getTime());
						textMessage.setContent(respContent);
						textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
						//将Object转换成XML格式
						String respContentXml = MessageUtil.textMessageToXml(textMessage);
						System.out.println("----------respContentXml---------"+respContentXml);
						outXml(respContentXml, response);
						return;
					}
					outText("success", response);
					return;
				}
			}else if(msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)){
				String Content = requestMap.get("Content");
				log.info("------Content------"+Content);
				Boolean keyWord = false;
				if("支付宝激活码".equals(Content)){
					// 订阅
					Map<String,Object> kfParamsMap = new HashMap<String, Object>();
					kfParamsMap.put("accessToken", getAccessToken());
					kfParamsMap.put("toUserOpenId", userNameOpenId);
					kfParamsMap.put("media_id", "");
					Thread thread = new Thread(new KFNotifyNotice("kefuMessage_image",kfParamsMap));
					thread.start();
					outText("success", response);
					return;
				}else{
					log.info("------转移至客服系统----");
					TextMessage textMessage = new TextMessage();
					textMessage.setToUserName(userNameOpenId);
					textMessage.setFromUserName(publicAccount);
					textMessage.setCreateTime(new Date().getTime());
					textMessage.setMsgType(MessageUtil.EVENT_TYPE_CUSTOMER);
					//将Object转换成XML格式
					String respContentXml = MessageUtil.textMessageToXml(textMessage);
					log.info("----respContentXml----"+respContentXml);
					outXml(respContentXml, response);
					return;
				}
                /*if(keyWord){
                    TextMessage textMessage = new TextMessage();
                    textMessage.setToUserName(userNameOpenId);
                    textMessage.setFromUserName(publicAccount);
                    textMessage.setCreateTime(new Date().getTime());
                    textMessage.setContent(respContent);
                    textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
                    //将Object转换成XML格式
                    String respContentXml = MessageUtil.textMessageToXml(textMessage);
                    System.out.println("----respContentXml----"+respContentXml);
                    outXml(respContentXml, response);
                    return;
                }*/
			}else{
				outText("success", response);
				return;
			}

		} catch (Exception e) {
			e.printStackTrace();
			outText("success", response);
			return;
		}
	}

	public synchronized String getAccessToken(){
		System.out.println("进入getAccessToken()方法锁");
		AccessToken accessToken = weixinService.getAccessToken();
		if(accessToken==null||(new Date().getTime() - accessToken.getLastTime().getTime())>90*60*1000){
			System.out.println("------重新插入access_token--------");
			insertAccessToken();
			accessToken = weixinService.getAccessToken();
		}
		System.out.println("getAccessToken()方法锁里面的accessToken.getLastTime()--->" + accessToken.getLastTime() + "   accessToken.getAccessToken()-->" + accessToken.getAccessToken());
		return accessToken.getAccessToken();
	}
	
	/**
	 * 插入access_token
	 * @return
	 * @throws Exception
	 */
	public void insertAccessToken(){
		System.out.println("进入insertAccessToken()");
		InputStream is = null;
		try {
			URL url1=new URL("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+weixinService.getParamValue("AppID")+"&secret="+weixinService.getParamValue("AppSecret"));
			HttpURLConnection huc=(HttpURLConnection) url1.openConnection();
			huc.setDoOutput(true);
			huc.setDoInput(true);
			huc.setRequestMethod("GET");
			is=huc.getInputStream();
			int len=0;
			byte[] buff=new byte[1024];
			String allStr  = null;
			while((len=is.read(buff))>0){
				allStr = new String(buff,0,len);
			}
			System.out.println("----allStr------"+allStr);
			String[] accessTokenArr = allStr.split("\"");
			String accessToken = accessTokenArr[3];
			weixinService.insertAccessToken(accessToken);

		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				if(is!=null){
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}


	/**
	 * 根据openid获取用户信息
	 * @param openid
	 * @return
	 */
	public Map<String,Object> getUserInfoByOpenid(String openid){
		if(StringUtils.isEmpty(openid) || "null".equals(openid)){
			return null;
		}
		String urlStr = "https://api.weixin.qq.com/cgi-bin/user/info?access_token="+getAccessToken()+"&openid="+openid+"&lang=zh_CN";
		String httpReturnStr = LZJUtil.sendGet(urlStr, "UTF-8");
		log.info("-------httpReturnStr-----" + httpReturnStr);
		Map<String, Object> map = LZJUtil.jsonToMap(httpReturnStr);
		return map;
	}

	public String getOpenid(HttpServletRequest request,String code){
		String openid = (String)request.getSession().getAttribute("openid");
		if(StringUtils.isEmpty(openid) || "".equals(openid)){
			String appId = weixinService.getParamValue("AppID");
			String appsecret = weixinService.getParamValue("AppSecret");
			String url1Str = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+appId+"&secret="+appsecret+"&code="+code+"&grant_type=authorization_code";
			String httpReturnStr = LZJUtil.sendGet(url1Str, "UTF-8");
			log.info("-------httpReturnStr-----"+httpReturnStr);
			Map<String, Object> map = LZJUtil.jsonToMap(httpReturnStr);
			openid = (String)map.get("openid");
			if(!StringUtils.isEmpty(openid) && !"NULL".equals(openid) && !"null".equals(openid)){
				request.getSession().setAttribute("openid",openid);
			}
		}
		return openid;
	}

	/**
	 * 网页授权获取用户信息
	 */
	@RequestMapping(value = "auth.do", method = RequestMethod.GET)
	public void auth(final ModelMap model,@RequestParam Map<String, String> params,HttpServletRequest request,HttpServletResponse response){
		log.info("---------进入网页授权获取用户信息方法---------");
		response.setCharacterEncoding("UTF-8");
		String state = params.get("state");
		String code = params.get("code");
		log.info("网页授权中，code={},state={}", code, state);
		try {
			if("joke".equals(state)){
				log.info("--------笑话订阅---------");
				String openid = getOpenid(request, code);
				if(StringUtils.isEmpty(openid) || "NULL".equals(openid) || "null".equals(openid)){
					log.info("--------openid为空---------");
					response.sendRedirect("/api/errorPage?errorMsg=获取用户信息异常，请重新进入&errorCode=100000000");
				}else{
					Map<String,Object> userMap = weixinService.selectUserByOpenId(openid);
					if(userMap==null || userMap.isEmpty()) {
						new UserAction(userService,apiService).insertUser(getUserInfoByOpenid(openid));
					}
					userMap = weixinService.selectUserByOpenId(openid);
					String userNo = String.valueOf(userMap.get("user_no"));
					response.sendRedirect("/api/jokePage?userNo="+userNo);
				}
			}else if("lottery".equals(state)){
				log.info("--------彩票开奖查询---------");
				String openid = getOpenid(request, code);
				if(StringUtils.isEmpty(openid) || "NULL".equals(openid) || "null".equals(openid)){
					log.info("--------openid为空---------");
					response.sendRedirect("/api/errorPage?errorMsg=获取用户信息异常，请重新进入&errorCode=100000000");
				}else{
					Map<String,Object> userMap = weixinService.selectUserByOpenId(openid);
					if(userMap==null || userMap.isEmpty()) {
						new UserAction(userService,apiService).insertUser(getUserInfoByOpenid(openid));
					}
					userMap = weixinService.selectUserByOpenId(openid);
					String userNo = String.valueOf(userMap.get("user_no"));
					response.sendRedirect("/api/lotteryPage?userNo="+userNo);
				}
			}else if("zipcode".equals(state)){
				log.info("--------邮编查询---------");
				String openid = getOpenid(request, code);
				if(StringUtils.isEmpty(openid) || "NULL".equals(openid) || "null".equals(openid)){
					log.info("--------openid为空---------");
					response.sendRedirect("/api/errorPage?errorMsg=获取用户信息异常，请重新进入&errorCode=100000000");
				}else{
					Map<String,Object> userMap = weixinService.selectUserByOpenId(openid);
					if(userMap==null || userMap.isEmpty()) {
						new UserAction(userService,apiService).insertUser(getUserInfoByOpenid(openid));
					}
					userMap = weixinService.selectUserByOpenId(openid);
					String userNo = String.valueOf(userMap.get("user_no"));
					response.sendRedirect("/api/zipcodePage?userNo="+userNo);
				}
			}else if("verified".equals(state)){
				log.info("--------银行卡认证查询---------");
				String openid = getOpenid(request, code);
				if(StringUtils.isEmpty(openid) || "NULL".equals(openid) || "null".equals(openid)){
					log.info("--------openid为空---------");
					response.sendRedirect("/api/errorPage?errorMsg=获取用户信息异常，请重新进入&errorCode=100000000");
				}else{
					Map<String,Object> userMap = weixinService.selectUserByOpenId(openid);
					if(userMap==null || userMap.isEmpty()) {
						new UserAction(userService,apiService).insertUser(getUserInfoByOpenid(openid));
					}
					userMap = weixinService.selectUserByOpenId(openid);
					String userNo = String.valueOf(userMap.get("user_no"));
					response.sendRedirect("/api/verifiedPage?userNo="+userNo);
				}
			}else if("dishonesty".equals(state)){
				log.info("--------失信查询查询---------");
				String openid = getOpenid(request, code);
				if(StringUtils.isEmpty(openid) || "NULL".equals(openid) || "null".equals(openid)){
					log.info("--------openid为空---------");
					response.sendRedirect("/api/errorPage?errorMsg=获取用户信息异常，请重新进入&errorCode=100000000");
				}else{
					Map<String,Object> userMap = weixinService.selectUserByOpenId(openid);
					if(userMap==null || userMap.isEmpty()) {
						new UserAction(userService,apiService).insertUser(getUserInfoByOpenid(openid));
					}
					userMap = weixinService.selectUserByOpenId(openid);
					String userNo = String.valueOf(userMap.get("user_no"));
					response.sendRedirect("/api/dishonestyPage?userNo="+userNo);
				}
			}else if("userInfo".equals(state)){
				log.info("--------个人中心---------");
				String openid = getOpenid(request, code);
				if(StringUtils.isEmpty(openid) || "NULL".equals(openid) || "null".equals(openid)){
					log.info("--------openid为空---------");
					response.sendRedirect("/api/errorPage?errorMsg=获取用户信息异常，请重新进入&errorCode=100000000");
				}else {
					Map<String, Object> userMap = weixinService.selectUserByOpenId(openid);
					if (userMap == null || userMap.isEmpty()) {
						new UserAction(userService, apiService).insertUser(getUserInfoByOpenid(openid));
					}
					userMap = weixinService.selectUserByOpenId(openid);
					String userNo = String.valueOf(userMap.get("user_no"));
					response.sendRedirect("/user/userInfo?userNo=" + userNo);
				}
			}else if("weiCoinRecharge".equals(state)){
				log.info("--------微币充值---------");
				String openid = getOpenid(request, code);
				if(StringUtils.isEmpty(openid) || "NULL".equals(openid) || "null".equals(openid)){
					log.info("--------openid为空---------");
					response.sendRedirect("/api/errorPage?errorMsg=获取用户信息异常，请重新进入&errorCode=100000000");
				}else {
					Map<String, Object> userMap = weixinService.selectUserByOpenId(openid);
					if (userMap == null || userMap.isEmpty()) {
						new UserAction(userService, apiService).insertUser(getUserInfoByOpenid(openid));
					}
					userMap = weixinService.selectUserByOpenId(openid);
					String userNo = String.valueOf(userMap.get("user_no"));
					response.sendRedirect("/user/rechargeCoin?userNo=" + userNo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			try {
				response.sendRedirect("/api/errorPage?errorMsg=系统异常，请截图反馈&errorCode=100000000");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

	}

	/**
	 * 微信支付
	 * @param model
	 * @param params
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "weixinRechargePay")
	public void weixinRechargePay(final ModelMap model,@RequestParam Map<String, String> params,HttpServletRequest request,HttpServletResponse response){
		log.info("---------微信支付方法---------params:" + params);
		try {
			String channel = params.get("channel");
			String userNo = params.get("userNo");
			Map<String,Object> checkUserWhereMap = new HashMap<String, Object>();
			checkUserWhereMap.put("user_no",userNo);
			Map<String,Object> userMap = apiService.getOneMethod("user", checkUserWhereMap, "id", "desc", 0);
			if(userMap==null || userMap.isEmpty()){
				String jsonStr = buildJson("FAIL",null,"无此用户");
				outJson(jsonStr, response);
				return;
			}
			String openId = String.valueOf(userMap.get("openid"));
			String rechargeAmount = params.get("rechargeAmount");
			BigDecimal totalAmountBig = new BigDecimal(rechargeAmount).setScale(2, RoundingMode.HALF_UP);
			String appId = weixinService.getParamValue("AppID");
			String nonceStr = String.valueOf(WeiXinPay.buildRandom(10));
			String apikey = weixinService.getParamValue("apikey");
			String out_trade_no = String.valueOf(System.currentTimeMillis()) + String.valueOf(Math.round(Math.random() * 90000 + 10000));
			log.info("out_trade_no="+out_trade_no+",rechargeAmount="+rechargeAmount);
			String body = "";
			if("0".equals(channel)){
				body = "微币充值";
			}else if("1".equals(channel)){
				body = "打赏";
			}else{
				body = "深圳市路遥里科技有限公司";
			}
			WeiXinPayOrder weixinPayOrder = new WeiXinPayOrder();
			weixinPayOrder.setAppid(appId);
			weixinPayOrder.setMch_id(weixinService.getParamValue("merchantID"));
			weixinPayOrder.setOut_trade_no(out_trade_no);
			weixinPayOrder.setBody(body);
			int totalAmountInt = totalAmountBig.multiply(new BigDecimal("100")).intValue();
			weixinPayOrder.setTotal_fee(totalAmountInt);//分
			weixinPayOrder.setNotify_url(weixinService.getParamValue("weixinUrl") + "/wx/weixinPayCallback");
			weixinPayOrder.setTrade_type("JSAPI");
			weixinPayOrder.setSpbill_create_ip(request.getRemoteAddr());
			weixinPayOrder.setNonce_str(nonceStr);
			weixinPayOrder.setOpenid(openId);
			int perOrderId = weixinService.insertWeiXinPerOrder(weixinPayOrder,userNo,Integer.parseInt(channel));
			if(perOrderId==0){
				log.info("-------插入微信认证统一下单信息失败--------");
				String jsonStr = buildJson("FAIL",null,"写入订单失败");
				outJson(jsonStr, response);
				return;
			}
			Map<String, String> addOrderResultMap = WeiXinPay.addOrder(weixinPayOrder,apikey);
			if(addOrderResultMap==null){
				log.info("--------微信认证统一下单失败-----------");
				String jsonStr = buildJson("FAIL",null,"下单失败");
				outJson(jsonStr, response);
				return;
			}
			String return_code = addOrderResultMap.get("return_code");
			Map<String,Object> paramsMap = new HashMap<String, Object>();
			Map<String,Object> whereMap = new HashMap<String, Object>();
			whereMap.put("out_trade_no", out_trade_no);
			paramsMap.put("per_return_code", return_code);
			if("SUCCESS".equals(return_code)){
				String result_code = addOrderResultMap.get("result_code");
				paramsMap.put("per_result_code", result_code);
				if("SUCCESS".equals(result_code)){
					String prepay_id = addOrderResultMap.get("prepay_id");
					paramsMap.put("prepay_id", prepay_id);
					String timeStamp = String.valueOf(System.currentTimeMillis()/1000);
					String packageStr = "prepay_id="+prepay_id;
					String signType = "MD5";
					Map<String, Object> payMap = new HashMap<String, Object>();
					payMap.put("appId", appId);
					payMap.put("timeStamp", timeStamp);
					payMap.put("nonceStr", nonceStr);
					payMap.put("package", packageStr);
					payMap.put("signType", signType);
					String paramsList = WeiXinPay.paramsAdd(payMap);
					paramsList += "&key="+apikey;
					log.info("payParamsList="+paramsList);
					String paySign=MD5.MD5Str(paramsList).toUpperCase();
					payMap.put("paySign", paySign);
					/*JSONObject jsonObject = JSONObject.fromObject(payMap);
					log.info("jsonObject="+jsonObject.toString());
					outJson(jsonObject.toString(), response);*/
					String jsonStr = buildJson("SUCCESS", payMap, "");
					log.info("jsonStr===="+jsonStr);
					outJson(jsonStr, response);
				}else{
					String err_code = addOrderResultMap.get("err_code");
					String err_code_des = addOrderResultMap.get("err_code_des");
					log.info("-----业务结果失败-----err_code="+err_code+",err_code_des="+err_code_des);
					paramsMap.put("per_err_code", err_code);
					paramsMap.put("per_err_code_des", err_code_des);
					String jsonStr = buildJson("FAIL",null,"支付失败");
					outJson(jsonStr, response);
				}
			}else{
				String return_msg = addOrderResultMap.get("return_msg");
				log.info("-----通信失败----msg="+return_msg);
				paramsMap.put("per_return_msg", return_msg);
				String jsonStr = buildJson("FAIL",null,"支付通信失败");
				outJson(jsonStr, response);
			}
			apiService.updateMethod("weixin_pay_order",paramsMap,whereMap);
		} catch (Exception e) {
			e.printStackTrace();
			String jsonStr = buildJson("FAIL",null,"系统异常，请稍后再试");
			outJson(jsonStr, response);
			return;
		}
	}


	/**
	 * 微信支付回调方法
	 * @param model
	 * @param params
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "weixinPayCallback", method = RequestMethod.POST)
	public void weixinPayCallback(final ModelMap model,@RequestParam Map<String, String> params,HttpServletRequest request,HttpServletResponse response){
		log.info("---------微信支付回调方法---------");
		BufferedReader bis = null;
		ResultMsg resultMsg = new ResultMsg();
		resultMsg.setReturn_code("SUCCESS");
		resultMsg.setReturn_msg("OK");
		try {
			bis = new BufferedReader(new java.io.InputStreamReader(request.getInputStream()));
			String line = null;
			String result = "";
			while ((line = bis.readLine()) != null) {
				result += line+"\r\n";
			}
			log.info("result="+result);
			Map<String,String> resultMap = LZJUtil.parseXml(result);
			String return_code = resultMap.get("return_code");
			Map<String,Object> paramsMap = new HashMap<String, Object>();
			Map<String,Object> whereMap = new HashMap<String, Object>();
			paramsMap.put("pay_return_code", return_code);
			if("SUCCESS".equals(return_code)){
				String result_code = resultMap.get("result_code");
				String err_code = resultMap.get("err_code");
				String err_code_des = resultMap.get("err_code_des");
				String bank_type = resultMap.get("bank_type");//银行类型，采用字符串类型的银行标识
				String out_trade_no = resultMap.get("out_trade_no");//商户订单号
				String transaction_id = resultMap.get("transaction_id");//微信支付订单号
				String time_end = resultMap.get("time_end");//支付完成时间，格式为yyyyMMddHHmmss
				StringBuffer new_time_end = new StringBuffer(time_end.substring(0, 4));
				new_time_end.append("-").append(time_end.substring(4, 6)).append("-").append(time_end.substring(6, 8)).append(" ").append(time_end.substring(8, 10)).append(":").append(time_end.substring(10, 12)).append(":").append(time_end.substring(12, 14));
				Map<String, Object> orderMap = weixinService.selectWeiXinOrder(out_trade_no);
				if(orderMap==null){
					log.info("-----无此订单------");
				}else{
					String orderMap_result_code = String.valueOf(orderMap.get("result_code"));
					if("SUCCESS".equals(orderMap_result_code)){
						log.info("-----订单已支付成功，请勿重复------");
						return;
					}
					int channel = (Integer)orderMap.get("channel");
					String body = String.valueOf(orderMap.get("body"));
					paramsMap.put("pay_result_code", result_code);
					paramsMap.put("pay_err_code", err_code);
					paramsMap.put("pay_err_code_des", err_code_des);
					paramsMap.put("transaction_id", transaction_id);
					paramsMap.put("bank_type", bank_type);
					paramsMap.put("time_end", new_time_end.toString());
					whereMap.put("out_trade_no", out_trade_no);
					apiService.updateMethod("weixin_pay_order", paramsMap, whereMap);
					if("SUCCESS".equals(result_code)){
						if(channel==0){
							//微币充值
							String userNo = String.valueOf(orderMap.get("user_no"));
							Map<String,Object> checkUserWhereMap = new HashMap<String, Object>();
							checkUserWhereMap.put("user_no",userNo);
							Map<String,Object> userMap = apiService.getOneMethod("user", checkUserWhereMap, "id", "desc", 0);
							if(userMap==null || userMap.isEmpty()){
								log.info("-----无此用户------userNo="+userNo);
								return;
							}
							String total_fee = String.valueOf(orderMap.get("total_fee"));
							int isFirstRecharge = (Integer)userMap.get("is_first_recharge");
							int totalAmount = Integer.parseInt(total_fee)/100;
							int weiCoin = totalAmount*10;
							/*if(totalAmount>=100){
								weiCoin += 100;
							}else if(totalAmount>=50){
								weiCoin += 40;
							}else if(totalAmount>=30){
								weiCoin += 20;
							}else if(totalAmount>=10){
								weiCoin += 5;
							}*/
							if(isFirstRecharge==0 && weiCoin!=0){
								weiCoin += 10;
							}
							userService.updateUserCoinByRecharge(userNo,weiCoin);
						}else if(channel==1){
							//打赏
							log.info("打赏成功");
						}
					}
				}

			}else{
				String return_msg = resultMap.get("return_msg");
				log.info("-----回调方法通信失败----msg="+return_msg);
			}
			//将Object转换成XML格式
			String respContentXml = MessageUtil.objToXml(resultMsg);
			respContentXml = respContentXml.replaceAll("__", "_");
			System.out.println("respContentXml="+respContentXml);
			outXml(respContentXml, response);
		} catch (Exception e) {
			e.printStackTrace();
			//将Object转换成XML格式
			String respContentXml = MessageUtil.objToXml(resultMsg);
			respContentXml = respContentXml.replaceAll("__", "_");
			System.out.println("respContentXml=" + respContentXml);
			outXml(respContentXml, response);
		} finally{
			if(bis!=null){
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 微信发送模板消息
	 */
	public void sendWXModelMsg(String toUserOpenId,String templateId,Map<String,String> paramsMap){
		log.info("------------进入微信发送模板消息方法-------------");
		OutputStream os=null;
		InputStream is=null;
		try {
			URL url2=new URL("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token="+getAccessToken());
			HttpURLConnection huc=(HttpURLConnection) url2.openConnection();
			huc.setDoOutput(true);
			huc.setDoInput(true);
			huc.setRequestMethod("POST");
			//String template_id = "JnFiGYhw6xgEa8lA-ViYoCrUb5QSwxpFXIXQIpWI7o0";//正式模板ID
			String first = paramsMap.get("first");
			String keyword1 = paramsMap.get("keyword1");
			String keyword2 = paramsMap.get("keyword2");
			String keyword3 = paramsMap.get("keyword3");
			String remark = paramsMap.get("remark");
			String descUrl = paramsMap.get("descUrl");
			String msgString="{ \"touser\":\""+toUserOpenId+"\", \"template_id\":\""+templateId+"\",\"url\":\""+descUrl+"\",  \"data\":{ \"first\": { \"value\":\""+first+"\", \"color\":\"#173177\" }, \"keyword1\":{ \"value\":\""+keyword1+"\", \"color\":\"#173177\" }, \"keyword2\":{ \"value\":\""+keyword2+"\", \"color\":\"#173177\" }, \"keyword3\":{ \"value\":\""+keyword3+"\", \"color\":\"#173177\" }, \"remark\":{ \"value\":\""+remark+"\", \"color\":\"#173177\" } } }";
			os=huc.getOutputStream();
			os.write(msgString.getBytes("UTF-8"));
			os.flush();
			is=huc.getInputStream();
			int len=0;
			byte[] buff=new byte[1024];
			String sendModelResult = "";
			while((len=is.read(buff))>0){
				sendModelResult = new String(buff,0,len);
				log.info("sendWXModelMsg中的--->"+sendModelResult);
			}
			log.info("--------sendModelResult-------" + sendModelResult);
			Map<String,Object> sendModelResultMap = LZJUtil.jsonToMap(sendModelResult);
			int errcode = (Integer)sendModelResultMap.get("errcode");
			String errmsg = String.valueOf(sendModelResultMap.get("errmsg"));
			String msgid = "";
			if(errcode==0 && "ok".equals(errmsg)){
				log.info("--------------发送模板消息成功---------------");
				msgid = String.valueOf(sendModelResultMap.get("msgid"));
			}else{
				log.info("--------------发送模板消息失败---------------");
			}
			//weixinService.insertModelMsg(template_id, toUserOpenId, first, keyword1, keyword2, keyword3, "", remark, "", sendNum, errcode, errmsg, msgid);
			Map<String,Object> insertMap = new HashMap<String, Object>();
			insertMap.put("template_id",templateId);
			insertMap.put("open_id",toUserOpenId);
			insertMap.put("first",first);
			insertMap.put("keyword1",keyword1);
			insertMap.put("keyword2",keyword2);
			insertMap.put("keyword3",keyword3);
			insertMap.put("remark",remark);
			insertMap.put("desc_url",descUrl);
			insertMap.put("errcode",errcode);
			insertMap.put("errmsg",errmsg);
			insertMap.put("msgid",msgid);
			insertMap.put("create_time",new Date());
			apiService.insertMethod("model_msg",insertMap);
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				if(is!=null){
					is.close();
				}
				if(os!=null){
					os.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


	}

	public static String countPercent(int num1,int num2){
		// 创建一个数值格式化对象
		NumberFormat numberFormat = NumberFormat.getInstance();
		// 设置精确到小数点后2位
		numberFormat.setMaximumFractionDigits(2);
		String result = numberFormat.format((float) num1 / (float) num2 * 100);
		System.out.println("num1和num2的百分比为:" + result + "%");
		return result;
	}


	
}
