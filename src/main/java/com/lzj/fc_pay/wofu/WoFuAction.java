package com.lzj.fc_pay.wofu;

import com.alibaba.fastjson.JSONObject;
import com.lzj.utils.DESPlus;
import com.lzj.utils.LZJUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lzj on 2017/2/14.
 */
public class WoFuAction {
    private static final Logger log = LoggerFactory.getLogger(WoFuAction.class);

    /**
     * 二维码支付下单
     * @param payType wxNative微信下单 alipay支付宝下单
     * @param orderNo     20位以内订单编号
     * @param body        商品名称，在支付的时候展示给付款人
     * @param transAmount 交易金额(元)
     * @param callbackUrl 回调地址，交易成功回调此地址
     * @return
     */
    public Map<String, Object> createOrder(String payType,String orderNo, String body, String transAmount, String callbackUrl,String merchantName,String merchantNo,Map<String,Object> cardInfoMap) throws  Exception{
        log.info("------------支付下单--------------payType="+payType);
        String jsonStr = "";
        if("wxNative".equals(payType)){
            jsonStr = "{\"bizName\":\"wxNative\",\"data\":{\"orderNo\":\""+orderNo+"\",\"body\":\""+body+"\",\"transAmount\":\""+transAmount+"\",\"callbackUrl\":\""+callbackUrl+"\",\"merchantName\":\""+merchantName+"\",\"merchantNo\":\""+merchantNo+"\"}}";
        }else if("alipay".equals(payType)){
            jsonStr = "{\"bizName\":\"alipay\",\"data\":{\"orderNo\":\""+orderNo+"\",\"body\":\""+body+"\",\"transAmount\":\""+transAmount+"\",\"callbackUrl\":\""+callbackUrl+"\"}}";
        }else if("fastPay".equals(payType)){
            String accountName = String.valueOf(cardInfoMap.get("accountName"));
            String accountNo = String.valueOf(cardInfoMap.get("accountNo"));
            String idCardNo = String.valueOf(cardInfoMap.get("idCardNo"));
            String mobileNo = String.valueOf(cardInfoMap.get("mobileNo"));
            String cvn2 = String.valueOf(cardInfoMap.get("cvn2"));
            String expDate = String.valueOf(cardInfoMap.get("expDate"));
            jsonStr = "{\"bizName\":\"fastPay\",\"data\":{\"orderNo\":\""+orderNo+"\",\"transAmount\":\""+transAmount+"\",\"accountName\":\""+accountName+"\",\"accountNo\":\""+accountNo+"\",\"idCardNo\":\""+idCardNo+"\",\"mobileNo\":\""+mobileNo+"\",\"cvn2\":\""+cvn2+"\",\"expDate\":\""+expDate+"\",\"callbackUrl\":\""+callbackUrl+"\"}}";
        }else if("unipay".equals(payType)){
            String accountNo = String.valueOf(cardInfoMap.get("accountNo"));
            String successUrl = String.valueOf(cardInfoMap.get("successUrl"));
            callbackUrl = String.valueOf(cardInfoMap.get("callbackUrl"));
            jsonStr = "{\"bizName\":\"unipay\",\"data\":{\"orderNo\":\""+orderNo+"\",\"transAmount\":\""+transAmount+"\",\"accountNo\":\""+accountNo+"\",\"successUrl\":\""+successUrl+"\",\"callbackUrl\":\""+callbackUrl+"\"}}";
        }
        log.info("------------支付下单jsonStr--------------"+jsonStr);
        DESPlus des = new DESPlus(WoFuConfig.SECRET_KEY);
        String json = des.encrypt(jsonStr);
        String url = WoFuConfig.TRANS_URL+"?appKey="+WoFuConfig.APP_KEY+"&data="+json;
        //String req = LZJUtil.sendGet(url,"UTF-8");
        String req = "";
        try{
            req = LZJUtil.doGet(url);
        }catch(Exception e1){
            e1.printStackTrace();
            log.error(e1.getMessage());
            Map<String,Object> errorMap = new HashMap<>();
            Map<String,String> headMap = new HashMap<>();
            headMap.put("biz_name",payType);
            headMap.put("result_code","FAIL");
            headMap.put("result_msg","提交订单失败");
            errorMap.put("head",headMap);
            return errorMap;
        }
        log.info("req=" + req);
        String responseStr = des.decrypt(req);
        log.info("responseStr=" + responseStr);
        //{"head":{"biz_name":"wxNative","result_code":"SUCCESS","result_msg":""},"content":{"qrUrl":"weixin://wxpay/bizpayurl?pr=3FCQHYB"}}
        return LZJUtil.jsonToMap(responseStr);
    }

