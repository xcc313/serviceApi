package com.lzj.action;

import com.alibaba.fastjson.JSONObject;
import com.lzj.fc_pay.wofu.WoFuAction;
import com.lzj.fc_pay.wofu.WoFuConfig;
import com.lzj.service.ApiService;
import com.lzj.service.PayService;
import com.lzj.utils.DESPlus;
import com.lzj.utils.LZJUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lzj on 2017/2/16.
 */
@Controller
@RequestMapping(value = "/pay")
public class PayAction extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(PayAction.class);

    @Resource
    private PayService payService;
    @Resource
    private ApiService apiService;

    @RequestMapping(value="reg")
    public void reg(@RequestParam Map<String,String> params,HttpServletResponse response){
        log.info("-------------reg收款码注册-------------params="+params);
        Map<String,Object> resultMap = new HashMap<String, Object>();
        try{
            String encryptUserNo = params.get("userNo");
            String merchantName = params.get("merchantName");
            String bankcard = params.get("bankcard");
            String idcard = params.get("idcard");
            String realname = params.get("realname");
            String mobileNo = params.get("mobileNo");
            if(isEmpty(encryptUserNo) || isEmpty(merchantName) || isEmpty(bankcard) || isEmpty(idcard) || isEmpty(realname) || isEmpty(mobileNo)){
                resultMap.put("success",false);
                resultMap.put("msg", "必要信息为空");
                outJson(JSONObject.toJSONString(resultMap), response);
                return;
            }
            String userNo = decryptUserNo(encryptUserNo);
            Map<String,Object> userMap = payService.selectUserByUserNo(userNo);
            if(userMap==null || userMap.isEmpty()){
                resultMap.put("success",false);
                resultMap.put("msg", "用户不存在");
                outJson(JSONObject.toJSONString(resultMap), response);
                return;
            }
            String userStatus = String.valueOf(userMap.get("status"));
            if(!"NORMAL".equals(userStatus)){
                resultMap.put("success",false);
                resultMap.put("msg", "用户状态异常");
                outJson(JSONObject.toJSONString(resultMap), response);
                return;
            }
            Map<String,Object> checkVerified = checkFourVerified(bankcard,idcard,realname,mobileNo);
            Boolean verifiedResult = (Boolean)checkVerified.get("resultBoolean");
            String verifiedMsg = String.valueOf(checkVerified.get("resultMsg"));
            if(!verifiedResult){
                resultMap.put("success",false);
                resultMap.put("msg", verifiedMsg);
                outJson(JSONObject.toJSONString(resultMap), response);
                return;
            }
            Map<String,Object> updateWhereMap = new HashMap<>();
            updateWhereMap.put("user_no",userNo);
            Map<String,Object> updateMap = new HashMap<>();
            updateMap.put("mobile_no",mobileNo);
            updateMap.put("merchant_name",merchantName);
            updateMap.put("id_card_no",idcard);
            updateMap.put("bank_no",bankcard);
            updateMap.put("wx_trans_fee_rate","0.0049");
            updateMap.put("extraction_fee","1");
            updateMap.put("real_name",realname);
            int resultRow = apiService.updateMethod("user", updateMap, updateWhereMap);
            if(resultRow==1){
                resultMap.put("success",true);
                resultMap.put("msg", "注册成功");
                outJson(JSONObject.toJSONString(resultMap), response);
                return;
            }else{
                resultMap.put("success",false);
                resultMap.put("msg", "注册失败");
                outJson(JSONObject.toJSONString(resultMap), response);
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
            resultMap.put("success",false);
            resultMap.put("msg", "系统异常");
            outJson(JSONObject.toJSONString(resultMap), response);
            return;
        } finally {
            log.info("reg注册结果:" + JSONObject.toJSONString(resultMap));
        }
    }

    @RequestMapping(value="toMyPayCode")
    public String toMyPayCode(final ModelMap model, @RequestParam Map<String,String> params,HttpServletResponse response){
        log.info("-------------toMyPayCode到我的收款码页面----------params="+params);
        String encryptUserNo = params.get("userNo");
        try {
            String userNo = decryptUserNo(encryptUserNo);
            Map<String,Object> whereMap = new HashMap<String, Object>();
            whereMap.put("user_no",userNo);
            Map<String,Object> userMap = apiService.getOneMethod("user", whereMap, "id", "desc", 0);
            if(userMap==null || userMap.isEmpty()){
                model.put("errorMsg","无此用户");
                model.put("errorCode","toMyPayCode"+encryptUserNo);
                return "errorPage";
            }
            String mobileNo = String.valueOf(userMap.get("mobile_no"));
            String bankNo = String.valueOf(userMap.get("bank_no"));
            String idCardNo = String.valueOf(userMap.get("id_card_no"));
            if(isNotEmpty(mobileNo) && isNotEmpty(bankNo) && isNotEmpty(idCardNo)){
                String weixinUrl = payService.getParamValue("weixinUrl");
                String url = weixinUrl+"/pay/toPay?userNo="+encryptUserNo;
                model.put("url",url);
                model.put("merchantName",String.valueOf(userMap.get("merchant_name")));
                model.put("headimgurl",String.valueOf(userMap.get("headimgurl")));
                return "user/payQrCode";
            }else{
                model.put("userNo",encryptUserNo);
                return "user/reg";
            }

        } catch (Exception e) {
            e.printStackTrace();
            model.put("errorMsg","系统异常");
            model.put("errorCode", "toMyPayCode100000000");
            return "errorPage";
        }
    }

    @RequestMapping(value="toPay")
    public String toPay(final ModelMap model, @RequestParam Map<String,String> params,@RequestHeader("User-Agent") String userAgent){
        log.info("-------------toPay到付款页面----------params="+params+",userAgent="+userAgent);
        String encryptUserNo = params.get("userNo");
        try {
            String userNo = decryptUserNo(encryptUserNo);
            Map<String,Object> whereMap = new HashMap<String, Object>();
            whereMap.put("user_no",userNo);
            Map<String,Object> userMap = apiService.getOneMethod("user", whereMap, "id", "desc", 0);
            if(userMap==null || userMap.isEmpty()){
                model.put("errorMsg","无此用户");
                model.put("errorCode","toPay"+encryptUserNo);
                return "errorPage";
            }
            String userStatus = String.valueOf(userMap.get("status"));
            if("CLOSE".equals(userStatus)){
                model.put("errorMsg","该用户已注销");
                model.put("errorCode", "toPay" + encryptUserNo);
                return "errorPage";
            }
            if (userAgent.toLowerCase().indexOf("MicroMessenger".toLowerCase()) > -1) {
                log.info("------------微信扫码---------");
                model.put("payType", "wxNative");
            }else if(userAgent.toLowerCase().indexOf("AlipayClient".toLowerCase()) > -1) {
                log.info("----------支付宝扫码---------");
                model.put("payType", "alipay");
            }else{
                log.info("------------当做微信扫码---------");
                model.put("payType", "wxNative");
            }
            model.put("userNo",encryptUserNo);
            model.put("merchantName", String.valueOf(userMap.get("merchant_name")));
            model.put("headimgurl",String.valueOf(userMap.get("headimgurl")));
            return "user/pay";

        } catch (Exception e) {
            e.printStackTrace();
            model.put("errorMsg","系统异常");
            model.put("errorCode", "toPay100000000");
            return "errorPage";
        }
    }

    //到二维码支付页面
    @RequestMapping(value = "toQrcodePay")
    public String toQrcodePay(final ModelMap model, @RequestParam Map<String,String> params) {
        log.info("--------到二维码支付页面---------params="+params);
        String codeUrl = params.get("codeUrl");
        String amount = params.get("amount");
        String encryptUserNo = params.get("userNo");
        String payType = params.get("payType");
        try {
            String userNo = decryptUserNo(encryptUserNo);
            Map<String,Object> whereMap = new HashMap<String, Object>();
            whereMap.put("user_no",userNo);
            Map<String,Object> userMap = apiService.getOneMethod("user", whereMap, "id", "desc", 0);
            if(userMap==null || userMap.isEmpty()){
                model.put("errorMsg","无此用户");
                model.put("errorCode","toPay"+encryptUserNo);
                return "errorPage";
            }
            String userStatus = String.valueOf(userMap.get("status"));
            if("CLOSE".equals(userStatus)){
                model.put("errorMsg","该用户已注销");
                model.put("errorCode", "toPay" + encryptUserNo);
                return "errorPage";
            }
            model.put("payType",payType);
            model.put("codeUrl", codeUrl);
            model.put("amount", amount);
            model.put("merchantName", String.valueOf(userMap.get("merchant_name")));
            model.put("mobileNo", String.valueOf(userMap.get("mobile_no")));
            return "user/qrcodePay";
        }catch (Exception e){
            e.printStackTrace();
            model.put("errorMsg","系统异常");
            model.put("errorCode", "toPay100000000");
            return "errorPage";
        }

    }

    @RequestMapping(value="payCreateOrder")
    public void payCreateOrder(@RequestParam Map<String,String> params,HttpServletResponse response){
        log.info("-------------payCreateOrder支付下单-------------params="+params);
        Map<String,Object> resultMap = new HashMap<String, Object>();
        try{
            String userNo = decryptUserNo(params.get("userNo"));
            String amount = params.get("amount");
            String payType = params.get("payType");
            if(StringUtils.isEmpty(userNo) || StringUtils.isEmpty(amount) || StringUtils.isEmpty(payType)){
                resultMap.put("success",false);
                resultMap.put("msg", "必要信息为空");
                outJson(JSONObject.toJSONString(resultMap), response);
                return;
            }
            Map<String,Object> userMap = payService.selectUserByUserNo(userNo);
            if(userMap==null || userMap.isEmpty()){
                resultMap.put("success",false);
                resultMap.put("msg", "用户不存在");
                outJson(JSONObject.toJSONString(resultMap), response);
                return;
            }
            String userStatus = String.valueOf(userMap.get("status"));
            if(!"NORMAL".equals(userStatus)){
                resultMap.put("success",false);
                resultMap.put("msg", "用户状态异常");
                outJson(JSONObject.toJSONString(resultMap), response);
                return;
            }
            if(!isAmount(amount)){
                resultMap.put("success",false);
                resultMap.put("msg", "金额非法");
                outJson(JSONObject.toJSONString(resultMap), response);
                return;
            }
            String orderNo = String.valueOf(System.nanoTime());
            String body = String.valueOf(userMap.get("nickname"));

            Map<String,Object> orderMap = new HashMap<>();
            orderMap.put("openid",String.valueOf(userMap.get("openid")));
            orderMap.put("order_no",orderNo);
            orderMap.put("trans_amount",amount);
            BigDecimal feeRate = new BigDecimal("0.0049");
            if("wxNative".equals(payType)){
                orderMap.put("acq_name","WOFU_WX");
                feeRate = new BigDecimal(String.valueOf(userMap.get("wx_trans_fee_rate")));
            }else if("alipay".equals(payType)){
                //暂无支付宝交易费率设置
                orderMap.put("acq_name","WOFU_ZFB");
            }
            BigDecimal fee = (new BigDecimal(amount).multiply(feeRate)).setScale(2, BigDecimal.ROUND_UP);
            orderMap.put("trans_fee_rate",feeRate);
            orderMap.put("trans_fee",fee);
            BigDecimal acqFeeRate = new BigDecimal(WoFuConfig.CHENNEL_TRANS_FEE_RATE);
            BigDecimal acqFee = (new BigDecimal(amount).multiply(acqFeeRate)).setScale(2, BigDecimal.ROUND_UP);
            orderMap.put("acq_fee",acqFee);
            orderMap.put("body",body);
            orderMap.put("create_time",new Date());
            orderMap.put("order_status","0"); //订单状态 0初始化  1订单提交上游成功  2订单提交上游失败 3上游已回调
            orderMap.put("order_msg","初始化");
            orderMap.put("user_no",String.valueOf(userMap.get("user_no")));
            orderMap.put("parent_no", String.valueOf(userMap.get("parent_no")));
            log.info("支付写入订单，orderMap=" + orderMap);
            payService.insertMethod("pay_order",orderMap);
            String callbackUrl = payService.getParamValue("pay_callback_url");
            Map<String,Object> createOrderResultMap = new WoFuAction().createOrder(payType,orderNo,body,amount,callbackUrl);
            log.info("上游下单返回，createOrderResultMap="+createOrderResultMap);
            Map<String,String> headMap = (Map<String,String>)createOrderResultMap.get("head");
            Map<String,String> contentMap = (Map<String,String>)createOrderResultMap.get("content");
            String bizName = headMap.get("biz_name");
            String resultCode = headMap.get("result_code");
            if(payType.equals(bizName)){
                if("SUCCESS".equals(resultCode)){
                    payService.updatePayOrder("1","提交上游成功","0",orderNo);
                    String qrUrl = contentMap.get("qrUrl");
                    resultMap.put("success", true);
                    resultMap.put("msg", qrUrl);
                    outJson(JSONObject.toJSONString(resultMap), response);
                    return;
                }else{
                    String resultMsg = headMap.get("result_msg");
                    payService.updatePayOrder("2",resultMsg,null,orderNo);
                    resultMap.put("success",false);
                    resultMap.put("msg", resultMsg);
                    outJson(JSONObject.toJSONString(resultMap), response);
                    return;
                }
            }else{
                resultMap.put("success",false);
                resultMap.put("msg", "非允许业务类型,订单编号:"+orderNo);
                outJson(JSONObject.toJSONString(resultMap), response);
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
            resultMap.put("success",false);
            resultMap.put("msg", "系统异常");
            outJson(JSONObject.toJSONString(resultMap), response);
            return;
        } finally {
            log.info("payCreateOrder支付下单结果:" + JSONObject.toJSONString(resultMap));
        }
    }

    @RequestMapping(value="callbackWow")
    public void callbackWow(@RequestParam Map<String,String> params,HttpServletResponse response){
        log.info("---------callbackWow---------params=" + params);
        try {
            String dataStr = new DESPlus(WoFuConfig.SECRET_KEY).decrypt(params.get("data"));
            log.info("dataStr=" + dataStr);
            Map<String,Object> callbackMap = LZJUtil.jsonToMap(dataStr);
            payService.insertOperationLog("WoFu","callback",null,""+callbackMap);
            String headStr = String.valueOf(callbackMap.get("head"));
            String contentStr = String.valueOf(callbackMap.get("content"));
            Map<String,Object> headMap = LZJUtil.jsonToMap(headStr);
            if(headMap!=null && !headMap.isEmpty()){
                String bizName = String.valueOf(headMap.get("biz_name"));
                String resultCode = String.valueOf(headMap.get("result_code"));
                String resultMsg = String.valueOf(headMap.get("result_msg"));
                Map<String,Object> contentMap = LZJUtil.jsonToMap(contentStr);
                String orderNo = String.valueOf(contentMap.get("order_no"));
                Map<String,Object> orderMap = payService.selectPayOrder(orderNo);
                if(orderMap==null || orderMap.isEmpty()){
                    log.info("无此订单,orderNo="+orderNo);
                    payService.insertOperationLog("WoFu", "callback", null, "无此订单,orderNo="+orderNo);
                    return;
                }
                if("3".equals(String.valueOf(orderMap.get("order_status"))) || !"0".equals(String.valueOf(orderMap.get("trans_status")))){
                    log.info("该订单上游已回调，或不是交易中状态,orderNo="+orderNo);
                    payService.insertOperationLog("WoFu", "callback", null, "该订单上游已回调，或不是交易中状态,orderNo="+orderNo);
                    return;
                }
                log.info("更新订单信息,orderNo="+orderNo);
                if("SUCCESS".equals(resultCode)){
                    //payService.updatePayOrder("3","订单支付成功","1",orderNo);
                    payService.recharge(orderNo);
                }else if("FAIL".equals(resultCode)){
                    payService.updatePayOrder("3",resultMsg,"2",orderNo);
                }else{
                    payService.updatePayOrder("3",resultMsg,"3",orderNo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            outText("204", response);
        }

    }

    //四元素实名认证
    public Map<String,Object> checkFourVerified(String bankcard,String idcard,String realname,String mobileNo){
        Boolean resultBoo = true;
        String msg = "";
        Map<String,Object> resultMap = new HashMap<>();
        Boolean resultBoolean = true;
        String resultMsg = "";
        try {
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
                if(!isEmpty(bankcard) && !isEmpty(idcard) && !isEmpty(realname) && !isEmpty(mobileNo)){
                    String responseBody = null;
                    responseBody = SearchNewApiUtil.getNewVerifiedResultByYFB(bankcard, idcard, realname, mobileNo);
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
                    paramsMap.put("user_no","pay_reg");
                    apiService.insertMethod("verified_history", paramsMap);
                    apiService.addApiAccessNum("use_num", "verified");
                    verifiedHistoryMap = apiService.getOneMethod("verified_history", verifiedWhereMap,"id","desc",0);
                }
            }else{
                log.info("有此用户实名认证历史记录");
                String hisUserNo = String.valueOf(verifiedHistoryMap.get("user_no"));
                String hisId = String.valueOf(verifiedHistoryMap.get("id"));
                Map<String,Object> updateMap = new HashMap<String,Object>();
                updateMap.put("user_no", hisUserNo + ";pay_reg");
                Map<String, Object> whereMap = new HashMap<String,Object>();
                whereMap.put("id", hisId);
                apiService.updateMethod("verified_history", updateMap, whereMap);
                apiService.addApiAccessNum("use_num", "verified");
            }
            log.info("verifiedHistoryMap=====" + verifiedHistoryMap);
            String resultIsok = String.valueOf(verifiedHistoryMap.get("isok"));
            String resultCode = String.valueOf(verifiedHistoryMap.get("code"));
            if("1".equals(resultIsok) && "1".equals(resultCode)){
                resultBoolean = true;
                resultMsg = "校验一致";
            }else{
                resultBoolean = false;
                resultMsg = "银行卡实名认证校验不一致";
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultBoolean = false;
            resultMsg = "实名认证失败";
        }
        resultMap.put("resultBoolean",resultBoolean);
        resultMap.put("resultMsg",resultMsg);
        return resultMap;
    }

    //金额验证
    public static boolean isAmount(String str){
        Pattern pattern=Pattern.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$"); // 判断小数点后2位的数字的正则表达式
        Matcher match=pattern.matcher(str);
        return match.matches();
    }

}
