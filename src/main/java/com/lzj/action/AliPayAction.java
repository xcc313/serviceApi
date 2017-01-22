package com.lzj.action;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayOpenPublicTemplateMessageIndustryModifyRequest;
import com.alipay.api.response.AlipayOpenPublicTemplateMessageIndustryModifyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Created by Administrator on 2016/12/27.
 */
@Controller
@RequestMapping(value = "/ali")
public class AliPayAction extends BaseController{
    private static final Logger log = LoggerFactory.getLogger(AliPayAction.class);

    @RequestMapping(value = "notify")
    public void notify(@RequestParam Map<String, String> params){
        log.info("支付宝异步通知,params="+params);
    }

    @RequestMapping(value = "redirect")
    public void redirect(@RequestParam Map<String, String> params){
        log.info("支付宝回调,params="+params);
    }

    @RequestMapping(value = "qrcodePay")
    public void qrcodePay(@RequestParam Map<String, String> params){
        log.info("支付宝扫码,params="+params);
        try {
            String APP_ID = "";
            String APP_PRIVATE_KEY = "";
            String ALIPAY_PUBLIC_KEY = "";
            //实例化客户端
            AlipayClient client = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",APP_ID,APP_PRIVATE_KEY,"json","GBK",ALIPAY_PUBLIC_KEY);
            //实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.open.public.template.message.industry.modify
            AlipayOpenPublicTemplateMessageIndustryModifyRequest request = new AlipayOpenPublicTemplateMessageIndustryModifyRequest();
            //SDK已经封装掉了公共参数，这里只需要传入业务参数
            //此次只是参数展示，未进行字符串转义，实际情况下请转义
            request.setBizContent("{\n" +
                    "                \"primary_industry_name\":\"IT科技/IT软件与服务\",\n" +
                    "                \"primary_industry_code\":\"10001/20102\",\n" +
                    "                \"secondary_industry_code\":\"10001/20102\",\n" +
                    "                \"secondary_industry_name\":\"IT科技/IT软件与服务\"\n" +
                    "        }");
            AlipayOpenPublicTemplateMessageIndustryModifyResponse response = client.execute(request);
            //调用成功，则处理业务逻辑
            if(response.isSuccess()){
                //.....
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
    }

}
