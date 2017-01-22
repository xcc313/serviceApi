package com.lzj.action;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lzj.service.ApiService;
import com.lzj.service.UserService;
import com.lzj.service.WeiXinService;
import com.lzj.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping(value = "/api")
public class ApiAction extends BaseController{
	private static final Logger log = LoggerFactory
			.getLogger(ApiAction.class);
	@Resource
	private ApiService apiService;
	@Resource
	private UserService userService;
	@Resource
	private WeiXinService weixinService;

	public ApiAction(){}
	public ApiAction(ApiService apiService){this.apiService = apiService;}

	@RequestMapping(value = "errorPage", method = RequestMethod.GET)
	public String errorPage(final ModelMap model, @RequestParam Map<String, String> params) {
		log.info("---------errorPage---------");
		String errorMsg = params.get("errorMsg");
		String errorCode = params.get("errorCode");
		model.put("errorMsg",errorMsg);
		model.put("errorCode",errorCode);
		return "errorPage";
	}

	@RequestMapping(value = "lotteryPage", method = RequestMethod.GET)
	public String lotteryPage(final ModelMap model, @RequestParam Map<String, String> params) {
		log.info("---------lotteryPage---------");
		try {
			String userNo = params.get("userNo");
			Map<String,Object> whereMap = new HashMap<String, Object>();
			whereMap.put("user_no",userNo);
			Map<String,Object> userMap = apiService.getOneMethod("user", whereMap, "id", "desc", 0);
			if(userMap==null || userMap.isEmpty()){
				model.put("errorMsg","无此用户");
				model.put("errorCode",userNo);
				return "errorPage";
			}else{
				model.put("userNo",userNo);
				apiService.addApiAccessNum("view_num","lottery");
				Map<String,Object> findWhereMap = new HashMap<String,Object>();
				findWhereMap.put("lottery_code", "ssq");
				List<Map<String,Object>> lotteryList = apiService.getListMethod("lottery_history", findWhereMap, "id", "desc",10);
				model.put("lotteryList", lotteryList);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			model.put("errorMsg","系统异常");
			model.put("errorCode", "100000000");
			return "errorPage";
		}
		return "api/lottery";
	}

	@RequestMapping(value = "loadLotteryHistory", method = RequestMethod.GET)
	public void loadLotteryHistory(HttpServletResponse response, @RequestParam Map<String, String> params) {
		log.info("---------loadLotteryHistory---------params===="+params);
		try {
			String lotteryName = params.get("lotteryName");
			Map<String,Object> whereMap = new HashMap<String,Object>();
			whereMap.put("lottery_name", lotteryName);
			List<Map<String,Object>> lotteryList = apiService.getListMethod("lottery_history", whereMap, "id", "desc", 8);
			String listJsonStr = JSON.toJSONString(lotteryList);
			log.info("listJsonStr====" + listJsonStr);
			outJson(listJsonStr, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "lotteryResult", method = RequestMethod.GET)
	public void lotteryResult(HttpServletResponse response, @RequestParam Map<String, String> params) {
		log.info("---------lotteryResult---------params====" + params);
		long diffHours = 0;
		Boolean resultBoo = true;
		String msg = "";
		try {
			String userNo = params.get("userNo");
			String lotteryName = params.get("lotteryName");
			String lotteryExpect = params.get("expect");
			//校验用户
			Map<String,Object> checkWhereMap = new HashMap<String, Object>();
			checkWhereMap.put("user_no",userNo);
			Map<String,Object> userMap = apiService.getOneMethod("user", checkWhereMap, "id", "desc", 0);
			if(userMap==null || userMap.isEmpty()){
				log.info("无此用户");
				msg = "无此用户,查询码:"+userNo;
				String jsonStr = buildJson("FAIL",null,msg);
				log.info("jsonStr===="+jsonStr);
				outJson(jsonStr, response);
				return;
			}
			int weiCoin = (Integer)userMap.get("wei_coin");
			if(weiCoin<2){
				log.info("微币不足");
				msg = "微币不足";
				String jsonStr = buildJson("FAIL",null,msg);
				log.info("jsonStr===="+jsonStr);
				outJson(jsonStr, response);
				return;
			}
			userService.updateUserCoin(userNo,"less",2);
			Map<String,Object> lotteryWhereMap = new HashMap<String, Object>();
			lotteryWhereMap.put("lottery_name",lotteryName);
			Map<String,Object> lotteryCategoryMap = apiService.getOneMethod("lottery_category",lotteryWhereMap,"id","desc",0);
			String lotteryCode = String.valueOf(lotteryCategoryMap.get("lottery_code"));
			int lotteryDiffHours = Integer.parseInt(lotteryCategoryMap.get("diff_hours").toString());
			//更新该彩票种类查询使用次数
			apiService.addLotteryUseNum(lotteryCode);
			Map<String,Object> whereMap = new HashMap<String,Object>();
			whereMap.put("lottery_code",lotteryCode);
			whereMap.put("expect", lotteryExpect);
			Map<String,Object> lotteryMap = apiService.getOneMethod("lottery_history", whereMap, "id", "desc", 0);
			if(lotteryMap!=null && !lotteryMap.isEmpty()){
				log.info("有此彩票查询历史记录");
				long nowTimeDay = Calendar.getInstance().getTimeInMillis();
				Calendar openTimeCal = Calendar.getInstance();
				openTimeCal.setTime((Date) lotteryMap.get("open_time"));
				long openTimeDay = openTimeCal.getTimeInMillis();
				log.info("nowTimeDay="+nowTimeDay+",openTimeDay="+openTimeDay);
				long subLong = nowTimeDay-openTimeDay;
				diffHours = subLong/(1000*60*60);
				log.info("diffHours=" + diffHours);
			}
			if(lotteryMap==null || lotteryMap.isEmpty() || (diffHours>=lotteryDiffHours && StringUtils.isEmpty(lotteryExpect))) {
				log.info("无此彩票查询历史记录或最新记录开奖时间距现在时间大于等于间隔时间，再次获取最新记录");
				//获取response的body
				String responseBody = SearchNewApiUtil.getNewLotteryResult(lotteryCode, lotteryExpect);
				//String responseBody = "{\"showapi_res_code\":0,\"showapi_res_error\":\"\",\"showapi_res_body\":{\"result\":{\"timestamp\":1478438440,\"expect\":\"2016130\",\"time\":\"2016-11-06 21:20:40\",\"name\":\"双色球\",\"code\":\"ssq\",\"openCode\":\"03,17,21,23,27,28+01\"},\"ret_code\":0}}";
				//{"showapi_res_code":0,"showapi_res_error":"","showapi_res_body":{"ret_code":0}}
				log.info(responseBody);
				JSONObject bodyJson = JSONObject.parseObject(responseBody);
				JSONObject resultBodyJson = (JSONObject)bodyJson.get("showapi_res_body");
				JSONObject resultJson = (JSONObject)resultBodyJson.get("result");
				if(resultJson!=null){
					Map<String,Object> paramsMap = new HashMap<String,Object>();
					String expect = String.valueOf(resultJson.get("expect"));
					String name = String.valueOf(resultJson.get("name"));
					String code = String.valueOf(resultJson.get("code"));
					String openCode = String.valueOf(resultJson.get("openCode"));
					String time = String.valueOf(resultJson.get("time"));
					whereMap.put("expect", expect);
					lotteryMap = apiService.getOneMethod("lottery_history", whereMap, "id", "desc",0);
					if(lotteryMap==null || lotteryMap.isEmpty()){
						paramsMap.put("expect",expect);
						paramsMap.put("lottery_code",code);
						paramsMap.put("lottery_name",name);
						paramsMap.put("open_code",openCode);
						paramsMap.put("open_time",time);
						paramsMap.put("create_time",new Date());
						paramsMap.put("user_no",userNo);
						apiService.insertMethod("lottery_history", paramsMap);
						lotteryMap = apiService.getOneMethod("lottery_history", whereMap,"id","desc",0);
					}
				}else{
					log.info("没有最新记录");
					resultBoo = false;
					msg = "未查到最新纪录";
				}
			}else{
				log.info("不获取最新记录");
				String hisUserNo = String.valueOf(lotteryMap.get("user_no"));
				String hisId = String.valueOf(lotteryMap.get("id"));
				Map<String,Object> updateMap = new HashMap<String,Object>();
				updateMap.put("user_no",hisUserNo+";"+userNo);
				Map<String,Object> updateWhereMap = new HashMap<String,Object>();
				updateWhereMap.put("id",hisId);
				apiService.updateMethod("lottery_history", updateMap, updateWhereMap);
			}
			log.info("lotteryMap=====" + lotteryMap);
			if(resultBoo){
				String jsonStr = buildJson("SUCCESS",lotteryMap,"");
				log.info("jsonStr===="+jsonStr);
				outJson(jsonStr, response);
			}else{
				String jsonStr = buildJson("FAIL",null,msg);
				log.info("jsonStr===="+jsonStr);
				outJson(jsonStr, response);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			String jsonStr = buildJson("FAIL",null,"系统异常");
			outJson(jsonStr, response);
		}
	}




	@RequestMapping(value = "verifiedPage", method = RequestMethod.GET)
	public String verifiedPage(final ModelMap model, @RequestParam Map<String, String> params) {
		log.info("---------verifiedPage---------");
		try {
			String userNo = params.get("userNo");
			Map<String,Object> whereMap = new HashMap<String, Object>();
			whereMap.put("user_no",userNo);
			Map<String,Object> userMap = apiService.getOneMethod("user", whereMap, "id", "desc", 0);
			if(userMap==null || userMap.isEmpty()){
				model.put("errorMsg","无此用户");
				model.put("errorCode",userNo);
				return "errorPage";
			}else{
				model.put("userNo",userNo);
				apiService.addApiAccessNum("view_num", "verified");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			model.put("errorMsg", "系统异常");
			model.put("errorCode", "100000000");
			return "errorPage";
		}
		return "api/verified";
	}


	@RequestMapping(value = "verifiedResult", method = RequestMethod.GET)
	public void verifiedResult(HttpServletResponse response, @RequestParam Map<String, String> params) {
		log.info("---------verifiedResult---------params===="+params);
		Boolean resultBoo = true;
		String msg = "";
		try {
			String bankcard = params.get("bankcard");
			String idcard = params.get("idcard");
			String realname = params.get("realname");
			String mobileNo = params.get("mobileNo");
			String userNo = params.get("userNo");
			//校验用户
			Map<String,Object> checkWhereMap = new HashMap<String, Object>();
			checkWhereMap.put("user_no",userNo);
			Map<String,Object> userMap = apiService.getOneMethod("user", checkWhereMap, "id", "desc", 0);
			if(userMap==null || userMap.isEmpty()){
				log.info("无此用户");
				msg = "无此用户,查询码:"+userNo;
				String jsonStr = buildJson("FAIL",null,msg);
				log.info("jsonStr===="+jsonStr);
				outJson(jsonStr, response);
				return;
			}
			int weiCoin = (Integer)userMap.get("wei_coin");
			if(weiCoin<10){
				log.info("微币不足");
				msg = "微币不足";
				String jsonStr = buildJson("FAIL",null,msg);
				log.info("jsonStr===="+jsonStr);
				outJson(jsonStr, response);
				return;
			}
			userService.updateUserCoin(userNo,"less",10);
			Map<String,Object> verifiedWhereMap = new HashMap<String, Object>();
			verifiedWhereMap.put("bankcard",bankcard);
			verifiedWhereMap.put("idcard", idcard);
			verifiedWhereMap.put("realname", realname);
			verifiedWhereMap.put("mobile_no", mobileNo);
			Map<String,Object> verifiedHistoryMap = apiService.getOneMethod("verified_history", verifiedWhereMap, "id", "desc", 0);
			if(verifiedHistoryMap==null || verifiedHistoryMap.isEmpty()){
				log.info("无此用户实名认证历史记录");
				String isok = ""; //isok 是否查询成功 0：查询失败 ， 1：查询成功
				String code = "";
				String bankname = "";
				String cardname = "";
				String yfbErrCode = "init";
				//获取response的body
				if(!StringUtils.isEmpty(bankcard) && !StringUtils.isEmpty(idcard) && !StringUtils.isEmpty(realname) && !StringUtils.isEmpty(mobileNo)){
					String responseBody = SearchNewApiUtil.getNewVerifiedResultByYFB(bankcard,idcard,realname,mobileNo);
					log.info("移付宝查询结果:"+responseBody);
					JSONObject yfbBodyJson = JSONObject.parseObject(responseBody);
					yfbErrCode = String.valueOf(yfbBodyJson.get("errCode"));
					String errMsg = String.valueOf(yfbBodyJson.get("errMsg"));
				}
				if("0000".equals(yfbErrCode)){
					isok = "1";
					code = "1";
				}else if("0001".equals(yfbErrCode)){
					isok = "1";
					code = "2";
				}else{
					String responseBody = SearchNewApiUtil.getNewVerifiedResult(bankcard,idcard,realname,mobileNo);
					//String responseBody = "{\"status\":\"0\",\"msg\":\"ok\",\"result\":{\"bankcard\":\"6226220617514830\",\"realname\":\"卢紫俊\",\"idcard\":\"421087199210082734\",\"verifystatus\":\"0\",\"verifymsg\":\"恭喜您，银行卡号校验一致！\"}}";
					//String responseBody = "{\"status\":\"204\",\"msg\":\"真实姓名包含特殊字符\",\"result\":\"\"}";
					//String responseBody = "{\"status\":\"0\",\"msg\":\"ok\",\"result\":{\"bankcard\":\"6226220617514830\",\"realname\":\"卢紫\",\"idcard\":\"421087199210082734\",\"verifystatus\":\"1\",\"verifymsg\":\"抱歉，银行卡号校验不一致！\"}}";
					//String responseBody = "{\"isok\":1,\"code\":1,\"data\":{\"bankname\":\"民生银行\",\"cardname\":\"民生借记卡(银联卡)\"}}";
					//String responseBody = "{\"isok\":0,\"code\":2,\"data\":{\"bankname\":\"农业银行\",\"cardname\":\"金穗通宝卡(银联卡)\"}}";
					log.info("阿里云实名认证通道返回:"+responseBody);
					JSONObject bodyJson = JSONObject.parseObject(responseBody);
					isok = String.valueOf(bodyJson.get("isok"));
					code = String.valueOf(bodyJson.get("code"));
					JSONObject resultJson = (JSONObject)bodyJson.get("data");
					if(resultJson!=null){
						bankname = String.valueOf(resultJson.get("bankname"));
						cardname = String.valueOf(resultJson.get("cardname"));
					}
				}

				if("0".equals(isok)){
					if("11".equals(code)){
						resultBoo = false;
						msg = "参数不正确";
					}else if("20".equals(code)){
						resultBoo = false;
						msg = "身份证中心维护中";
					}else {
						resultBoo = false;
						msg = "查询失败，请截图反馈给公众号补偿微币";
					}
				}else if("1".equals(isok)){
					log.info("实名认证查询,isok="+isok);
				}else{
					log.info("实名认证查询出错");
					resultBoo = false;
					msg = "未查到相关纪录，请截图反馈给公众号补偿微币";
				}

				if(resultBoo){
					Map<String,Object> paramsMap = new HashMap<String,Object>();
					paramsMap.put("bankname",bankname);
					paramsMap.put("cardname",cardname);
					paramsMap.put("bankcard",bankcard);
					paramsMap.put("idcard",idcard);
					paramsMap.put("realname",realname);
					paramsMap.put("mobile_no",mobileNo);
					paramsMap.put("isok",isok);
					paramsMap.put("code",code);
					paramsMap.put("create_time",new Date());
					paramsMap.put("user_no",userNo);
					apiService.insertMethod("verified_history", paramsMap);
					apiService.addApiAccessNum("use_num", "verified");
					verifiedHistoryMap = apiService.getOneMethod("verified_history", verifiedWhereMap,"id","desc",0);
				}
			}else{
				log.info("有此用户实名认证历史记录");
				String hisUserNo = String.valueOf(verifiedHistoryMap.get("user_no"));
				String hisId = String.valueOf(verifiedHistoryMap.get("id"));
				Map<String,Object> updateMap = new HashMap<String,Object>();
				updateMap.put("user_no", hisUserNo + ";" + userNo);
				Map<String, Object> whereMap = new HashMap<String,Object>();
				whereMap.put("id", hisId);
				apiService.updateMethod("verified_history", updateMap, whereMap);
				apiService.addApiAccessNum("use_num", "verified");
			}
			log.info("verifiedHistoryMap=====" + verifiedHistoryMap);
			if(resultBoo){
				String jsonStr = buildJson("SUCCESS", verifiedHistoryMap,"");
				log.info("jsonStr===="+jsonStr);
				outJson(jsonStr, response);
			}else{
				String jsonStr = buildJson("FAIL",null,msg);
				log.info("jsonStr===="+jsonStr);
				outJson(jsonStr, response);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			String jsonStr = buildJson("FAIL",null,"系统异常，请截图反馈给公众号补偿微币");
			outJson(jsonStr, response);
		}
	}

	public void insertVerifiedHistory(){

	}

	@RequestMapping(value = "dishonestyPage", method = RequestMethod.GET)
	public String dishonestyPage(final ModelMap model, @RequestParam Map<String, String> params) {
		log.info("---------dishonestyPage---------");
		try {
			String userNo = params.get("userNo");
			Map<String,Object> whereMap = new HashMap<String, Object>();
			whereMap.put("user_no",userNo);
			Map<String,Object> userMap = apiService.getOneMethod("user", whereMap, "id", "desc", 0);
			if(userMap==null || userMap.isEmpty()){
				model.put("errorMsg","无此用户");
				model.put("errorCode",userNo);
				return "errorPage";
			}else{
				model.put("userNo",userNo);
				apiService.addApiAccessNum("view_num", "dishonesty");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			model.put("errorMsg", "系统异常");
			model.put("errorCode", "100000000");
			return "errorPage";
		}
		return "api/dishonesty";
	}

	@RequestMapping(value = "dishonestyResult", method = RequestMethod.GET)
	public void dishonestyResult(HttpServletResponse response, @RequestParam Map<String, String> params) {
		log.info("---------dishonestyResult---------params===="+params);
		Boolean resultBoo = true;
		String msg = "";
		try {
			String sx_name = params.get("sxName");
			String userNo = params.get("userNo");
			//校验用户
			Map<String,Object> checkWhereMap = new HashMap<String, Object>();
			checkWhereMap.put("user_no",userNo);
			Map<String,Object> userMap = apiService.getOneMethod("user", checkWhereMap, "id", "desc", 0);
			if(userMap==null || userMap.isEmpty()){
				log.info("无此用户");
				msg = "无此用户,查询码:"+userNo;
				String jsonStr = buildJson("FAIL",null,msg);
				log.info("jsonStr===="+jsonStr);
				outJson(jsonStr, response);
				return;
			}
			int weiCoin = (Integer)userMap.get("wei_coin");
			if(weiCoin<18){
				log.info("微币不足");
				msg = "微币不足";
				String jsonStr = buildJson("FAIL",null,msg);
				log.info("jsonStr===="+jsonStr);
				outJson(jsonStr, response);
				return;
			}
			userService.updateUserCoin(userNo, "less", 18);
			Map<String,Object> dishonestyWhereMap = new HashMap<String, Object>();
			dishonestyWhereMap.put("sx_name","%"+sx_name+"%");
			List<Map<String,Object>> dishonestyHistoryList = apiService.getListMethod("dishonesty_sx_history", dishonestyWhereMap, "sx_publicdate", "asc", 10);
			if(dishonestyHistoryList==null || dishonestyHistoryList.isEmpty()){
				log.info("无此失信历史记录");
				//获取response的body
				String responseBody = SearchNewApiUtil.getNewDishonestyResult("json",null,null,null,null,sx_name);
				//String responseBody = "{\"Status\":\"200\",\"Message\":\"查询成功\",\"Paging\":null,\"Result\":{\"ShiXinResult\":{\"Paging\":{\"PageSize\":10,\"PageIndex\":1,\"TotalRecords\":4},\"Items\":[{\"Id\":\"240a5c635698b4e9632620c1e333bef92\",\"Sourceid\":0,\"Uniqueno\":\"\",\"Name\":\"深圳市快播科技有限公司\",\"Liandate\":\"2014-09-19T00:00:00+08:00\",\"Anno\":\"(2014)深南法执字第03105号\",\"Orgno\":\"670046067\",\"Ownername\":\"王欣\",\"Executegov\":\"南山区人民法院\",\"Province\":\"黑龙江\",\"Executeunite\":\"深圳市南山区人民法院\",\"Yiwu\":\"详见调解书。\",\"Executestatus\":\"全部未履行\",\"Actionremark\":\"违反财产报告制度\",\"Publicdate\":\"2014-12-08\",\"Follows\":0,\"Age\":0,\"Sexy\":\"\",\"Createdate\":\"0001-01-01T00:00:00\",\"Updatedate\":\"0001-01-01T00:00:00\",\"Executeno\":\"（2014）深南法蛇民初字第706号\",\"Performedpart\":\"\",\"Unperformpart\":\"\",\"Isperson\":0},{\"Id\":\"6d4384671a40bdecaee3cb75b0b06c4d2\",\"Sourceid\":0,\"Uniqueno\":\"\",\"Name\":\"深圳市快播科技有限公司\",\"Liandate\":\"2015-08-03T00:00:00+08:00\",\"Anno\":\"(2015)深南法执字第03400号\",\"Orgno\":\"67004606-7\",\"Ownername\":\"　王欣\",\"Executegov\":\"南山区人民法院\",\"Province\":\"黑龙江\",\"Executeunite\":\"深圳市中级人民法院\",\"Yiwu\":\"共8案赔偿损失共16万元及利息。\",\"Executestatus\":\"全部未履行\",\"Actionremark\":\"其他有履行能力而拒不履行生效法律文书确定义务\",\"Publicdate\":\"2015-11-11\",\"Follows\":0,\"Age\":0,\"Sexy\":\"\",\"Createdate\":\"0001-01-01T00:00:00\",\"Updatedate\":\"0001-01-01T00:00:00\",\"Executeno\":\"（2015）深中法知民终字第674号\",\"Performedpart\":\"\",\"Unperformpart\":\"\",\"Isperson\":0},{\"Id\":\"844a2a77997f89817a4dd1895bb7a8502\",\"Sourceid\":0,\"Uniqueno\":\"\",\"Name\":\"深圳市快播科技有限公司\",\"Liandate\":\"2015-09-08T00:00:00+08:00\",\"Anno\":\"(2015)深南法执字第03895号\",\"Orgno\":\"67004606-7\",\"Ownername\":\"王欣\",\"Executegov\":\"南山区人民法院\",\"Province\":\"黑龙江\",\"Executeunite\":\"深圳市中级人民法院\",\"Yiwu\":\"支付人民币22万元及利息；支付案件受理费人民币13280元。\",\"Executestatus\":\"全部未履行\",\"Actionremark\":\"违反财产报告制度,其他有履行能力而拒不履行生效法律文书确定义务\",\"Publicdate\":\"2015-11-02\",\"Follows\":0,\"Age\":0,\"Sexy\":\"\",\"Createdate\":\"0001-01-01T00:00:00\",\"Updatedate\":\"0001-01-01T00:00:00\",\"Executeno\":\"（2015）深中法知民终字第108号\",\"Performedpart\":\"\",\"Unperformpart\":\"\",\"Isperson\":0},{\"Id\":\"32650c4aab35aad5583489e4e707502a2\",\"Sourceid\":0,\"Uniqueno\":\"\",\"Name\":\"深圳市快播科技有限公司\",\"Liandate\":\"2015-09-08T00:00:00+08:00\",\"Anno\":\"(2015)深南法执字第03896号\",\"Orgno\":\"67004606-7\",\"Ownername\":\"王欣\",\"Executegov\":\"南山区人民法院\",\"Province\":\"黑龙江\",\"Executeunite\":\"深圳市中级人民法院\",\"Yiwu\":\"支付人民币22万元及利息；支付案件受理费人民币13280元。\",\"Executestatus\":\"全部未履行\",\"Actionremark\":\"违反财产报告制度,其他有履行能力而拒不履行生效法律文书确定义务\",\"Publicdate\":\"2015-11-02\",\"Follows\":0,\"Age\":0,\"Sexy\":\"\",\"Createdate\":\"0001-01-01T00:00:00\",\"Updatedate\":\"0001-01-01T00:00:00\",\"Executeno\":\"（2015）深中法知民终字第424号\",\"Performedpart\":\"\",\"Unperformpart\":\"\",\"Isperson\":0}]},\"ZhiXingResult\":{\"Paging\":{\"PageSize\":10,\"PageIndex\":1,\"TotalRecords\":100},\"Items\":[{\"Id\":\"9ed4fd774e62a8357b63db37bf54570c\",\"Sourceid\":0,\"Name\":\"深圳市快播科技有限公司\",\"Liandate\":\"2015-08-03T00:00:00+08:00\",\"Anno\":\"(2015)深南法执字第03401号\",\"Follows\":0,\"ExecuteGov\":\"南山区人民法院\",\"Biaodi\":\"20000\",\"Status\":\"执行中\",\"PartyCardNum\":\"0\",\"Createdate\":\"0001-01-01T00:00:00\",\"Updatedate\":\"0001-01-01T00:00:00\"},{\"Id\":\"4d88a3b437328437747ff70d996cda04\",\"Sourceid\":0,\"Name\":\"深圳市快播科技有限公司\",\"Liandate\":\"2014-09-17T00:00:00+08:00\",\"Anno\":\"(2014)深南法执字第03092号\",\"Follows\":0,\"ExecuteGov\":\"南山区人民法院\",\"Biaodi\":\"297000\",\"Status\":\"执行中\",\"PartyCardNum\":\"670046067\",\"Createdate\":\"0001-01-01T00:00:00\",\"Updatedate\":\"0001-01-01T00:00:00\"},{\"Id\":\"fa6273cbd38c1c5d2fb7bedbbe2b6c06\",\"Sourceid\":0,\"Name\":\"深圳市快播科技有限公司\",\"Liandate\":\"2015-01-08T00:00:00+08:00\",\"Anno\":\"(2015)深南法执字第00795号\",\"Follows\":0,\"ExecuteGov\":\"南山区人民法院\",\"Biaodi\":\"356900\",\"Status\":\"执行中\",\"PartyCardNum\":\"0\",\"Createdate\":\"0001-01-01T00:00:00\",\"Updatedate\":\"0001-01-01T00:00:00\"},{\"Id\":\"f3490886d81411141e9bc8774498acea\",\"Sourceid\":0,\"Name\":\"深圳市快播科技有限公司\",\"Liandate\":\"2014-09-17T00:00:00+08:00\",\"Anno\":\"(2014)深南法执字第03091号\",\"Follows\":0,\"ExecuteGov\":\"南山区人民法院\",\"Biaodi\":\"86935\",\"Status\":\"执行中\",\"PartyCardNum\":\"670046067\",\"Createdate\":\"0001-01-01T00:00:00\",\"Updatedate\":\"0001-01-01T00:00:00\"},{\"Id\":\"c1395c5ee79d48165227f78198994cae\",\"Sourceid\":0,\"Name\":\"深圳市快播科技有限公司\",\"Liandate\":\"2015-07-21T00:00:00+08:00\",\"Anno\":\"(2015)深南法执字第03236号\",\"Follows\":0,\"ExecuteGov\":\"南山区人民法院\",\"Biaodi\":\"686296\",\"Status\":\"执行中\",\"PartyCardNum\":\"0\",\"Createdate\":\"0001-01-01T00:00:00\",\"Updatedate\":\"0001-01-01T00:00:00\"},{\"Id\":\"03501628ae5a5cfe8c1ecb2dd8021419\",\"Sourceid\":0,\"Name\":\"深圳市快播科技有限公司\",\"Liandate\":\"2015-05-15T00:00:00+08:00\",\"Anno\":\"(2015)深南法执字第02272号\",\"Follows\":0,\"ExecuteGov\":\"南山区人民法院\",\"Biaodi\":\"111550\",\"Status\":\"已结案\",\"PartyCardNum\":\"0\",\"Createdate\":\"0001-01-01T00:00:00\",\"Updatedate\":\"0001-01-01T00:00:00\"},{\"Id\":\"cd065703a3b81f06ffb4207a0f1fc13d\",\"Sourceid\":0,\"Name\":\"深圳市快播科技有限公司\",\"Liandate\":\"2015-05-21T00:00:00+08:00\",\"Anno\":\"(2015)深南法执字第02351号\",\"Follows\":0,\"ExecuteGov\":\"南山区人民法院\",\"Biaodi\":\"218800\",\"Status\":\"已结案\",\"PartyCardNum\":\"0\",\"Createdate\":\"0001-01-01T00:00:00\",\"Updatedate\":\"0001-01-01T00:00:00\"},{\"Id\":\"2bb75f78a85c2df73194e5e69d57ed7f\",\"Sourceid\":0,\"Name\":\"深圳市快播科技有限公司\",\"Liandate\":\"2015-07-21T00:00:00+08:00\",\"Anno\":\"(2015)深南法执字第03238号\",\"Follows\":0,\"ExecuteGov\":\"南山区人民法院\",\"Biaodi\":\"350416\",\"Status\":\"执行中\",\"PartyCardNum\":\"0\",\"Createdate\":\"0001-01-01T00:00:00\",\"Updatedate\":\"0001-01-01T00:00:00\"},{\"Id\":\"815c4e3b8ce6f26a88ab9db110a44c9d\",\"Sourceid\":0,\"Name\":\"深圳市快播科技有限公司\",\"Liandate\":\"2015-01-08T00:00:00+08:00\",\"Anno\":\"(2015)深南法执字第00777号\",\"Follows\":0,\"ExecuteGov\":\"南山区人民法院\",\"Biaodi\":\"213800\",\"Status\":\"已结案\",\"PartyCardNum\":\"0\",\"Createdate\":\"0001-01-01T00:00:00\",\"Updatedate\":\"0001-01-01T00:00:00\"},{\"Id\":\"bbdaaacf431bf4dd3d4678168217968e\",\"Sourceid\":0,\"Name\":\"深圳市快播科技有限公司\",\"Liandate\":\"2015-05-21T00:00:00+08:00\",\"Anno\":\"(2015)深南法执字第02363号\",\"Follows\":0,\"ExecuteGov\":\"南山区人民法院\",\"Biaodi\":\"115800\",\"Status\":\"已结案\",\"PartyCardNum\":\"0\",\"Createdate\":\"0001-01-01T00:00:00\",\"Updatedate\":\"0001-01-01T00:00:00\"}]}}}";
				//String responseBody = "{\"Status\":\"201\",\"Message\":\"查询无结果\",\"Paging\":null,\"Result\":null}";
				log.info(responseBody);
				JSONObject bodyJson = JSONObject.parseObject(responseBody);
				String Status = String.valueOf(bodyJson.get("Status"));
				String Message = String.valueOf(bodyJson.get("Message"));
				if("200".equals(Status)){
					JSONObject resultJson = (JSONObject)bodyJson.get("Result");
					JSONObject ShiXinResultJson = (JSONObject)resultJson.get("ShiXinResult");
					JSONObject ZhiXingResultJson = (JSONObject)resultJson.get("ZhiXingResult");
					JSONArray ShiXinItems = ShiXinResultJson.getJSONArray("Items");
					JSONArray ZhiXingItems = ZhiXingResultJson.getJSONArray("Items");
					for(int i=0;i<ShiXinItems.size();i++){
						JSONObject itemJson = (JSONObject)ShiXinItems.get(i);
						String Id = String.valueOf(itemJson.get("Id"));
						String Name = String.valueOf(itemJson.get("Name"));
						String Liandate = String.valueOf(itemJson.get("Liandate"));
						if(Liandate.contains("T")){
							Liandate = Liandate.substring(0,Liandate.indexOf("T"));
						}
						String Anno = String.valueOf(itemJson.get("Anno"));
						String Orgno = String.valueOf(itemJson.get("Orgno"));
						String Ownername = String.valueOf(itemJson.get("Ownername"));
						String Executegov = String.valueOf(itemJson.get("Executegov"));
						String Province = String.valueOf(itemJson.get("Province"));
						String Executeunite = String.valueOf(itemJson.get("Executeunite"));
						String Yiwu = String.valueOf(itemJson.get("Yiwu"));
						String Executestatus = String.valueOf(itemJson.get("Executestatus"));
						String Actionremark = String.valueOf(itemJson.get("Actionremark"));
						String Publicdate = String.valueOf(itemJson.get("Publicdate"));
						String Age = String.valueOf(itemJson.get("Age"));
						String Sexy = String.valueOf(itemJson.get("Sexy"));
						String Executeno = String.valueOf(itemJson.get("Executeno"));
						String Performedpart = String.valueOf(itemJson.get("Performedpart"));
						String Unperformpart = String.valueOf(itemJson.get("Unperformpart"));
						String Isperson = String.valueOf(itemJson.get("Isperson"));
						Map<String,Object> paramsMap = new HashMap<String,Object>();
						paramsMap.put("sx_id",Id);
						paramsMap.put("sx_name",Name);
						paramsMap.put("sx_liandate",Liandate);
						paramsMap.put("sx_anno",Anno);
						paramsMap.put("sx_orgno",Orgno);
						paramsMap.put("sx_ownername",Ownername);
						paramsMap.put("sx_executegov",Executegov);
						paramsMap.put("sx_province",Province);
						paramsMap.put("sx_executeunite",Executeunite);
						paramsMap.put("sx_yiwu",Yiwu);
						paramsMap.put("sx_executestatus",Executestatus);
						paramsMap.put("sx_actionremark",Actionremark);
						paramsMap.put("sx_publicdate",Publicdate);
						paramsMap.put("sx_age",Age);
						paramsMap.put("sx_sexy",Sexy);
						paramsMap.put("sx_executeno",Executeno);
						paramsMap.put("sx_performedpart",Performedpart);
						paramsMap.put("sx_unperformpart",Unperformpart);
						paramsMap.put("sx_isperson",Isperson);
						paramsMap.put("create_time", new Date());
						paramsMap.put("user_no", userNo);
						Map<String,Object> findWhereMap = new HashMap<String, Object>();
						findWhereMap.put("sx_id",Id);
						Map<String,Object> hisMap = apiService.getOneMethod("dishonesty_sx_history",findWhereMap,"id","desc",0);
						if(hisMap==null || hisMap.isEmpty()){
							apiService.insertMethod("dishonesty_sx_history", paramsMap);
						}
					}
					apiService.addApiAccessNum("use_num", "dishonesty");
					dishonestyHistoryList = apiService.getListMethod("dishonesty_sx_history", dishonestyWhereMap, "sx_publicdate", "asc", 10);
					for(int i=0;i<ZhiXingItems.size();i++){
						JSONObject itemJson = (JSONObject)ZhiXingItems.get(i);
						String Id = String.valueOf(itemJson.get("Id"));
						String Name = String.valueOf(itemJson.get("Name"));
						String Liandate = String.valueOf(itemJson.get("Liandate"));
						if(Liandate.contains("T")){
							Liandate = Liandate.substring(0,Liandate.indexOf("T"));
						}
						String Anno = String.valueOf(itemJson.get("Anno"));
						String ExecuteGov = String.valueOf(itemJson.get("ExecuteGov"));
						String Biaodi = String.valueOf(itemJson.get("Biaodi"));
						String ZhiXingStatus = String.valueOf(itemJson.get("Status"));
						String PartyCardNum = String.valueOf(itemJson.get("PartyCardNum"));
						Map<String,Object> paramsMap = new HashMap<String,Object>();
						paramsMap.put("zx_id",Id);
						paramsMap.put("zx_name",Name);
						paramsMap.put("zx_liandate",Liandate);
						paramsMap.put("zx_anno",Anno);
						paramsMap.put("zx_executegov",ExecuteGov);
						paramsMap.put("zx_biaodi",Biaodi);
						paramsMap.put("zx_status",ZhiXingStatus);
						paramsMap.put("zx_partycardnum",PartyCardNum);
						paramsMap.put("create_time", new Date());
						paramsMap.put("user_no", userNo);
						Map<String,Object> findWhereMap = new HashMap<String, Object>();
						findWhereMap.put("zx_id",Id);
						Map<String,Object> hisMap = apiService.getOneMethod("dishonesty_zx_history",findWhereMap,"id","desc",0);
						if(hisMap==null || hisMap.isEmpty()){
							apiService.insertMethod("dishonesty_zx_history", paramsMap);
						}

					}
				}else if("201".equals(Status)){
					msg = "信用良好";
				}else{
					resultBoo = false;
					msg = Message;
				}
			}else{
				log.info("有此关键词失信历史记录");
			}
			log.info("dishonestyHistoryList=====" + dishonestyHistoryList);
			if(resultBoo){
				String jsonStr = buildJson("SUCCESS", dishonestyHistoryList,"");
				log.info("jsonStr===="+jsonStr);
				outJson(jsonStr, response);
			}else{
				String jsonStr = buildJson("FAIL",null,msg);
				log.info("jsonStr===="+jsonStr);
				outJson(jsonStr, response);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			String jsonStr = buildJson("FAIL",null,"系统异常");
			outJson(jsonStr, response);
		}
	}

	@RequestMapping(value = "loadDishonestyDetail", method = RequestMethod.GET)
	public void loadDishonestyDetail(HttpServletResponse response, @RequestParam Map<String, String> params) {
		log.info("---------loadDishonestyDetail---------params===="+params);
		try {
			String id = params.get("id");
			Map<String,Object> whereMap = new HashMap<String,Object>();
			whereMap.put("id", id);
			Map<String,Object> dishonestyDetailMap = apiService.getOneMethod("dishonesty_sx_history", whereMap, "id", "desc",0);
			String listJsonStr = JSON.toJSONString(dishonestyDetailMap);
			log.info("listJsonStr====" + listJsonStr);
			outJson(listJsonStr, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@RequestMapping(value = "zipcodePage", method = RequestMethod.GET)
	public String zipcodePage(final ModelMap model, @RequestParam Map<String, String> params) {
		log.info("---------zipcodePage---------");
		try {
			String userNo = params.get("userNo");
			Map<String,Object> whereMap = new HashMap<String, Object>();
			whereMap.put("user_no",userNo);
			Map<String,Object> userMap = apiService.getOneMethod("user", whereMap, "id", "desc", 0);
			if(userMap==null || userMap.isEmpty()){
				model.put("errorMsg","无此用户");
				model.put("errorCode",userNo);
				return "errorPage";
			}else{
				model.put("userNo",userNo);
				apiService.addApiAccessNum("view_num", "zipcode");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			model.put("errorMsg", "系统异常");
			model.put("errorCode", "100000000");
			return "errorPage";
		}
		return "api/zipcode";
	}


	@RequestMapping(value = "zipcodeResult", method = RequestMethod.GET)
	public void zipcodeResult(HttpServletResponse response, @RequestParam Map<String, String> params) {
		log.info("---------zipcodeResult---------params===="+params);
		Boolean resultBoo = true;
		String msg = "";
		try {
			String cityPicker = params.get("cityPicker");
			String citys[] = cityPicker.split(" ");
			String province = citys[0];
			String city = citys[1];
			String town = citys[2];
			String areaid = apiService.getAreaId(province,city,town);
			String searchKey = params.get("searchKey");
			String userNo = params.get("userNo");
			//校验用户
			Map<String,Object> checkWhereMap = new HashMap<String, Object>();
			checkWhereMap.put("user_no",userNo);
			Map<String,Object> userMap = apiService.getOneMethod("user", checkWhereMap, "id", "desc", 0);
			if(userMap==null || userMap.isEmpty()){
				log.info("无此用户");
				msg = "无此用户,查询码:"+userNo;
				String jsonStr = buildJson("FAIL",null,msg);
				log.info("jsonStr===="+jsonStr);
				outJson(jsonStr, response);
				return;
			}
			int weiCoin = (Integer)userMap.get("wei_coin");
			if(weiCoin<2){
				log.info("微币不足");
				msg = "微币不足";
				String jsonStr = buildJson("FAIL",null,msg);
				log.info("jsonStr===="+jsonStr);
				outJson(jsonStr, response);
				return;
			}
			userService.updateUserCoin(userNo,"less",2);
			List<Map<String,Object>> zipcodeHistoryList = apiService.getZipcodeHistory(areaid,searchKey);
			if(zipcodeHistoryList==null || zipcodeHistoryList.isEmpty()){
				log.info("无此邮编查询历史记录");
				//获取response的body
				String responseBody = SearchNewApiUtil.getNewZipcodeResult(areaid, searchKey);
				//String responseBody = "{\"status\":\"0\",\"msg\":\"ok\",\"result\":[{\"province\":\"广东\",\"city\":\"深圳\",\"town\":\"南山区\",\"address\":\"侨香路东方科技园公寓\",\"zipcode\":\"518053\"},{\"province\":\"广东\",\"city\":\"深圳\",\"town\":\"南山区\",\"address\":\"侨香路东方科技园\",\"zipcode\":\"518053\"},{\"province\":\"广东\",\"city\":\"深圳\",\"town\":\"南山区\",\"address\":\"西丽街道平山民企科技园\",\"zipcode\":\"518055\"},{\"province\":\"广东\",\"city\":\"深圳\",\"town\":\"南山区\",\"address\":\"科苑路科技园大厦\",\"zipcode\":\"518057\"},{\"province\":\"广东\",\"city\":\"深圳\",\"town\":\"南山区\",\"address\":\"科兴路科技园老住宅区\",\"zipcode\":\"518057\"},{\"province\":\"广东\",\"city\":\"深圳\",\"town\":\"南山区\",\"address\":\"翠溪路科技园住宅区\",\"zipcode\":\"518057\"}]}";
				//String responseBody = "{\"status\":\"204\",\"msg\":\"没有信息\",\"result\":\"\"}";
				log.info(responseBody);
				JSONObject bodyJson = JSONObject.parseObject(responseBody);
				String status = String.valueOf(bodyJson.get("status"));
				String responseMsg = String.valueOf(bodyJson.get("msg"));
				if("0".equals(status) && "ok".equals(responseMsg)){
					JSONArray resultJsonArr = (JSONArray)bodyJson.get("result");
					System.out.println("resultJsonArr====" + resultJsonArr.toString());
					if(resultJsonArr!=null){
						for(Object obj:resultJsonArr){
							JSONObject resultJson = (JSONObject)obj;
							Map<String,Object> paramsMap = new HashMap<String,Object>();
							String resultProvince = String.valueOf(resultJson.get("province"));
							String resultCity = String.valueOf(resultJson.get("city"));
							String resultTown = String.valueOf(resultJson.get("town"));
							String resultAddress = String.valueOf(resultJson.get("address"));
							String resultZipcode = String.valueOf(resultJson.get("zipcode"));
							paramsMap.put("create_time",new Date());
							paramsMap.put("user_no",userNo);
							paramsMap.put("areaid",areaid);
							paramsMap.put("search_key",searchKey);
							paramsMap.put("province",resultProvince);
							paramsMap.put("city",resultCity);
							paramsMap.put("town",resultTown);
							paramsMap.put("address",resultAddress);
							paramsMap.put("zipcode",resultZipcode);
							Map<String,Object> findWhereMap = new HashMap<String, Object>();
							findWhereMap.put("areaid",areaid);
							findWhereMap.put("address",resultAddress);
							Map<String,Object> hisMap = apiService.getOneMethod("zipcode_history",findWhereMap,"id","desc",0);
							if(hisMap==null || hisMap.isEmpty()){
								apiService.insertMethod("zipcode_history", paramsMap);
							}
							apiService.addApiAccessNum("use_num", "zipcode");
							zipcodeHistoryList = apiService.getZipcodeHistory(areaid, searchKey);
						}
					}else{
						resultBoo = false;
						msg = "没有信息";
					}
				}else if("204".equals(status)){
					resultBoo = false;
					msg = "未查到记录,请检查省市区或更换地址关键词";
				}else{
					resultBoo = false;
					msg = "没有信息";
				}
			}else{
				log.info("有此邮编查询历史记录");
			}
			log.info("zipcodeHistoryList=====" + zipcodeHistoryList);
			if(resultBoo){
				String jsonStr = buildJson("SUCCESS", zipcodeHistoryList,"");
				log.info("jsonStr===="+jsonStr);
				outJson(jsonStr, response);
			}else{
				String jsonStr = buildJson("FAIL",null,msg);
				log.info("jsonStr===="+jsonStr);
				outJson(jsonStr, response);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			String jsonStr = buildJson("FAIL",null,"系统异常");
			outJson(jsonStr, response);
		}
	}

	/**
	 * 笑话订阅
	 * @param model
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "jokePage", method = RequestMethod.GET)
	public String jokePage(final ModelMap model, @RequestParam Map<String, String> params) {
		log.info("---------jokePage---------");
		try {
			String userNo = params.get("userNo");
			Map<String,Object> whereMap = new HashMap<String, Object>();
			whereMap.put("user_no", userNo);
			Map<String,Object> userMap = apiService.getOneMethod("user", whereMap, "id", "desc", 0);
			if(userMap==null || userMap.isEmpty()){
				model.put("errorMsg","无此用户");
				model.put("errorCode",userNo);
				return "errorPage";
			}else{
				String subJoke = String.valueOf(userMap.get("sub_joke"));
				Map<String,Object> findWhereMap = new HashMap<String, Object>();
				findWhereMap.put("user_no",userNo);
				findWhereMap.put("status",0);
				List<Map<String,Object>> subMapList = apiService.getListMethod("sub_list", findWhereMap, "id", "asc", 0);
				System.out.println(JSONObject.toJSON(subMapList).toString());
				model.put("subMapList",JSONObject.toJSON(subMapList).toString());
				model.put("userNo",userNo);
				model.put("subJoke",subJoke);
				apiService.addApiAccessNum("view_num","joke");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			model.put("errorMsg", "系统异常");
			model.put("errorCode", "100000000");
			return "errorPage";
		}
		return "api/joke";
	}

	@RequestMapping(value = "jokeSubResult", method = RequestMethod.GET)
	public void jokeSubResult(HttpServletResponse response, @RequestParam Map<String, String> params) {
		log.info("---------jokeSubResult---------params===="+params);
		String msg = "";
		try {
			String checkedIndexs = params.get("checkedIndexs");
			String needPayCoin = params.get("needPayCoin");
			String userNo = params.get("userNo");
			//校验用户
			Map<String,Object> checkWhereMap = new HashMap<String, Object>();
			checkWhereMap.put("user_no",userNo);
			Map<String,Object> userMap = apiService.getOneMethod("user", checkWhereMap, "id", "desc", 0);
			if(userMap==null || userMap.isEmpty()){
				log.info("无此用户");
				msg = "无此用户,查询码:"+userNo;
				String jsonStr = buildJson("FAIL",null,msg);
				log.info("jsonStr===="+jsonStr);
				outJson(jsonStr, response);
				return;
			}
			int weiCoin = (Integer)userMap.get("wei_coin");
			if(weiCoin<Integer.parseInt(needPayCoin)){
				log.info("微币不足");
				msg = "微币不足";
				String jsonStr = buildJson("FAIL",null,msg);
				log.info("jsonStr===="+jsonStr);
				outJson(jsonStr, response);
				return;
			}
			userService.updateUserCoin(userNo, "less", Integer.parseInt(needPayCoin));
			Map<String,Object> changeSwitchUpdateMap = new HashMap<String, Object>();
			changeSwitchUpdateMap.put("sub_joke", 1);    //笑话订阅开关  0未订阅  1已订阅
			apiService.updateMethod("user", changeSwitchUpdateMap, checkWhereMap);
			String[] checkIndexArr = checkedIndexs.split(",");
			for(String checkIndex:checkIndexArr){
				Map<String,Object> findWhereMap = new HashMap<String, Object>();
				findWhereMap.put("user_no",userNo);
				findWhereMap.put("sub_index",checkIndex);
				Map<String,Object> subMap = apiService.getOneMethod("sub_list",findWhereMap,"id","asc",0);
				if(subMap==null || subMap.isEmpty()){
					log.info("--------尚未订阅该区间checkIndex=" + checkIndex + "--------");
					Map<String,Object> updateMap = new HashMap<String, Object>();
					updateMap.put("sub_joke", 1);    //笑话订阅开关  0未订阅  1已订阅
					apiService.updateMethod("user", updateMap, checkWhereMap);
					Map<String,Object> insertMap = new HashMap<String, Object>();
					insertMap.put("user_no",userNo);
					insertMap.put("sub_index",checkIndex);
					insertMap.put("status",0);
					insertMap.put("create_time",new Date());
					Calendar nowCal = Calendar.getInstance();
					insertMap.put("begin_time",nowCal.getTime());
					nowCal.add(Calendar.MONTH,1);
					insertMap.put("end_time",nowCal.getTime());
					apiService.insertMethod("sub_list",insertMap);
				}else{
					int status = (Integer)subMap.get("status");
					if(status==0){
						log.info("--------已订阅该区间，且已启用checkIndex=" + checkIndex + "--------");
						Map<String,Object> updateMap = new HashMap<String, Object>();
						Calendar nowCal = Calendar.getInstance();
						updateMap.put("begin_time",nowCal.getTime());
						nowCal.add(Calendar.MONTH, 1);
						updateMap.put("end_time",nowCal.getTime());
						findWhereMap.put("status",0);
						apiService.updateMethod("sub_list", updateMap, findWhereMap);
					}else if(status==1){
						log.info("--------已订阅该区间，且停用,重新启用checkIndex=" + checkIndex + "--------");
						Map<String,Object> updateMap = new HashMap<String, Object>();
						updateMap.put("status",0);
						Calendar nowCal = Calendar.getInstance();
						updateMap.put("begin_time",nowCal.getTime());
						nowCal.add(Calendar.MONTH, 1);
						updateMap.put("end_time",nowCal.getTime());
						findWhereMap.put("status",1);
						apiService.updateMethod("sub_list",updateMap,findWhereMap);
					}
				}
			}
			String jsonStr = buildJson("SUCCESS", null, "订阅成功,小微将在每个订阅时区内博您一笑");
			log.info("jsonStr====" + jsonStr);
			outJson(jsonStr, response);

		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			String jsonStr = buildJson("FAIL",null,"系统异常");
			outJson(jsonStr, response);
		}
	}

	/**
	 * 免费赠送7天笑话订阅
	 * @param checkedIndexs
	 * @param userNo
	 */
	public void freeSubJoke(String checkedIndexs,String userNo) {
		log.info("---------freeSubJoke---------");
		String msg = "";
		try {
			//校验用户
			Map<String,Object> checkWhereMap = new HashMap<String, Object>();
			checkWhereMap.put("user_no", userNo);
			Map<String,Object> userMap = apiService.getOneMethod("user", checkWhereMap, "id", "desc", 0);
			if(userMap==null || userMap.isEmpty()){
				log.info("无此用户");
				return;
			}
			Map<String,Object> changeSwitchUpdateMap = new HashMap<String, Object>();
			changeSwitchUpdateMap.put("sub_joke", 1);    //笑话订阅开关  0未订阅  1已订阅
			apiService.updateMethod("user", changeSwitchUpdateMap, checkWhereMap);
			String[] checkIndexArr = checkedIndexs.split(",");
			for(String checkIndex:checkIndexArr){
				Map<String,Object> findWhereMap = new HashMap<String, Object>();
				findWhereMap.put("user_no",userNo);
				findWhereMap.put("sub_index",checkIndex);
				Map<String,Object> subMap = apiService.getOneMethod("sub_list",findWhereMap,"id","asc",0);
				if(subMap==null || subMap.isEmpty()){
					log.info("--------尚未订阅该区间checkIndex=" + checkIndex + "--------");
					Map<String,Object> updateMap = new HashMap<String, Object>();
					updateMap.put("sub_joke", 1);    //笑话订阅开关  0未订阅  1已订阅
					apiService.updateMethod("user", updateMap, checkWhereMap);
					Map<String,Object> insertMap = new HashMap<String, Object>();
					insertMap.put("user_no",userNo);
					insertMap.put("sub_index",checkIndex);
					insertMap.put("status",0);
					insertMap.put("create_time",new Date());
					Calendar nowCal = Calendar.getInstance();
					insertMap.put("begin_time",nowCal.getTime());
					nowCal.add(Calendar.DAY_OF_MONTH,7);//送7天笑话
					insertMap.put("end_time",nowCal.getTime());
					apiService.insertMethod("sub_list",insertMap);
				}else{
					int status = (Integer)subMap.get("status");
					if(status==0){
						log.info("--------已订阅该区间，且已启用checkIndex=" + checkIndex + "--------");
						Map<String,Object> updateMap = new HashMap<String, Object>();
						Calendar nowCal = Calendar.getInstance();
						updateMap.put("begin_time",nowCal.getTime());
						nowCal.add(Calendar.DAY_OF_MONTH, 7);
						updateMap.put("end_time",nowCal.getTime());
						findWhereMap.put("status",0);
						apiService.updateMethod("sub_list", updateMap, findWhereMap);
					}else if(status==1){
						log.info("--------已订阅该区间，且停用,重新启用checkIndex=" + checkIndex + "--------");
						Map<String,Object> updateMap = new HashMap<String, Object>();
						updateMap.put("status",0);
						Calendar nowCal = Calendar.getInstance();
						updateMap.put("begin_time",nowCal.getTime());
						nowCal.add(Calendar.DAY_OF_MONTH, 7);
						updateMap.put("end_time",nowCal.getTime());
						findWhereMap.put("status",1);
						apiService.updateMethod("sub_list",updateMap,findWhereMap);
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "changeJokeSubSwitch", method = RequestMethod.GET)
	public void changeJokeSubSwitch(HttpServletResponse response, @RequestParam Map<String, String> params) {
		log.info("---------changeJokeSubSwitch---------params===="+params);
		String msg = "";
		try {
			String newSwitchStatus = params.get("newSwitchStatus");
			String userNo = params.get("userNo");
			//校验用户
			Map<String,Object> checkWhereMap = new HashMap<String, Object>();
			checkWhereMap.put("user_no", userNo);
			Map<String,Object> userMap = apiService.getOneMethod("user", checkWhereMap, "id", "desc", 0);
			if(userMap==null || userMap.isEmpty()){
				log.info("无此用户");
				msg = "无此用户,查询码:"+userNo;
				String jsonStr = buildJson("FAIL",null,msg);
				log.info("jsonStr===="+jsonStr);
				outJson(jsonStr, response);
				return;
			}
			Map<String,Object> updateMap = new HashMap<String, Object>();
			updateMap.put("sub_joke", newSwitchStatus);    //笑话订阅开关  0未订阅  1已订阅
			apiService.updateMethod("user", updateMap, checkWhereMap);
			if("1".equals(newSwitchStatus)){
				//查询所有已订阅时区
				checkWhereMap.put("status", 0);
				List<Map<String,Object>> subMapList = apiService.getListMethod("sub_list", checkWhereMap, "id", "asc", 0);
				String jsonStr = buildJson("SUCCESS", subMapList,"");
				log.info("jsonStr====" + jsonStr);
				outJson(jsonStr, response);
			}else{
				Map<String,Object> updateSubListMap = new HashMap<String, Object>();
				updateSubListMap.put("status",1);
				apiService.updateMethod("sub_list", updateSubListMap, checkWhereMap);
				String jsonStr = buildJson("SUCCESS", "操作成功", "");
				log.info("jsonStr====" + jsonStr);
				outJson(jsonStr, response);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			String jsonStr = buildJson("FAIL",null,"系统异常");
			outJson(jsonStr, response);
		}
	}

	@RequestMapping(value = "jokeListPage", method = RequestMethod.GET)
	public String jokeListPage(final ModelMap model, @RequestParam Map<String, String> params) {
		log.info("---------jokeListPage---------");
		try {
			String userNo = params.get("userNo");
			Map<String,Object> whereMap = new HashMap<String, Object>();
			whereMap.put("user_no", userNo);
			Map<String,Object> userMap = apiService.getOneMethod("user", whereMap, "id", "desc", 0);
			if(userMap==null || userMap.isEmpty()){
				model.put("errorMsg", "无此用户");
				model.put("errorCode", userNo);
				return "errorPage";
			}else{
				model.put("userNo", userNo);
				List<Map<String,Object>> jokeList = apiService.getListMethod("joke_list", null, "id", "desc", 20);
				for(Map<String,Object> map:jokeList){
					Date createTimeDate = (Date)map.get("create_time");
					SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
					String createTimeStr = sdf.format(createTimeDate);
					map.put("create_time",createTimeStr);
					String content = String.valueOf(map.get("content"));
					if(!StringUtils.isEmpty(content)){
						map.put("subContent",content.replace("<br />",""));
					}else{
						map.put("subContent",content);
					}
					/*if(!StringUtils.isEmpty(content) && content.length()>6){
						map.put("subContent",content.substring(0,6)+"...");
					}else{
						map.put("subContent",content);
					}*/
				}
				model.put("jokeList", jokeList);
				model.put("firstIndex", jokeList.get(jokeList.size() - 1).get("id"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			model.put("errorMsg", "系统异常");
			model.put("errorCode", "100000000");
			return "errorPage";
		}
		return "api/jokeList";
	}

	@RequestMapping(value = "loadJokeHistory", method = RequestMethod.GET)
	public void loadJokeHistory(HttpServletResponse response, @RequestParam Map<String, String> params) {
		log.info("---------loadJokeHistory---------params===="+params);
		try {
			String firstIndex = params.get("firstIndex");
			List<Map<String,Object>> jokeList = apiService.getNextJoke(0,Integer.parseInt(firstIndex), 20, null,"desc");
			for(Map<String,Object> map:jokeList){
				Date createTimeDate = (Date)map.get("create_time");
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				String createTimeStr = sdf.format(createTimeDate);
				map.put("create_time",createTimeStr);
				String content = String.valueOf(map.get("content"));
				if(!StringUtils.isEmpty(content)){
					map.put("subContent",content.replace("<br />",""));
				}else{
					map.put("subContent",content);
				}
			}
			String listJsonStr = JSON.toJSONString(jokeList);
			log.info("listJsonStr====" + listJsonStr);
			outJson(listJsonStr, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 插入最新笑话
	 * @param jokeType 笑话类型  text文字  pic图片  all所有
	 */
	public void insertNewJoke(String jokeType) {
		log.info("插入最新笑话");
		//获取response的body
		try {
			for(int i=1;i<11;i++){
				log.info("-------------插入最新笑话,第"+i+"次---------------");
				Boolean resultBoo = true;
				String responseBody = SearchNewApiUtil.getNewJokeResult(jokeType, ""+i, "20", "addtime");
				//String responseBody = "{\"status\":\"0\",\"msg\":\"ok\",\"result\":{\"total\":\"38117\",\"pagenum\":\"1\",\"pagesize\":\"20\",\"list\":[{\"content\":\"今天降温，站路边定车后跟师傅电话联系：“您在大厦南门停，看见一个快冻死的傻逼就是我，特好认。”说完哆哆嗦嗦挂了电话。过了一会儿，师傅又打过来了：“您好，这路边好几个傻逼，哪个是你？”\",\"pic\":\"\",\"addtime\":\"2016-11-25 16:15:04\",\"url\":\"http:\\/\\/m.kaixinhui.com\\/detail-38288.html\"},{\"content\":\"闺蜜因为喜欢网购和老公吵架，跑我这来了，刚来到就气愤的说:“他就是跪着求我，我也不会原谅他的。”一小时后，闺蜜说:“他如果来接我，我就勉为其难跟他走。”两小时后她说:“想想是我太任性了，他如果打电话让我回去今天这事就算了。”三小时后，闺蜜说:“我想想阳台上的衣服还没收，我先回去收衣服了哈。。。。”\",\"pic\":\"\",\"addtime\":\"2016-11-23 12:27:45\",\"url\":\"http:\\/\\/m.kaixinhui.com\\/detail-38287.html\"},{\"content\":\"\",\"pic\":\"http:\\/\\/m.kaixinhui.com\\/upload\\/201611\\/2016112311384910643.jpg\",\"addtime\":\"2016-11-23 11:38:49\",\"url\":\"http:\\/\\/m.kaixinhui.com\\/detail-38286.html\"},{\"content\":\"傍晚，室友和她男票出去散步，结果突然开始下雨，于是她男票把外套脱下来，遮在两个人头上挡雨。啊，听起来还挺美好的。但是直男永远不会辜负大家的期望，室友男票：“诶，你看我们像不像舞龙？”\",\"pic\":\"\",\"addtime\":\"2016-11-23 11:27:17\",\"url\":\"http:\\/\\/m.kaixinhui.com\\/detail-38285.html\"},{\"content\":\"知道又要下暴雨了，看我多机智\",\"pic\":\"http:\\/\\/m.kaixinhui.com\\/upload\\/201611\\/2016111717503290580.png\",\"addtime\":\"2016-11-17 17:50:33\",\"url\":\"http:\\/\\/m.kaixinhui.com\\/detail-38284.html\"},{\"content\":\"这智商不适合砍树\",\"pic\":\"http:\\/\\/m.kaixinhui.com\\/upload\\/201611\\/2016111717405776429.png\",\"addtime\":\"2016-11-17 17:40:57\",\"url\":\"http:\\/\\/m.kaixinhui.com\\/detail-38283.html\"},{\"content\":\"小学生优秀作文选：《当领导的好处》<br \\/>\\r\\n 爸爸当上领导后，从来不敢多说话，生怕给别人带来压力。上个月，下属小李到我家来玩，爸爸随口说了句：“家里没空调，比较热，见笑了。” <br \\/>\\r\\n　　第二天，小李带师傅给我家安装了5匹的大空调！怎么也不肯收钱。<br \\/>\\r\\n　　上个星期，爸爸看到老张在办公室吃午饭，其中有煎的鱼，随口说了句：“哎呦，真香！”！<br \\/>\\r\\n 当天晚上。老张给我家送了一箱鲜鱼来。<br \\/>\\r\\n<br \\/>\\r\\n　　有天晚上在路上，碰到单位的小王和他老婆逛街，爸爸随口称赞道：“哎呦，小王，你媳妇真漂亮！”<br \\/>\\r\\n　　第二天晚上，妈妈不在家，突然听到有人敲门，打开门一看，小王媳妇站在门口，见到爸爸就满脸的微笑说：“领导，我们家小王说嫂子不在家，让我来陪陪你！”<br \\/>\\r\\n<br \\/>\\r\\n 当领导真好，怪不得这么多人喜欢当领导！\",\"pic\":\"\",\"addtime\":\"2016-10-14 12:06:18\",\"url\":\"http:\\/\\/m.kaixinhui.com\\/detail-38274.html\"}]}}";
				log.info(responseBody);
				JSONObject bodyJson = JSONObject.parseObject(responseBody);
				String status = String.valueOf(bodyJson.get("status"));
				String responseMsg = String.valueOf(bodyJson.get("msg"));
				if ("0".equals(status) && "ok".equals(responseMsg)) {
					String resultJsonStr = String.valueOf(bodyJson.get("result"));
					JSONObject resultJson = JSONObject.parseObject(resultJsonStr);
					JSONArray listJsonArr = (JSONArray) resultJson.get("list");
					System.out.println("listJsonArr====" + listJsonArr.toString());
					if (listJsonArr != null) {
						for (Object obj : listJsonArr) {
							JSONObject listResultJson = (JSONObject) obj;
							Map<String, Object> paramsMap = new HashMap<String, Object>();
							String content = String.valueOf(listResultJson.get("content"));
							String pic = String.valueOf(listResultJson.get("pic"));
							String addtime = String.valueOf(listResultJson.get("addtime"));
							String url = String.valueOf(listResultJson.get("url"));
							String type = "";
							if(!StringUtils.isEmpty(content) && StringUtils.isEmpty(pic)){
								type = "text";
							}
							if(!StringUtils.isEmpty(pic)){
								type = "pic";
							}
							paramsMap.put("type",type);
							paramsMap.put("content",content);
							paramsMap.put("pic",pic);
							paramsMap.put("addtime",addtime);
							paramsMap.put("desc_url",url);
							paramsMap.put("create_time",new Date());
							paramsMap.put("batch",getBatch());
							Map<String,Object> findWhereMap = new HashMap<String, Object>();
							findWhereMap.put("desc_url",url);
							Map<String,Object> hisMap = apiService.getOneMethod("joke_list", findWhereMap, "id", "desc", 0);
							if(hisMap==null || hisMap.isEmpty()){
								apiService.insertMethod("joke_list", paramsMap);
							}else{
								resultBoo = false;
								break;
							}
						}//end for
						if(!resultBoo){
							break;
						}
					}
				}
			}//end for

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendJoke(){
		try {
			int subIndex = apiService.getNowSubIndex("joke");
			log.info("-------------发送笑话订阅subIndex="+subIndex+"--------------");
			if(subIndex!=0){
				Map<String,Object> findWhereMap = new HashMap<String, Object>();
				findWhereMap.put("sub_index",subIndex);
				findWhereMap.put("status",0);
				List<Map<String,Object>> subList = apiService.getListMethod("sub_list", findWhereMap, "id", "asc", 0);
				for(Map<String,Object> subMap:subList){
					String userNo = String.valueOf(subMap.get("user_no"));
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					String subBeginTime = sdf.format((Date)subMap.get("begin_time"));
					String subEndTime = sdf.format((Date)subMap.get("end_time"));
					log.info("subBeginTime="+subBeginTime+",subEndTime="+subEndTime);
					Map<String,Object> userMap = userService.selectUserByUserNo(userNo);
					if(userMap==null || userMap.isEmpty()){
						log.info("-----------无此用户----------");
					}else{
						int subJoke = (Integer)userMap.get("sub_joke");
						if(subJoke==0){
							log.info("-----------没有订阅----------");
						}else{
							log.info("-----------有订阅----------");
							String weixinUrl = weixinService.getParamValue("weixinUrl");
							String openid = String.valueOf(userMap.get("openid"));
							//查询该用户以发送的历史记录
							int lastHistoryJokeId = apiService.getLastHistoryJokeId(userNo);
							log.info("---------------lastHistoryJokeId="+lastHistoryJokeId+"-------------");
							List<Map<String,Object>> jokeList = apiService.getNextJoke(lastHistoryJokeId,0,5,"text","asc");
							if(jokeList==null || jokeList.isEmpty() || jokeList.size()<5){
								insertNewJoke("all");
								jokeList = apiService.getNextJoke(lastHistoryJokeId,0,5,"text","asc");
							}
							StringBuffer allContent = new StringBuffer("笑话订阅推送");
							log.info(""+compDate((Date)subMap.get("begin_time"),(Date)subMap.get("end_time")));
							if(compDate((Date)subMap.get("begin_time"),(Date)subMap.get("end_time"))==7){
								log.info("赠送的7天笑话订阅");
								allContent.append("【赠送】");
							}
							allContent.append("\n").append(subBeginTime).append("~").append(subEndTime);
							allContent.append("\n\uE032郑重提醒：由于微信限制，请至少保持两天签到一次，不然您会错过小微的推送哦\ue032\n");
							int j = 1;
							for(int i=1;i<jokeList.size()+1;i++){
								Map<String,Object> map = jokeList.get(i-1);
								String content = String.valueOf(map.get("content"));
								String tempContent = allContent.toString();
								if((tempContent+content).length()>580){
									if(i==1){
										continue;
									}else{
										break;
									}
								}
								allContent.append(j+"、").append(content).append("\n\n");
								int jokeId = (Integer)map.get("id");
								Map<String,Object> insertMap = new HashMap<String, Object>();
								insertMap.put("user_no",userNo);
								insertMap.put("joke_id",jokeId);
								insertMap.put("create_time", new Date());
								apiService.insertMethod("joke_history",insertMap);
								j++;
							}
							allContent.append("<a href='"+weixinUrl+"/api/jokeListPage?userNo="+userNo+"'>更多乐趣,点我查看>></a>");
							String contentStr = allContent.toString();
							contentStr = contentStr.replace("<br />","");
							contentStr = contentStr.replace("\r","");
							Map<String,Object> kfParamsMap = new HashMap<String, Object>();
							kfParamsMap.put("accessToken",new WxAction(weixinService).getAccessToken());
							kfParamsMap.put("toUserOpenId",openid);
							kfParamsMap.put("content",contentStr);
							Thread thread = new Thread(new KFNotifyNotice("kefuMessage_text",kfParamsMap));
							thread.start();
						}
					}
				}
				apiService.updateSubCategory(""+subIndex,1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getBatch(){
		String nowTimeStr = String.valueOf(System.currentTimeMillis());
		return "joke"+nowTimeStr.substring(nowTimeStr.length()-4)+String.valueOf(Math.round(Math.random()*90000+10000));
	}

	/**
	 * 区域插入
	 */
	public void insertZipcodeArea(){
		try {
			String host = "http://jisuybcx.market.alicloudapi.com";
			String path = "/zipcode/area";
			String method = "GET";
			Map<String, String> headers = new HashMap<String, String>();
			//最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
			headers.put("Authorization", "APPCODE 5ef4a89682574ccabe8d5850d25b6305");
			Map<String, String> querys = new HashMap<String, String>();
			HttpResponse httpResponse =  HttpUtils.doGet(host, path, method, headers, querys);
			//获取response的body
			String responseBody = EntityUtils.toString(httpResponse.getEntity());
			log.info("responseBody====" + responseBody);
			JSONObject bodyJson = JSONObject.parseObject(responseBody);
			String status = String.valueOf(bodyJson.get("status"));
			String msg = String.valueOf(bodyJson.get("msg"));
			if("0".equals(status) && "ok".equals(msg)){
				JSONArray resultJsonArr = (JSONArray)bodyJson.get("result");
				System.out.println("resultJsonArr====" + resultJsonArr.toString());
				if(resultJsonArr!=null){
					for(Object obj:resultJsonArr){
						JSONObject resultJson = (JSONObject)obj;
						Map<String,Object> paramsMap = new HashMap<String,Object>();
						String areaid = String.valueOf(resultJson.get("areaid"));
						String name = String.valueOf(resultJson.get("name"));
						String parentid = String.valueOf(resultJson.get("parentid"));
						paramsMap.put("areaid",areaid);
						paramsMap.put("name",name);
						paramsMap.put("parentid",parentid);
						apiService.insertMethod("china_area", paramsMap);
					}

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	/**
	 * 支持彩票类型查询并插入数据库
	 * @throws Exception
	 */
	public void insertLotteryCategory() throws Exception {
		try {
			String host = "http://ali-lottery.showapi.com";
			String path = "/typesearch";
			String method = "GET";
			Map<String, String> headers = new HashMap<String, String>();
			//最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
			headers.put("Authorization", "APPCODE 5ef4a89682574ccabe8d5850d25b6305");
			Map<String, String> querys = new HashMap<String, String>();
			//HttpResponse httpResponse = HttpUtils.doGet(host, path, method, headers, querys);
			//获取response的body
			//String responseBody = EntityUtils.toString(httpResponse.getEntity());
			//log.info("responseBody===="+responseBody);
			String responseBody = "{\"showapi_res_code\":0,\"showapi_res_error\":\"\",\"showapi_res_body\":{\"result\":[{\"series\":\"\",\"area\":\"\",\"issuer\":\"体彩\",\"tdescr\":\"全国彩\",\"times\":1,\"tcode\":\"wide\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"dlt\",\"notes\":\"每周一、三、六的20:30开奖\",\"descr\":\"超级大乐透\"},{\"series\":\"\",\"area\":\"\",\"issuer\":\"福彩\",\"tdescr\":\"全国彩\",\"times\":1,\"tcode\":\"wide\",\"serdescr\":\"未定义\",\"hots\":\"true\",\"high\":\"false\",\"code\":\"fc3d\",\"notes\":\"每天的20:30开奖\",\"descr\":\"福彩3d\"},{\"series\":\"\",\"area\":\"\",\"issuer\":\"体彩\",\"tdescr\":\"全国彩\",\"times\":1,\"tcode\":\"wide\",\"serdescr\":\"未定义\",\"hots\":\"true\",\"high\":\"false\",\"code\":\"pl3\",\"notes\":\"每天的20:30开奖\",\"descr\":\"排列3\"},{\"series\":\"\",\"area\":\"\",\"issuer\":\"体彩\",\"tdescr\":\"全国彩\",\"times\":1,\"tcode\":\"wide\",\"serdescr\":\"未定义\",\"hots\":\"true\",\"high\":\"false\",\"code\":\"pl5\",\"notes\":\"每天的20:30开奖\",\"descr\":\"排列5\"},{\"series\":\"\",\"area\":\"\",\"issuer\":\"福彩\",\"tdescr\":\"全国彩\",\"times\":1,\"tcode\":\"wide\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"qlc\",\"notes\":\"每周一、三、五的21:15开奖\",\"descr\":\"七乐彩\"},{\"series\":\"\",\"area\":\"\",\"issuer\":\"体彩\",\"tdescr\":\"全国彩\",\"times\":1,\"tcode\":\"wide\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"qxc\",\"notes\":\"每周二、五、日的20:30开奖\",\"descr\":\"七星彩\"},{\"series\":\"\",\"area\":\"\",\"issuer\":\"福彩\",\"tdescr\":\"全国彩\",\"times\":1,\"tcode\":\"wide\",\"serdescr\":\"未定义\",\"hots\":\"true\",\"high\":\"false\",\"code\":\"ssq\",\"notes\":\"每周二、四、日的21:15开奖\",\"descr\":\"双色球\"},{\"series\":\"soccer\",\"area\":\"\",\"issuer\":\"体彩\",\"tdescr\":\"全国彩\",\"times\":0,\"tcode\":\"wide\",\"serdescr\":\"足彩\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"zcbqc\",\"notes\":\"不定期开奖\",\"descr\":\"六场半全场\"},{\"series\":\"soccer\",\"area\":\"\",\"issuer\":\"体彩\",\"tdescr\":\"全国彩\",\"times\":0,\"tcode\":\"wide\",\"serdescr\":\"足彩\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"zcjqc\",\"notes\":\"不定期开奖\",\"descr\":\"四场进球彩\"},{\"series\":\"soccer\",\"area\":\"\",\"issuer\":\"体彩\",\"tdescr\":\"全国彩\",\"times\":0,\"tcode\":\"wide\",\"serdescr\":\"足彩\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"zcsfc\",\"notes\":\"不定期开奖\",\"descr\":\"十四场胜负彩(任9)\"},{\"series\":\"11x5\",\"area\":\"安徽\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":81,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"ah11x5\",\"notes\":\"每天81期，08:42起每10分钟一期\",\"descr\":\"11选5\"},{\"series\":\"11x5\",\"area\":\"北京\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":85,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"bj11x5\",\"notes\":\"每天85期，09:00起每10分钟一期\",\"descr\":\"11选5\"},{\"series\":\"11x5\",\"area\":\"福建\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":78,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"fj11x5\",\"notes\":\"每天78期，09:10起每10分钟一期\",\"descr\":\"11选5\"},{\"series\":\"11x5\",\"area\":\"广东\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":84,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"gd11x5\",\"notes\":\"每天84期，09:10起每10分钟一期\",\"descr\":\"11选5\"},{\"series\":\"11x5\",\"area\":\"甘肃\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":78,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"gs11x5\",\"notes\":\"每天78期，10:12起每10分钟一期\",\"descr\":\"11选5\"},{\"series\":\"11x5\",\"area\":\"广西\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":90,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"gx11x5\",\"notes\":\"每天90期，09:02起每10分钟一期\",\"descr\":\"11选5\"},{\"series\":\"11x5\",\"area\":\"贵州\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":80,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"gz11x5\",\"notes\":\"每天80期，09:00起每10分钟一期\",\"descr\":\"11选5\"},{\"series\":\"11x5\",\"area\":\"河北\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":85,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"heb11x5\",\"notes\":\"每天85期，08:30起每10分钟一期\",\"descr\":\"11选5\"},{\"series\":\"11x5\",\"area\":\"河南\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":72,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"hen11x5\",\"notes\":\"每天72期，10:08起每10分钟一期\",\"descr\":\"11选5\"},{\"series\":\"11x5\",\"area\":\"黑龙江\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":88,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"hlj11x5\",\"notes\":\"每天88期，08:05起每10分钟一期\",\"descr\":\"11选5(幸运)\"},{\"series\":\"11x5\",\"area\":\"湖北\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":81,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"hub11x5\",\"notes\":\"每天81期，08:35起每10分钟一期\",\"descr\":\"11选5\"},{\"series\":\"11x5\",\"area\":\"吉林\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":79,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"jl11x5\",\"notes\":\"每天79期，08:30起每10分钟一期\",\"descr\":\"11选5\"},{\"series\":\"11x5\",\"area\":\"江苏\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":82,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"js11x5\",\"notes\":\"每天82期，08:36起每10分钟一期\",\"descr\":\"11选5\"},{\"series\":\"11x5\",\"area\":\"江西\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":84,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"jx11x5\",\"notes\":\"每天84期，09:11起每10分钟一期\",\"descr\":\"11选5(多乐彩)\"},{\"series\":\"11x5\",\"area\":\"辽宁\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":83,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"ln11x5\",\"notes\":\"每天83期，08:50起每10分钟一期\",\"descr\":\"11选5\"},{\"series\":\"11x5\",\"area\":\"内蒙古\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":75,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"nmg11x5\",\"notes\":\"每天75期，09:47起每10分钟一期\",\"descr\":\"11选5\"},{\"series\":\"11x5\",\"area\":\"山东\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":78,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"sd11x5\",\"notes\":\"每天78期，09:06起每10分钟一期\",\"descr\":\"11选5(十一运夺金)\"},{\"series\":\"11x5\",\"area\":\"上海\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":90,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"sh11x5\",\"notes\":\"每天90期，09:00起每10分钟一期\",\"descr\":\"11选5\"},{\"series\":\"11x5\",\"area\":\"陕西\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":79,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"sxl11x5\",\"notes\":\"每天79期，09:00起每10分钟一期\",\"descr\":\"11选5\"},{\"series\":\"11x5\",\"area\":\"山西\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":94,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"sxr11x5\",\"notes\":\"每天94期，08:25起每10分钟一期\",\"descr\":\"11选5\"},{\"series\":\"11x5\",\"area\":\"天津\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":90,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"tj11x5\",\"notes\":\"每天90期，09:00起每10分钟一期\",\"descr\":\"11选5\"},{\"series\":\"11x5\",\"area\":\"新疆\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":97,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"xj11x5\",\"notes\":\"每天97期，10:00起每10分钟一期\",\"descr\":\"11选5\"},{\"series\":\"11x5\",\"area\":\"云南\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":85,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"yn11x5\",\"notes\":\"每天85期，09:00起每10分钟一期\",\"descr\":\"11选5\"},{\"series\":\"11x5\",\"area\":\"浙江\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":85,\"tcode\":\"freq\",\"serdescr\":\"11选5\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"zj11x5\",\"notes\":\"每天85期，08:50起每10分钟一期\",\"descr\":\"11选5\"},{\"series\":\"k3\",\"area\":\"安徽\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":80,\"tcode\":\"freq\",\"serdescr\":\"快3\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"ahk3\",\"notes\":\"每天80期，08:50起每10分钟一期\",\"descr\":\"快三\"},{\"series\":\"k3\",\"area\":\"北京\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":89,\"tcode\":\"freq\",\"serdescr\":\"快3\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"bjk3\",\"notes\":\"每天89期，09:10起每10分钟一期\",\"descr\":\"快三\"},{\"series\":\"k3\",\"area\":\"福建\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":78,\"tcode\":\"freq\",\"serdescr\":\"快3\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"fjk3\",\"notes\":\"每天78期，09:11起每10分钟一期\",\"descr\":\"快三\"},{\"series\":\"k3\",\"area\":\"甘肃\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":72,\"tcode\":\"freq\",\"serdescr\":\"快3\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"gsk3\",\"notes\":\"每天72期，10:13起每10分钟一期\",\"descr\":\"快三\"},{\"series\":\"k3\",\"area\":\"广西\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":78,\"tcode\":\"freq\",\"serdescr\":\"快3\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"gxk3\",\"notes\":\"每天78期，09:37起每10分钟一期\",\"descr\":\"快三(好运)\"},{\"series\":\"k3\",\"area\":\"贵州\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":78,\"tcode\":\"freq\",\"serdescr\":\"快3\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"gzk3\",\"notes\":\"每天78期，09:11起每10分钟一期\",\"descr\":\"快三\"},{\"series\":\"k3\",\"area\":\"河北\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":81,\"tcode\":\"freq\",\"serdescr\":\"快3\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"hebk3\",\"notes\":\"每天81期，08:40起每10分钟一期\",\"descr\":\"快三\"},{\"series\":\"k3\",\"area\":\"湖北\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":78,\"tcode\":\"freq\",\"serdescr\":\"快3\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"hubk3\",\"notes\":\"每天78期，09:10起每10分钟一期\",\"descr\":\"快三\"},{\"series\":\"k3\",\"area\":\"湖南\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":78,\"tcode\":\"freq\",\"serdescr\":\"快3\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"hunk3\",\"notes\":\"每天78期，09:20起每10分钟一期\",\"descr\":\"快三\"},{\"series\":\"k3\",\"area\":\"吉林\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":79,\"tcode\":\"freq\",\"serdescr\":\"快3\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"jlk3\",\"notes\":\"每天79期，08:05起每10分钟一期\",\"descr\":\"快三\"},{\"series\":\"k3\",\"area\":\"江苏\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":82,\"tcode\":\"freq\",\"serdescr\":\"快3\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"jsk3\",\"notes\":\"每天82期，08:40起每10分钟一期\",\"descr\":\"快三\"},{\"series\":\"k3\",\"area\":\"江西\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":84,\"tcode\":\"freq\",\"serdescr\":\"快3\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"jxk3\",\"notes\":\"每天84期，09:00起每10分钟一期\",\"descr\":\"快三\"},{\"series\":\"k3\",\"area\":\"内蒙古\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":73,\"tcode\":\"freq\",\"serdescr\":\"快3\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"nmgk3\",\"notes\":\"每天73期，09:45起每10分钟一期\",\"descr\":\"快三\"},{\"series\":\"k3\",\"area\":\"上海\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":82,\"tcode\":\"freq\",\"serdescr\":\"快3\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"shk3\",\"notes\":\"每天82期，08:58起每10分钟一期\",\"descr\":\"快三\"},{\"series\":\"keno\",\"area\":\"北京\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":179,\"tcode\":\"freq\",\"serdescr\":\"快乐8\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"bjkl8\",\"notes\":\"每天179期，09:05起每5分钟一期\",\"descr\":\"快乐8\"},{\"series\":\"kl10\",\"area\":\"重庆\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":97,\"tcode\":\"freq\",\"serdescr\":\"快乐10\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"cqklsf\",\"notes\":\"每天97期，00:05起每10、10分钟一期\",\"descr\":\"快乐十分(幸运农场)\"},{\"series\":\"kl10\",\"area\":\"广东\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":84,\"tcode\":\"freq\",\"serdescr\":\"快乐10\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"gdklsf\",\"notes\":\"每天84期，09:12起每10分钟一期\",\"descr\":\"快乐十分\"},{\"series\":\"kl10\",\"area\":\"广西\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":50,\"tcode\":\"freq\",\"serdescr\":\"快乐10\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"gxklsf\",\"notes\":\"每天50期，09:12起每15分钟一期\",\"descr\":\"快乐十分\"},{\"series\":\"kl10\",\"area\":\"黑龙江\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":84,\"tcode\":\"freq\",\"serdescr\":\"快乐10\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"hljklsf\",\"notes\":\"每天84期，09:25起每10分钟一期\",\"descr\":\"快乐十分\"},{\"series\":\"kl10\",\"area\":\"湖南\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":84,\"tcode\":\"freq\",\"serdescr\":\"快乐10\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"hunklsf\",\"notes\":\"每天84期，09:10起每10分钟一期\",\"descr\":\"快乐十分(动物总动员)\"},{\"series\":\"kl10\",\"area\":\"陕西\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":65,\"tcode\":\"freq\",\"serdescr\":\"快乐10\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"sxlklsf\",\"notes\":\"每天65期，09:10起每10分钟一期\",\"descr\":\"快乐十分\"},{\"series\":\"kl10\",\"area\":\"山西\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":90,\"tcode\":\"freq\",\"serdescr\":\"快乐10\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"sxrklsf\",\"notes\":\"每天90期，09:05起每10分钟一期\",\"descr\":\"快乐十分\"},{\"series\":\"kl10\",\"area\":\"深圳\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":83,\"tcode\":\"freq\",\"serdescr\":\"快乐10\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"szklsf\",\"notes\":\"每天83期，09:10起每10分钟一期\",\"descr\":\"快乐十分\"},{\"series\":\"kl10\",\"area\":\"天津\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":84,\"tcode\":\"freq\",\"serdescr\":\"快乐10\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"tjklsf\",\"notes\":\"每天84期，09:05起每10分钟一期\",\"descr\":\"快乐十分\"},{\"series\":\"kl10\",\"area\":\"云南\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":72,\"tcode\":\"freq\",\"serdescr\":\"快乐10\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"ynklsf\",\"notes\":\"每天72期，09:47起每10分钟一期\",\"descr\":\"快乐十分\"},{\"series\":\"kl12\",\"area\":\"辽宁\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":80,\"tcode\":\"freq\",\"serdescr\":\"快乐12\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"lnkl12\",\"notes\":\"每天80期，09:13起每10分钟一期\",\"descr\":\"快乐十二\"},{\"series\":\"kl12\",\"area\":\"四川\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":78,\"tcode\":\"freq\",\"serdescr\":\"快乐12\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"sckl12\",\"notes\":\"每天78期，09:10起每10分钟一期\",\"descr\":\"快乐十二\"},{\"series\":\"kl12\",\"area\":\"浙江\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":80,\"tcode\":\"freq\",\"serdescr\":\"快乐12\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"zjkl12\",\"notes\":\"每天80期，09:11起每10分钟一期\",\"descr\":\"快乐十二\"},{\"series\":\"ssc\",\"area\":\"重庆\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":120,\"tcode\":\"freq\",\"serdescr\":\"时时彩\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"cqssc\",\"notes\":\"每天120期，00:05起每5、10、5分钟一期\",\"descr\":\"时时彩\"},{\"series\":\"ssc\",\"area\":\"黑龙江\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":84,\"tcode\":\"freq\",\"serdescr\":\"时时彩\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"hljssc\",\"notes\":\"每天84期，09:30起每10分钟一期\",\"descr\":\"时时彩\"},{\"series\":\"ssc\",\"area\":\"吉林\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":79,\"tcode\":\"freq\",\"serdescr\":\"时时彩\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"jlssc\",\"notes\":\"每天79期，08:10起每10分钟一期\",\"descr\":\"时时彩\"},{\"series\":\"ssc\",\"area\":\"内蒙古\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":73,\"tcode\":\"freq\",\"serdescr\":\"时时彩\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"nmgssc\",\"notes\":\"每天73期，09:45起每10分钟一期\",\"descr\":\"时时彩\"},{\"series\":\"ssc\",\"area\":\"天津\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":84,\"tcode\":\"freq\",\"serdescr\":\"时时彩\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"tjssc\",\"notes\":\"每天84期，09:10起每10分钟一期\",\"descr\":\"时时彩\"},{\"series\":\"ssc\",\"area\":\"新疆\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":96,\"tcode\":\"freq\",\"serdescr\":\"时时彩\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"xjssc\",\"notes\":\"每天96期，10:10起每10分钟一期\",\"descr\":\"时时彩\"},{\"series\":\"ssc\",\"area\":\"云南\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":72,\"tcode\":\"freq\",\"serdescr\":\"时时彩\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"ynssc\",\"notes\":\"每天72期，09:42起每10分钟一期\",\"descr\":\"时时彩\"},{\"series\":\"zone\",\"area\":\"北京\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":179,\"tcode\":\"freq\",\"serdescr\":\"地区彩\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"bjpk10\",\"notes\":\"每天179期，09:07起每5分钟一期\",\"descr\":\"pk拾\"},{\"series\":\"zone\",\"area\":\"重庆\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":85,\"tcode\":\"freq\",\"serdescr\":\"地区彩\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"cqbbwp\",\"notes\":\"每天85期，09:57起每10分钟一期\",\"descr\":\"百变王牌\"},{\"series\":\"zone\",\"area\":\"湖南\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":84,\"tcode\":\"freq\",\"serdescr\":\"地区彩\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"hunxysc\",\"notes\":\"每天84期，09:12起每10分钟一期\",\"descr\":\"幸运赛车\"},{\"series\":\"zone\",\"area\":\"山东\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":79,\"tcode\":\"freq\",\"serdescr\":\"地区彩\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"sdklpk3\",\"notes\":\"每天79期，09:01起每10分钟一期\",\"descr\":\"快乐扑克3\"},{\"series\":\"zone\",\"area\":\"山东\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":78,\"tcode\":\"freq\",\"serdescr\":\"地区彩\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"sdqyh\",\"notes\":\"每天78期，09:10起每10分钟一期\",\"descr\":\"群英会\"},{\"series\":\"zone\",\"area\":\"上海\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":23,\"tcode\":\"freq\",\"serdescr\":\"地区彩\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"shssl\",\"notes\":\"每天23期，10:30起每30分钟一期\",\"descr\":\"时时乐\"},{\"series\":\"zone\",\"area\":\"山西\",\"issuer\":\"体彩\",\"tdescr\":\"高频彩\",\"times\":85,\"tcode\":\"freq\",\"serdescr\":\"地区彩\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"sxrytdj\",\"notes\":\"每天85期，08:32起每10分钟一期\",\"descr\":\"泳坛夺金\"},{\"series\":\"zone\",\"area\":\"新疆\",\"issuer\":\"福彩\",\"tdescr\":\"高频彩\",\"times\":16,\"tcode\":\"freq\",\"serdescr\":\"地区彩\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"xjxlc\",\"notes\":\"每天16期，11:00起每60分钟一期\",\"descr\":\"喜乐彩\"},{\"series\":\"\",\"area\":\"安徽\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"ahfc25x5\",\"notes\":\"每天的22:30开奖\",\"descr\":\"福彩25选5\"},{\"series\":\"\",\"area\":\"湖北\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"ctfc22x5\",\"notes\":\"每天的21:00开奖\",\"descr\":\"楚天风采22选5\"},{\"series\":\"\",\"area\":\"七省\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"df6j1\",\"notes\":\"每周一、三、六的21:00开奖\",\"descr\":\"东方6+1\"},{\"series\":\"\",\"area\":\"福建\",\"issuer\":\"体彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"fjtc22x5\",\"notes\":\"每天的20:00开奖\",\"descr\":\"体彩22选5\"},{\"series\":\"\",\"area\":\"福建\",\"issuer\":\"体彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"fjtc31x7\",\"notes\":\"每周一、三、五、日的20:00开奖\",\"descr\":\"体彩31选7\"},{\"series\":\"\",\"area\":\"福建\",\"issuer\":\"体彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"fjtc36x7\",\"notes\":\"每周二、四、六的19:25开奖\",\"descr\":\"体彩36选7\"},{\"series\":\"\",\"area\":\"广东\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"gdfc26x5\",\"notes\":\"每周二、四、日的20:40开奖\",\"descr\":\"南粤风采26选5\"},{\"series\":\"\",\"area\":\"广东\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"gdfc36x5\",\"notes\":\"每天的20:30开奖\",\"descr\":\"南粤风采36选7\"},{\"series\":\"\",\"area\":\"广东\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"gdfchc1\",\"notes\":\"每天的20:40开奖\",\"descr\":\"南粤风采好彩1\"},{\"series\":\"\",\"area\":\"广东\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"gdszfc\",\"notes\":\"每周二、五的20:55开奖\",\"descr\":\"深圳风采\"},{\"series\":\"\",\"area\":\"广西\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"gxklsc\",\"notes\":\"每天的21:45开奖\",\"descr\":\"快乐双彩\"},{\"series\":\"\",\"area\":\"七省\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"hd15x5\",\"notes\":\"每天的20:10开奖\",\"descr\":\"华东15选5\"},{\"series\":\"\",\"area\":\"黑龙江\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"hljfc22x5\",\"notes\":\"每天的19:25开奖\",\"descr\":\"龙江风彩22选5\"},{\"series\":\"\",\"area\":\"黑龙江\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"hljfc36x7\",\"notes\":\"每周一、三、六的20:15开奖\",\"descr\":\"龙江风彩36选7\"},{\"series\":\"\",\"area\":\"黑龙江\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"hljfcp62\",\"notes\":\"每天的19:35开奖\",\"descr\":\"福彩p62\"},{\"series\":\"\",\"area\":\"黑龙江\",\"issuer\":\"体彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"hljtc6j1\",\"notes\":\"每周二、五的23:40开奖\",\"descr\":\"体彩6+1\"},{\"series\":\"\",\"area\":\"江苏\",\"issuer\":\"体彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"jstc7ws\",\"notes\":\"每周二、四、五、日的23:20开奖\",\"descr\":\"体彩7位数\"},{\"series\":\"\",\"area\":\"辽宁\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"lnfc35x7\",\"notes\":\"每周二、四、日的20:20开奖\",\"descr\":\"辽宁风采35选7\"},{\"series\":\"\",\"area\":\"山东\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"qlfc23x5\",\"notes\":\"每天的21:00开奖\",\"descr\":\"齐鲁风采23选5\"},{\"series\":\"\",\"area\":\"上海\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"shttcx4\",\"notes\":\"每天的21:00开奖\",\"descr\":\"天天彩选4\"},{\"series\":\"\",\"area\":\"新疆\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"xjfc18x7\",\"notes\":\"每周一、五的23:40开奖\",\"descr\":\"新疆风采18选7\"},{\"series\":\"\",\"area\":\"新疆\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"xjfc25x7\",\"notes\":\"每周三、六的23:40开奖\",\"descr\":\"新疆风采25选7\"},{\"series\":\"\",\"area\":\"新疆\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"xjfc35x7\",\"notes\":\"每周一、五的23:40开奖\",\"descr\":\"新疆风采35选7\"},{\"series\":\"\",\"area\":\"云贵川\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"ygc25x5\",\"notes\":\"每天的20:50开奖\",\"descr\":\"25选5(天天乐)\"},{\"series\":\"\",\"area\":\"河北\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"yzfc20x5\",\"notes\":\"每天的20:00开奖\",\"descr\":\"燕赵风采20选5\"},{\"series\":\"\",\"area\":\"河北\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"yzfchyc2\",\"notes\":\"每天的20:00开奖\",\"descr\":\"燕赵风采好运彩2\"},{\"series\":\"\",\"area\":\"河北\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"yzfchyc3\",\"notes\":\"每天的20:00开奖\",\"descr\":\"燕赵风采好运彩3\"},{\"series\":\"\",\"area\":\"河北\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"yzfcpl5\",\"notes\":\"每周三、五、日的20:00开奖\",\"descr\":\"燕赵风采排列5\"},{\"series\":\"\",\"area\":\"河北\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"yzfcpl7\",\"notes\":\"每周一、三、五的23:30开奖\",\"descr\":\"燕赵风采排列7\"},{\"series\":\"\",\"area\":\"浙江\",\"issuer\":\"体彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"zjtc20x5\",\"notes\":\"每天的19:00开奖\",\"descr\":\"体彩20选5\"},{\"series\":\"\",\"area\":\"浙江\",\"issuer\":\"体彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"zjtc6j1\",\"notes\":\"每周二、五、日的19:20开奖\",\"descr\":\"体彩6+1\"},{\"series\":\"\",\"area\":\"河南\",\"issuer\":\"福彩\",\"tdescr\":\"低频彩\",\"times\":1,\"tcode\":\"area\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"false\",\"code\":\"zyfc22x5\",\"notes\":\"每天的23:30开奖\",\"descr\":\"中原风采22选5\"},{\"series\":\"\",\"area\":\"埃及\",\"issuer\":\"境外\",\"tdescr\":\"境外高频彩\",\"times\":1440,\"tcode\":\"outh\",\"serdescr\":\"未定义\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"ffc1\",\"notes\":\"每天1440期，00:00起每1分钟一期\",\"descr\":\"分分彩\"},{\"series\":\"\",\"area\":\"埃及\",\"issuer\":\"境外\",\"tdescr\":\"境外高频彩\",\"times\":720,\"tcode\":\"outh\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"ffc2\",\"notes\":\"每天720期，00:00起每2分钟一期\",\"descr\":\"二分彩\"},{\"series\":\"\",\"area\":\"埃及\",\"issuer\":\"境外\",\"tdescr\":\"境外高频彩\",\"times\":480,\"tcode\":\"outh\",\"serdescr\":\"未定义\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"ffc3\",\"notes\":\"每天480期，00:00起每3分钟一期\",\"descr\":\"三分彩\"},{\"series\":\"\",\"area\":\"埃及\",\"issuer\":\"境外\",\"tdescr\":\"境外高频彩\",\"times\":288,\"tcode\":\"outh\",\"serdescr\":\"未定义\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"ffc5\",\"notes\":\"每天288期，00:00起每5分钟一期\",\"descr\":\"五分彩\"},{\"series\":\"\",\"area\":\"马耳他\",\"issuer\":\"mla\",\"tdescr\":\"境外高频彩\",\"times\":180,\"tcode\":\"outh\",\"serdescr\":\"未定义\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"mlaft\",\"notes\":\"每天180期，13:09起每5分钟一期\",\"descr\":\"幸运飞艇\"},{\"series\":\"\",\"area\":\"台湾\",\"issuer\":\"境外\",\"tdescr\":\"境外高频彩\",\"times\":0,\"tcode\":\"outh\",\"serdescr\":\"未定义\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"twbingo\",\"notes\":\"不定期开奖\",\"descr\":\"宾果\"},{\"series\":\"keno\",\"area\":\"澳洲\",\"issuer\":\"境外\",\"tdescr\":\"境外高频彩\",\"times\":0,\"tcode\":\"outh\",\"serdescr\":\"快乐8\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"aukeno\",\"notes\":\"不定期开奖\",\"descr\":\"快乐彩(act)\"},{\"series\":\"keno\",\"area\":\"加拿大卑斯\",\"issuer\":\"境外\",\"tdescr\":\"境外高频彩\",\"times\":0,\"tcode\":\"outh\",\"serdescr\":\"快乐8\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"cakeno\",\"notes\":\"不定期开奖\",\"descr\":\"快乐8\"},{\"series\":\"keno\",\"area\":\"加拿大西部\",\"issuer\":\"境外\",\"tdescr\":\"境外高频彩\",\"times\":0,\"tcode\":\"outh\",\"serdescr\":\"快乐8\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"cwkeno\",\"notes\":\"不定期开奖\",\"descr\":\"快乐8\"},{\"series\":\"keno\",\"area\":\"日本\",\"issuer\":\"境外\",\"tdescr\":\"境外高频彩\",\"times\":920,\"tcode\":\"outh\",\"serdescr\":\"快乐8\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"jpkeno\",\"notes\":\"每天920期，00:01起每1分钟一期\",\"descr\":\"东京快乐彩\"},{\"series\":\"keno\",\"area\":\"韩国\",\"issuer\":\"境外\",\"tdescr\":\"境外高频彩\",\"times\":880,\"tcode\":\"outh\",\"serdescr\":\"快乐8\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"krkeno\",\"notes\":\"每天880期，00:00起每1、1分钟一期\",\"descr\":\"快乐彩(1.5分)\"},{\"series\":\"keno\",\"area\":\"韩国\",\"issuer\":\"境外\",\"tdescr\":\"境外高频彩\",\"times\":0,\"tcode\":\"outh\",\"serdescr\":\"快乐8\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"krkeno22\",\"notes\":\"不定期开奖\",\"descr\":\"快乐彩(官版)\"},{\"series\":\"keno\",\"area\":\"菲律宾\",\"issuer\":\"境外\",\"tdescr\":\"境外高频彩\",\"times\":0,\"tcode\":\"outh\",\"serdescr\":\"快乐8\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"phkeno\",\"notes\":\"不定期开奖\",\"descr\":\"快乐彩(1.5分)\"},{\"series\":\"keno\",\"area\":\"菲律宾\",\"issuer\":\"境外\",\"tdescr\":\"境外高频彩\",\"times\":0,\"tcode\":\"outh\",\"serdescr\":\"快乐8\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"phkeno2\",\"notes\":\"不定期开奖\",\"descr\":\"快乐彩(2分)\"},{\"series\":\"keno\",\"area\":\"菲律宾\",\"issuer\":\"境外\",\"tdescr\":\"境外高频彩\",\"times\":0,\"tcode\":\"outh\",\"serdescr\":\"快乐8\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"phkeno5\",\"notes\":\"不定期开奖\",\"descr\":\"快乐彩(5分)\"},{\"series\":\"keno\",\"area\":\"新加坡\",\"issuer\":\"境外\",\"tdescr\":\"境外高频彩\",\"times\":660,\"tcode\":\"outh\",\"serdescr\":\"快乐8\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"sgkeno\",\"notes\":\"每天660期，00:00起每2、2分钟一期\",\"descr\":\"快乐彩\"},{\"series\":\"ssc\",\"area\":\"印尼\",\"issuer\":\"境外\",\"tdescr\":\"境外高频彩\",\"times\":288,\"tcode\":\"outh\",\"serdescr\":\"时时彩\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"inffc5\",\"notes\":\"每天288期，00:05起每5分钟一期\",\"descr\":\"时时彩(五分彩)\"},{\"series\":\"ssc\",\"area\":\"泰国\",\"issuer\":\"境外\",\"tdescr\":\"境外高频彩\",\"times\":288,\"tcode\":\"outh\",\"serdescr\":\"时时彩\",\"hots\":\"false\",\"high\":\"true\",\"code\":\"thffc5\",\"notes\":\"每天288期，00:05起每5分钟一期\",\"descr\":\"时时彩(五分彩)\"},{\"series\":\"ssc\",\"area\":\"河内\",\"issuer\":\"境外\",\"tdescr\":\"境外高频彩\",\"times\":288,\"tcode\":\"outh\",\"serdescr\":\"时时彩\",\"hots\":\"true\",\"high\":\"true\",\"code\":\"viffc5\",\"notes\":\"每天288期，00:05起每5分钟一期\",\"descr\":\"时时彩(五分彩)\"},{\"series\":\"\",\"area\":\"香港\",\"issuer\":\"境外\",\"tdescr\":\"境外低频彩\",\"times\":0,\"tcode\":\"outl\",\"serdescr\":\"未定义\",\"hots\":\"true\",\"high\":\"false\",\"code\":\"hk6\",\"notes\":\"不定期开奖\",\"descr\":\"彩\"}],\"ret_code\":0}}";
			JSONObject bodyJson = JSONObject.parseObject(responseBody);
			JSONObject resultBodyJson = (JSONObject)bodyJson.get("showapi_res_body");
			JSONArray resultJsonArr = (JSONArray)resultBodyJson.get("result");
			System.out.println("resultJson="+resultJsonArr.toString());
			if(resultJsonArr!=null){
				for(Object obj:resultJsonArr){
					JSONObject resultJson = (JSONObject)obj;
					Map<String,Object> paramsMap = new HashMap<String,Object>();
					String name = String.valueOf(resultJson.get("descr"));
					String code = String.valueOf(resultJson.get("code"));
					String notes = String.valueOf(resultJson.get("notes"));
					String issuer = String.valueOf(resultJson.get("issuer"));
					String tdescr = String.valueOf(resultJson.get("tdescr"));
					String tcode = String.valueOf(resultJson.get("tcode"));
					String serdescr = String.valueOf(resultJson.get("serdescr"));
					String hots = String.valueOf(resultJson.get("hots"));
					String high = String.valueOf(resultJson.get("high"));
					paramsMap.put("lottery_code",code);
					paramsMap.put("lottery_name",name);
					paramsMap.put("notes",notes);
					paramsMap.put("issuer",issuer);
					paramsMap.put("tdescr",tdescr);
					paramsMap.put("tcode",tcode);
					paramsMap.put("serdescr",serdescr);
					paramsMap.put("hots",hots);
					paramsMap.put("high", high);
					apiService.insertMethod("lottery_category", paramsMap);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "testApi", method = RequestMethod.GET)
	public void testApi(final ModelMap model, @RequestParam Map<String, String> params,HttpServletResponse response) {
		log.info("---------testApi---------");
	}

	public static int compDate(Date fDate, Date oDate) {
		Calendar aCalendar = Calendar.getInstance();
		aCalendar.setTime(fDate);
		int day1 = aCalendar.get(Calendar.DAY_OF_YEAR);
		aCalendar.setTime(oDate);
		int day2 = aCalendar.get(Calendar.DAY_OF_YEAR);
		System.out.println("day1="+day1+",day2="+day2);
		return day2 - day1;
	}

}
