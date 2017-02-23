package com.lzj.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lzj.fc_pay.wofu.WoFuAction;
import com.lzj.fc_pay.wofu.WoFuConfig;
import com.lzj.service.ApiService;
import com.lzj.service.PayService;
import com.lzj.utils.DESPlus;
import com.lzj.utils.LZJUtil;
import com.lzj.utils.SignUtil;
import com.sun.org.apache.xpath.internal.operations.Bool;
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
import java.text.SimpleDateFormat;
import java.util.*;
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
    @Resource
    private WxAction wxAction;

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
            updateMap.put("extraction_fee","0");
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
                String AppID = payService.getParamValue("AppID");
                String weixinUrl = payService.getParamValue("weixinUrl");
                String url = weixinUrl+"/pay/toPay?userNo="+encryptUserNo;
                String now = String.valueOf(System.currentTimeMillis());
                Map<String,String> map = new HashMap<>();
                map.put("noncestr","Wm3WZYTPz0wzccnW");
                map.put("jsapi_ticket",wxAction.getJsapiTicket());
                map.put("timestamp",now);
                map.put("url",weixinUrl+"/pay/toMyPayCode?userNo="+encryptUserNo);
                String sign = String.valueOf(SignUtil.jsSign(map));
                model.addAttribute("appId", AppID);
                model.addAttribute("timestamp", now);
                model.addAttribute("noncestr", "Wm3WZYTPz0wzccnW");
                model.addAttribute("sign", sign);

                model.addAttribute("userNo", encryptUserNo);
                model.put("url",url);
                model.put("merchantName",String.valueOf(userMap.get("merchant_name")));
                model.put("headimgurl",String.valueOf(userMap.get("headimgurl")));
                model.put("parentNo",String.valueOf(userMap.get("parent_no")));
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

    @RequestMapping(value="bandParentNo")
    public void bandParentNo(@RequestParam Map<String,String> params,HttpServletResponse response){
        log.info("-------------bandParentNo绑定上级-------------params="+params);
        Map<String,Object> resultMap = new HashMap<String, Object>();
        try{
            String encryptUserNo = params.get("userNo");
            String parentUrl = params.get("parentUrl");
            if(isEmpty(encryptUserNo) || isEmpty(parentUrl)){
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
            String parentEncryptNo = parentUrl.substring(parentUrl.lastIndexOf("=")+1);
            String parentNo = decryptUserNo(parentEncryptNo);
            log.info("绑定上级,parentNo="+parentNo);
            if(userNo.equals(parentNo)){
                resultMap.put("success",false);
                resultMap.put("msg", "上级用户不能是自己");
                outJson(JSONObject.toJSONString(resultMap), response);
                return;
            }
            Map<String,Object> parentMap = payService.selectUserByUserNo(parentNo);
            if(parentMap==null || parentMap.isEmpty()){
                resultMap.put("success",false);
                resultMap.put("msg", "上级用户不存在");
                outJson(JSONObject.toJSONString(resultMap), response);
                return;
            }
            String parentStatus = String.valueOf(parentMap.get("status"));
            if(!"NORMAL".equals(parentStatus)){
                resultMap.put("success",false);
                resultMap.put("msg", "上级用户状态异常");
                outJson(JSONObject.toJSONString(resultMap), response);
                return;
            }
            Map<String,Object> updateWhereMap = new HashMap<>();
            updateWhereMap.put("user_no",userNo);
            Map<String,Object> updateMap = new HashMap<>();
            updateMap.put("parent_no",parentNo);
            updateMap.put("wx_trans_fee_rate","0.0047");
            int resultRow = apiService.updateMethod("user", updateMap, updateWhereMap);
            payService.insertOperationLog(userNo,"bandParent","user","绑定上级编号"+parentNo+",交易费率改为0.0047");
            if(resultRow==1){
                resultMap.put("success",true);
                resultMap.put("msg", "绑定成功,费率降为0.47%");
                outJson(JSONObject.toJSONString(resultMap), response);
                return;
            }else{
                resultMap.put("success",false);
                resultMap.put("msg", "绑定失败");
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
            log.info("bandParentNo绑定上级结果:" + JSONObject.toJSONString(resultMap));
        }
    }

    @RequestMapping(value="balanceBill")
    public String balanceBill(final ModelMap model, @RequestParam Map<String,String> params,HttpServletResponse response){
        log.info("-------------balanceBill到余额账单页面----------params="+params);
        String encryptUserNo = params.get("userNo");
        try {
            String userNo = decryptUserNo(encryptUserNo);
            Map<String,Object> whereMap = new HashMap<String, Object>();
            whereMap.put("user_no",userNo);
            Map<String,Object> userMap = apiService.getOneMethod("user", whereMap, "id", "desc", 0);
            if(userMap==null || userMap.isEmpty()){
                model.put("errorMsg","无此用户");
                model.put("errorCode","balanceBill"+encryptUserNo);
                return "errorPage";
            }
            List<Map<String,Object>> balanceHistoryList = apiService.getListMethod("balance_history", whereMap, "id", "desc", 10);
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
            for(Map<String,Object> tmpMap:balanceHistoryList){
                String createTime = sdf.format(tmpMap.get("create_time"));
                tmpMap.put("create_time",createTime);
                String method = String.valueOf(tmpMap.get("method"));
                if("IN".equals(method)){
                    method = "入账";
                }else if("OUT".equals(method)){
                    method = "出账";
                }
                tmpMap.put("method",method);
                //查询关联订单号
                String channel = String.valueOf(tmpMap.get("channel"));
                String channelId = String.valueOf(tmpMap.get("channel_id"));
                Map<String,Object> orderHsiatoryMap = payService.selectOrderByHistory(channel,channelId);
                if(orderHsiatoryMap!=null && !orderHsiatoryMap.isEmpty()){
                    String order_no = String.valueOf(orderHsiatoryMap.get("order_no"));
                    tmpMap.put("order_no",order_no);
                    tmpMap.put("trans_status",orderHsiatoryMap.get("trans_status"));
                }
            }
            model.put("balanceHistoryList",balanceHistoryList);
            model.put("userNo", encryptUserNo);
            return "user/balanceBill";

        } catch (Exception e) {
            e.printStackTrace();
            model.put("errorMsg","系统异常");
            model.put("errorCode", "balanceBill100000000");
            return "errorPage";
        }
    }

    @RequestMapping(value="childUser")
    public String childUser(final ModelMap model, @RequestParam Map<String,String> params,HttpServletResponse response){
        log.info("-------------childUser到我的下级页面----------params="+params);
        String encryptUserNo = params.get("userNo");
        try {
            String userNo = decryptUserNo(encryptUserNo);
            Map<String,Object> whereMap = new HashMap<String, Object>();
            whereMap.put("user_no",userNo);
            Map<String,Object> userMap = apiService.getOneMethod("user", whereMap, "id", "desc", 0);
            if(userMap==null || userMap.isEmpty()){
                model.put("errorMsg","无此用户");
                model.put("errorCode","childUser"+encryptUserNo);
                return "errorPage";
            }
            Map<String,Object> findChildWhereMap = new HashMap<String, Object>();
            findChildWhereMap.put("parent_no",userNo);
            List<Map<String,Object>> childUserList = apiService.getListMethod("user", findChildWhereMap, "id", "desc", 0);
            model.put("childUserList", childUserList);
            model.put("userNo", encryptUserNo);
            return "user/childUser";

        } catch (Exception e) {
            e.printStackTrace();
            model.put("errorMsg","系统异常");
            model.put("errorCode", "balanceBill100000000");
            return "errorPage";
        }
    }

    @RequestMapping(value="loadMoreBalanceBill")
    public void loadMoreBalanceBill( @RequestParam Map<String,String> params,HttpServletResponse response){
        log.info("-------------loadMoreBalanceBill加载更多余额账单----------params="+params);
        String encryptUserNo = params.get("userNo");
        List<Map<String,Object>> balanceHistoryList = new ArrayList<>();
        try {
            String index = params.get("index");
            if(StringUtils.isEmpty(index)){
                index = "0";
            }
            int startIndex = Integer.parseInt(index)*10;
            String userNo = decryptUserNo(encryptUserNo);
            Map<String,Object> whereMap = new HashMap<String, Object>();
            whereMap.put("user_no",userNo);
            Map<String,Object> userMap = apiService.getOneMethod("user", whereMap, "id", "desc", 0);
            if(userMap==null || userMap.isEmpty()){
                return;
            }
            balanceHistoryList = apiService.getListMethod("balance_history", whereMap, "id", "desc", startIndex,10);
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
            for(Map<String,Object> tmpMap:balanceHistoryList){
                String createTime = sdf.format(tmpMap.get("create_time"));
                tmpMap.put("create_time",createTime);
                String method = String.valueOf(tmpMap.get("method"));
                if("IN".equals(method)){
                    method = "入账";
                }else if("OUT".equals(method)){
                    method = "出账";
                }
                tmpMap.put("method",method);
                //查询关联订单号
                String channel = String.valueOf(tmpMap.get("channel"));
                String channelId = String.valueOf(tmpMap.get("channel_id"));
                Map<String,Object> orderHsiatoryMap = payService.selectOrderByHistory(channel,channelId);
                if(orderHsiatoryMap!=null && !orderHsiatoryMap.isEmpty()){
                    String order_no = String.valueOf(orderHsiatoryMap.get("order_no"));
                    tmpMap.put("order_no",order_no);
                    tmpMap.put("trans_status",orderHsiatoryMap.get("trans_status"));
                }
            }
            String json = JSON.toJSONString(balanceHistoryList);
            System.out.println("----loadMoreBalanceBill加载更多余额账单,json-----" + json);
            outJson(json, response);
            return ;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    @RequestMapping(value="toMerchantEdit")
    public String toMerchantEdit(final ModelMap model, @RequestParam Map<String,String> params,HttpServletResponse response){
        log.info("-------------toMerchantEdit到商户信息修改页面----------params="+params);
        String encryptUserNo = params.get("userNo");
        try {
            String userNo = decryptUserNo(encryptUserNo);
            Map<String,Object> whereMap = new HashMap<String, Object>();
            whereMap.put("user_no",userNo);
            Map<String,Object> userMap = apiService.getOneMethod("user", whereMap, "id", "desc", 0);
            if(userMap==null || userMap.isEmpty()){
                model.put("errorMsg","无此用户");
                model.put("errorCode","toMerchantEdit"+encryptUserNo);
                return "errorPage";
            }
            String idCard = String.valueOf(userMap.get("id_card_no"));
            String accountNo = String.valueOf(userMap.get("bank_no"));
            String mobileNo = String.valueOf(userMap.get("mobile_no"));
            String wx_trans_fee_rate = String.valueOf(userMap.get("wx_trans_fee_rate"));
            idCard = idCard.substring(0,6)+"******"+idCard.substring(idCard.length()-4);
            accountNo = accountNo.substring(0,6)+"******"+accountNo.substring(accountNo.length()-4);
            mobileNo = mobileNo.substring(0,3)+"****"+mobileNo.substring(mobileNo.length()-4);
            BigDecimal transFeeRate = new BigDecimal(wx_trans_fee_rate).multiply(new BigDecimal("100")).setScale(2);
            userMap.put("mobile_no",mobileNo);
            userMap.put("id_card_no",idCard);
            userMap.put("bank_no",accountNo);
            userMap.put("wx_trans_fee_rate",transFeeRate);
            model.put("userMap", userMap);
            model.put("encryptUserNo", encryptUserNo);
            return "user/merchantEdit";

        } catch (Exception e) {
            e.printStackTrace();
            model.put("errorMsg","系统异常");
            model.put("errorCode", "toMerchantEdit100000000");
            return "errorPage";
        }
    }

    @RequestMapping(value="merchantEdit")
    public void merchantEdit(@RequestParam Map<String,String> params,HttpServletResponse response){
        log.info("-------------merchantEdit商户信息修改-------------params="+params);
        Map<String,Object> resultMap = new HashMap<String, Object>();
        try{
            String encryptUserNo = params.get("userNo");
            String merchantName = params.get("merchantName");
            String bankcard = params.get("bankcard");
            if(isEmpty(encryptUserNo) || (isEmpty(merchantName) && isEmpty(bankcard))){
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
            Map<String,Object> updateMap = new HashMap<>();
            if(isNotEmpty(bankcard)){
                String idcard = String.valueOf(userMap.get("id_card_no"));
                String realname = String.valueOf(userMap.get("real_name"));
                String mobileNo = String.valueOf(userMap.get("mobile_no"));
                Map<String,Object> checkVerified = checkFourVerified(bankcard,idcard,realname,mobileNo);
                Boolean verifiedResult = (Boolean)checkVerified.get("resultBoolean");
                String verifiedMsg = String.valueOf(checkVerified.get("resultMsg"));
                if(!verifiedResult){
                    resultMap.put("success",false);
                    resultMap.put("msg", verifiedMsg);
                    outJson(JSONObject.toJSONString(resultMap), response);
                    return;
                }else{
                    updateMap.put("bank_no",bankcard);
                }
            }
            if(isNotEmpty(merchantName)){
                updateMap.put("merchant_name",merchantName);
            }
            Map<String,Object> updateWhereMap = new HashMap<>();
            updateWhereMap.put("user_no",userNo);
            int resultRow = apiService.updateMethod("user", updateMap, updateWhereMap);
            if(resultRow==1){
                String appId = payService.getParamValue("AppID");
                String weixinUrl = payService.getParamValue("weixinUrl");
                String redirect_uri = java.net.URLEncoder.encode(weixinUrl+"/wx/auth.do","UTF-8");
                String userInfoUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+appId+"&redirect_uri="+redirect_uri+"&response_type=code&scope=snsapi_base&state=userInfo&connect_redirect=1#wechat_redirect";
                resultMap.put("success",true);
                resultMap.put("msg", userInfoUrl);
                outJson(JSONObject.toJSONString(resultMap), response);
                return;
            }else{
                resultMap.put("success",false);
                resultMap.put("msg", "修改失败");
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
            log.info("merchantEdit商户信息修改结果:" + JSONObject.toJSONString(resultMap));
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
            Map<String,Object> compAmountResultMap = compAmountLimit("trade",userNo,new BigDecimal(amount));
            Boolean compAmountResultStatus = (Boolean)compAmountResultMap.get("resultStatus");
            String compAmountResultMsg = String.valueOf(compAmountResultMap.get("resultMsg"));
            if(!compAmountResultStatus){
                log.info("限额校验不通过");
                resultMap.put("success", false);
                resultMap.put("msg", compAmountResultMsg);
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

    @RequestMapping(value="purseCashCreateOrder")
    public void purseCashCreateOrder(@RequestParam Map<String,String> params,HttpServletResponse response){
        log.info("-------------purseCashCreateOrder代付下单-------------params=" + params);
        Map<String,Object> resultMap = new HashMap<String, Object>();
        try{
            String userNo = decryptUserNo(params.get("userNo"));
            if(StringUtils.isEmpty(userNo)){
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
            BigDecimal balance = new BigDecimal(String.valueOf(userMap.get("balance")));
            if(balance.compareTo(new BigDecimal("0"))!=1){
                resultMap.put("success",false);
                resultMap.put("msg", "无需提现");
                outJson(JSONObject.toJSONString(resultMap), response);
                return;
            }
            if (!isAmount(String.valueOf(balance))){
                resultMap.put("success",false);
                resultMap.put("msg", "金额非法");
                outJson(JSONObject.toJSONString(resultMap), response);
                return;
            }
            BigDecimal extractionFee = new BigDecimal(String.valueOf(userMap.get("extraction_fee")));
            if(balance.compareTo(extractionFee)!=1){
                resultMap.put("success",false);
                resultMap.put("msg", "提现手续费为"+String.valueOf(extractionFee)+"元/次，余额不足");
                outJson(JSONObject.toJSONString(resultMap), response);
                return;
            }
            BigDecimal extractionAmount = balance.subtract(extractionFee);
            String orderNo = String.valueOf(System.nanoTime());
            String accountName = String.valueOf(userMap.get("real_name"));
            String accountNo = String.valueOf(userMap.get("bank_no"));
            String remark = "速查服务";
            String transAmount = String.valueOf(extractionAmount);
            String callbackUrl = payService.getParamValue("pay_callback_url");
            Map<String,Object> extractionResultMap = payService.extraction(userNo, orderNo);
            log.info("插入代付订单结果extractionResultMap=" + extractionResultMap);
            Boolean resultBoo = (Boolean) extractionResultMap.get("success");
            String msg = String.valueOf(extractionResultMap.get("msg"));
            if(resultBoo){
                Map<String,Object> purseOrderResultMap = new WoFuAction().purseCashCreateOrder(orderNo, accountName, accountNo, remark, transAmount, null, callbackUrl, null, null);
                log.info("上游提现下单返回，purseOrderResultMap="+purseOrderResultMap);
                Map<String,String> headMap = (Map<String,String>)purseOrderResultMap.get("head");
                Map<String,String> contentMap = (Map<String,String>)purseOrderResultMap.get("content");
                String bizName = headMap.get("biz_name");
                String resultCode = headMap.get("result_code");
                if("transfer".equals(bizName)){
                    if("SEND".equals(resultCode)){
                        String order_no = contentMap.get("order_no");
                        if(orderNo.equals(order_no)){
                            payService.updatePurseOrder("1", "提交上游成功", "0", orderNo);
                            resultMap.put("success", true);
                            resultMap.put("msg", "提现成功");
                            outJson(JSONObject.toJSONString(resultMap), response);
                            return;
                        }
                    }else{
                        String resultMsg = headMap.get("result_msg");
                        payService.updatePurseOrder("2", resultMsg, null, orderNo);
                        payService.returnExtraction(orderNo,"提现提交上游失败");
                        resultMap.put("success", false);
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
            }else{
                resultMap.put("success",false);
                resultMap.put("msg", msg);
                outJson(JSONObject.toJSONString(resultMap), response);
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
            resultMap.put("success", false);
            resultMap.put("msg", "系统异常");
            outJson(JSONObject.toJSONString(resultMap), response);
            return;
        } finally {
            log.info("purseCashCreateOrder代付下单结果:" + JSONObject.toJSONString(resultMap));
        }
    }

    @RequestMapping(value="callbackWow")
    public void callbackWow(@RequestParam Map<String, String> params, HttpServletResponse response) {
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
                if("wxNative".equals(bizName) || "alipay".equals(bizName)){
                    log.info("支付回调,orderNo="+orderNo);
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
                        payService.recharge(orderNo);
                        //发送收款成功模板消息
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String appId = payService.getParamValue("AppID");
                        String weixinUrl = payService.getParamValue("weixinUrl");
                        String redirect_uri = java.net.URLEncoder.encode(weixinUrl+"/wx/auth.do","UTF-8");
                        String userInfoUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+appId+"&redirect_uri="+redirect_uri+"&response_type=code&scope=snsapi_base&state=userInfo&connect_redirect=1#wechat_redirect";
                        String transAmount = String.valueOf(orderMap.get("trans_amount"));
                        String userNo = String.valueOf(orderMap.get("user_no"));
                        Map<String,Object> userMap = payService.selectUserByUserNo(userNo);
                        String openid = String.valueOf(userMap.get("openid"));
                        String merchantName = String.valueOf(userMap.get("merchant_name"));
                        Map<String,String> paramsMap = new HashMap<>();
                        paramsMap.put("first","恭喜您有一笔收款到账");
                        paramsMap.put("keyword1",merchantName);
                        paramsMap.put("keyword2",transAmount);
                        paramsMap.put("keyword3",sdf.format(new Date()));
                        paramsMap.put("remark","点击“个人中心”--“余额”可进行提现");
                        paramsMap.put("descUrl",userInfoUrl);
                        wxAction.sendWXModelMsg(openid,"Oce0qp_QYtfpVy0o4kr-ZboGmtd1gmgdlR46Pyzqoac",paramsMap);
                        //上级分润
                        String parentNo = String.valueOf(userMap.get("parent_no"));
                        if(isNotEmpty(parentNo)){
                            Map<String,Object> parentMap = payService.selectUserByUserNo(parentNo);
                            BigDecimal parentProfitAmount = new BigDecimal(transAmount).multiply(new BigDecimal("0.001")).setScale(2,BigDecimal.ROUND_DOWN);
                            log.info("orderNo="+orderNo+",给上级"+parentNo+"分润"+String.valueOf(parentProfitAmount));
                            if(parentProfitAmount.compareTo(new BigDecimal("0.00"))==1){
                                Map<String,Object> profitResultMap = payService.parentProfit(parentNo, orderNo, parentProfitAmount);
                                payService.insertOperationLog(parentNo,"profit","user","订单orderNo="+orderNo+"分润结果："+profitResultMap);
                                log.info("分润结果extractionResultMap=" + profitResultMap);
                                Boolean resultBoo = (Boolean) profitResultMap.get("success");
                                if(resultBoo){
                                    Map<String,String> profitParamsMap = new HashMap<>();
                                    profitParamsMap.put("first","尊敬的用户：您有一笔分润资金已成功入账。");
                                    profitParamsMap.put("keyword1",String.valueOf(parentProfitAmount));
                                    profitParamsMap.put("keyword2",sdf.format(new Date()));
                                    profitParamsMap.put("remark","可通过“个人中心”--“余额账单”进行查看");
                                    profitParamsMap.put("descUrl",userInfoUrl);
                                    wxAction.sendWXModelMsg(String.valueOf(parentMap.get("openid")),"VEXUjzoYu2fhuIMPeAKaesH_t-HqmJODcI97TrKuOMk",profitParamsMap);
                                }
                            }
                        }
                    }else if("FAIL".equals(resultCode)){
                        payService.updatePayOrder("3",resultMsg,"2",orderNo);
                    }else{
                        payService.updatePayOrder("3",resultMsg,"3",orderNo);
                    }
                }else if("transfer".equals(bizName)){
                    log.info("提现回调,orderNo="+orderNo);
                    Map<String,Object> orderMap = payService.selectPurseOrder(orderNo);
                    if(orderMap==null || orderMap.isEmpty()){
                        log.info("无此订单,orderNo="+orderNo);
                        payService.insertOperationLog("WoFu", "callback", null, "无此订单,orderNo="+orderNo);
                        return;
                    }
                    if("3".equals(String.valueOf(orderMap.get("order_status"))) || !"0".equals(String.valueOf(orderMap.get("cash_status")))){
                        log.info("该订单上游已回调，或不是提现中状态,orderNo="+orderNo);
                        payService.insertOperationLog("WoFu", "callback", null, "该订单上游已回调，或不是提现中状态,orderNo="+orderNo);
                        return;
                    }
                    log.info("更新订单信息,orderNo="+orderNo);
                    if("SUCCESS".equals(resultCode)){
                        payService.updatePurseOrder("3", "提现成功", "1",new Date(), orderNo);
                    }else if("FAIL".equals(resultCode)){
                        payService.updatePurseOrder("3", resultMsg, "2", orderNo);
                        payService.returnExtraction(orderNo,"提现回调，提现失败");
                    }else{
                        payService.updatePurseOrder("3", resultMsg, "3", orderNo);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //outText("204", response);
            response.setStatus(204);
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
        resultMap.put("resultMsg", resultMsg);
        return resultMap;
    }

    //金额验证
    public static boolean isAmount(String str){
        Pattern pattern=Pattern.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$"); // 判断小数点后2位的数字的正则表达式
        Matcher match=pattern.matcher(str);
        return match.matches();
    }

    /**
     * 金额与限额比较
     * @param tradeType 限额类型 trade交易  extra提现
     * @param userNo 商户号
     * @param amount 金额
     * @return 比较结果  true表示在限额范围之内，false表示不在限额范围之内
     */
    public Map<String,Object> compAmountLimit(String tradeType,String userNo,BigDecimal amount){
        Map<String,Object> resultMap = new HashMap();
        resultMap.put("resultStatus",false);
        resultMap.put("resultMsg","额度超限");
        try{
            log.info("比较限额,{},{},{}",new Object[]{userNo,amount,tradeType});
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String today = sdf.format(new Date());
            String todayStart = today+" 00:00:00";
            String todayEnd = today+" 23:59:59";
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM");
            String nowMonth = sdf2.format(new Date());
            String monthStart = "${nowMonth}-01 00:00:00";
            String monthEnd = "${nowMonth}-31 23:59:59";

            if("trade".equals(tradeType)){
                Map<String,Object> userMap = payService.selectUserByUserNo(userNo);
                String ONE_FEE = String.valueOf(userMap.get("single_trade_max_amount")); //单笔交易限额  空为不限制
                String DAY_FEE = String.valueOf(userMap.get("day_trade_max_amount")); //单日交易限额  空为不限制

                if(isNotEmpty(ONE_FEE) && new BigDecimal(ONE_FEE).compareTo(amount)==-1){
                    log.info("大于单笔交易限额:" + ONE_FEE);
                    resultMap.put("resultStatus",false);
                    resultMap.put("resultMsg", "大于单笔交易限额:"+ONE_FEE);
                    return resultMap;
                }
                String HAVE_DAY_TRADE_AMOUNT = String.valueOf(payService.findOrderByUserNo(userNo, todayStart, todayEnd)); //单日已成功交易金额
                log.info("单日已成功交易金额:"+HAVE_DAY_TRADE_AMOUNT);
                if(isEmpty(HAVE_DAY_TRADE_AMOUNT)){
                    HAVE_DAY_TRADE_AMOUNT = "0.00";
                }
                if(isNotEmpty(DAY_FEE) && new BigDecimal(DAY_FEE).compareTo(amount.add(new BigDecimal(HAVE_DAY_TRADE_AMOUNT)))==-1){
                    log.info("大于单日交易限额:"+DAY_FEE+","+HAVE_DAY_TRADE_AMOUNT);
                    resultMap.put("resultStatus",false);
                    resultMap.put("resultMsg", "大于单日交易限额:" + DAY_FEE);
                    return resultMap;
                }
                log.info("交易限额校验通过");
                resultMap.put("resultStatus",true);
                resultMap.put("resultMsg", "");
                return resultMap;
            }else{
                return resultMap;
            }
        }catch(Exception e){
            e.printStackTrace();
            log.info(e.getMessage());
            resultMap.put("resultStatus",false);
            resultMap.put("resultMsg", "系统异常");
            return resultMap;
        }

    }

}
