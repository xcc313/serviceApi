package com.lzj.action;

import com.lzj.utils.LZJUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 异步线程
 * Created by lzj on 2016/8/27.
 */
public class KFNotifyNotice implements Runnable {
    private static final String KF_MESSAGE_URL = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=";//url
    private String notifyType;//异步线程类型  kefuMessage_tuwen(客服图文消息)
    private Map<String,Object> paramsMap;//参数map
    private static final Logger log = LoggerFactory.getLogger(KFNotifyNotice.class);

    public KFNotifyNotice(String notifyType, Map<String, Object> paramsMap){
        this.notifyType = notifyType;
        this.paramsMap = paramsMap;
    }

    public void run() {
        log.info("异步线程,url:{},messageTyle:{},paramsMap:{}",new Object[]{KF_MESSAGE_URL,notifyType,paramsMap});
        if("kefuMessage_tuwen".equals(notifyType)){
            Thread thread = Thread.currentThread();
            try {
                thread.sleep(500);//暂停0.5秒后程序继续执行
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String accessToken = String.valueOf(paramsMap.get("accessToken"));
            String toUserOpenId = String.valueOf(paramsMap.get("toUserOpenId"));
            String media_id = String.valueOf(paramsMap.get("media_id"));
            String url = KF_MESSAGE_URL+accessToken;
            String msgString="{ \"touser\":\""+toUserOpenId+"\", \"msgtype\":\"mpnews\", \"mpnews\":{ \"media_id\":\""+media_id+"\" } }";
            String kfSendTuWenMsgResult = LZJUtil.sendPostUrl(url, msgString, "utf-8");
            log.info("--------kefuMessage_tuwen:"+toUserOpenId+"-------Result-------"+kfSendTuWenMsgResult);
        }else if("kefuMessage_image".equals(notifyType)){
            Thread thread = Thread.currentThread();
            try {
                thread.sleep(500);//暂停0.5秒后程序继续执行
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String accessToken = String.valueOf(paramsMap.get("accessToken"));
            String toUserOpenId = String.valueOf(paramsMap.get("toUserOpenId"));
            String media_id = String.valueOf(paramsMap.get("media_id"));
            String url = KF_MESSAGE_URL+accessToken;
            String msgString="{ \"touser\":\""+toUserOpenId+"\", \"msgtype\":\"image\", \"image\":{ \"media_id\":\""+media_id+"\" } }";
            String kfSendTuWenMsgResult = LZJUtil.sendPostUrl(url, msgString, "utf-8");
            log.info("--------kefuMessage_image:"+toUserOpenId+"-------Result-------"+kfSendTuWenMsgResult);
        }else if("kefuMessage_text".equals(notifyType)){
            Thread thread = Thread.currentThread();
            try {
                thread.sleep(500);//暂停0.5秒后程序继续执行
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String accessToken = String.valueOf(paramsMap.get("accessToken"));
            String toUserOpenId = String.valueOf(paramsMap.get("toUserOpenId"));
            String content = String.valueOf(paramsMap.get("content"));
            String url = KF_MESSAGE_URL+accessToken;
            String msgString="{ \"touser\":\""+toUserOpenId+"\", \"msgtype\":\"text\", \"text\":{ \"content\":\""+content+"\" } }";
            String kfSendTuWenMsgResult = LZJUtil.sendPostUrl(url, msgString, "utf-8");
            log.info("--------kefuMessage_text:"+toUserOpenId+"-------Result-------"+kfSendTuWenMsgResult);
        }
    }
}