    /*public Map<String, Object> fastPayCreateOrder(String orderNo, String transAmount,String accountName,String accountNo,String idCardNo,String mobileNo,String cvn2,String expDate, String callbackUrl) throws Exception{
        log.info("------------快捷支付下单--------------");
        String jsonStr = "{\"bizName\":\"fastPay\",\"data\":{\"orderNo\":\""+orderNo+"\",\"transAmount\":\""+transAmount+"\",\"accountName\":\""+accountName+"\",\"accountNo\":\""+accountNo+"\",\"idCardNo\":\""+idCardNo+"\",\"mobileNo\":\""+mobileNo+"\",\"cvn2\":\""+cvn2+"\",\"expDate\":\""+expDate+"\",\"callbackUrl\":\""+callbackUrl+"\"}}";
        log.info("------------快捷支付下单jsonStr--------------"+jsonStr);
        DESPlus des = new DESPlus(WoFuConfig.SECRET_KEY);
        String json = des.encrypt(jsonStr);
        String url = WoFuConfig.TRANS_URL+"?appKey="+WoFuConfig.APP_KEY+"&data="+json;
        String req = LZJUtil.sendGet(url,"UTF-8");
        log.info("req=" + req);
        String responseStr = des.decrypt(req);
        log.info("responseStr=" + responseStr);
        return LZJUtil.jsonToMap(responseStr);
    }*/

    /**
     *  单笔代付下单（目前仅支持对私代付）
     * @param orderNo 必填 20位以内订单编号
     * @param accountName 必填 收款人户名
     * @param accountNo 必填 账号
     * @param remark 选填 备注信息，将要显示银行记录中
     * @param transAmount 必填 代付金额
     * @param bankFullName 选填 银行开户行*对公代付填写
     * @param callbackUrl 选填 回调地址，中间状态的交易将异步通知结果
     * @param accountType 选填 private对私 public对公 不填默认对私
     * @param cnaps 选填 联行号*对公代付填写
     * @return
     */
    public Map<String, Object> purseCashCreateOrder(String orderNo, String accountName, String accountNo, String remark, String transAmount, String bankFullName, String callbackUrl, String accountType, String cnaps) throws Exception{
        log.info("------------代付下单--------------");
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("bizName", "transfer");
        Map<String,String> innerMap = new HashMap<>();
        innerMap.put("orderNo",orderNo);
        innerMap.put("accountName", accountName);
        innerMap.put("accountNo",accountNo);
        innerMap.put("remark",remark);
        innerMap.put("transAmount", transAmount);
        innerMap.put("callbackUrl", callbackUrl);
        paramsMap.put("data", innerMap);
        String jsonStr = JSONObject.toJSONString(paramsMap);
        log.info("------------代付下单jsonStr--------------"+jsonStr);
        DESPlus des = new DESPlus(WoFuConfig.SECRET_KEY);
        String json = des.encrypt(jsonStr);
        String url = WoFuConfig.TRANS_URL+"?appKey="+WoFuConfig.APP_KEY+"&data="+json;
        //String req = LZJUtil.sendGet(url,"UTF-8");
        String req = LZJUtil.doGet(url);
        log.info("req=" + req);
        String responseStr = des.decrypt(req);
        log.info("responseStr=" + responseStr);
        return LZJUtil.jsonToMap(responseStr);
    }

    public static void main(String[] args) {
        try {
            Map<String,Object> resultMap = new WoFuAction().createOrder("wxNative","100005","路遥里科技收款","0.02","","路遥里科技","10001231",null);
            System.out.println(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
