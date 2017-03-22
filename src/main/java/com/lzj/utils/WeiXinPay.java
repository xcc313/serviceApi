package com.lzj.utils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lzj.op.WeiXinPayOrder;

public class WeiXinPay {
	private static final Logger log = LoggerFactory.getLogger(WeiXinPay.class);
	
	public static Map<String, String> addOrder(WeiXinPayOrder weixinPayOrder,String apikey){
		log.info("---------微信统一下单--------");
		try {
			Map<String, Object> weixinPayMap = getValueMap(weixinPayOrder);
			log.info("weixinPayMap="+weixinPayMap);
			String paramsList = paramsAdd(weixinPayMap);
			paramsList += "&key="+apikey;
			log.info("paramsList="+paramsList);
			String sign=MD5.MD5Str(paramsList).toUpperCase();
			weixinPayOrder.setSign(sign);
			String params = LZJUtil.getObjectToXml(weixinPayOrder);
			System.out.println("params="+params);
			String perOrderUrl = "https://api.mch.weixin.qq.com/pay/unifiedorder";
			String addOrderResult = LZJUtil.sendPostUrl(perOrderUrl, params, "UTF-8");
			log.info("-------addOrderResult-----"+addOrderResult);
			Map<String, String> map = LZJUtil.parseXml(addOrderResult);
			return map;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info("---------微信统一下单系统异常----------");
			return null;
		}
	}
	
	public static Map<String, Object> getValueMap(Object obj) {  
		  
        Map<String, Object> map = new HashMap<String, Object>();  
        // System.out.println(obj.getClass());  
        // 获取f对象对应类中的所有属性域  
        Field[] fields = obj.getClass().getDeclaredFields();  
        for (int i = 0, len = fields.length; i < len; i++) {  
            String varName = fields[i].getName();  
            try {  
                // 获取原来的访问控制权限  
                boolean accessFlag = fields[i].isAccessible();  
                // 修改访问控制权限  
                fields[i].setAccessible(true);  
                // 获取在对象f中属性fields[i]对应的对象中的变量  
                Object o = fields[i].get(obj);  
                if (o != null)  
                    map.put(varName, o.toString());  
                // System.out.println("传入的对象中包含一个如下的变量：" + varName + " = " + o);  
                // 恢复访问控制权限  
                fields[i].setAccessible(accessFlag);  
            } catch (IllegalArgumentException ex) {  
                ex.printStackTrace();  
            } catch (IllegalAccessException ex) {  
                ex.printStackTrace();  
            }  
        }  
        return map;  
  
    }
	
	/**
	 * 取出一个指定长度大小的随机正整数.
	 * 
	 * @param length
	 *            int 设定所取出随机数的长度。length小于11
	 * @return int 返回生成的随机数。
	 */
	public static int buildRandom(int length) {
		int num = 1;
		double random = Math.random();
		if (random < 0.1) {
			random = random + 0.1;
		}
		for (int i = 0; i < length; i++) {
			num = num * 10;
		}
		return (int) ((random * num));
	}
	
	/**
	 * 拼接
	 * @param map
	 * @return
	 */
    public static String paramsAdd(Map<String, Object> map){
    	List keys = new ArrayList(map.keySet());
    	Collections.sort(keys);
    	StringBuffer sb = new StringBuffer();
    	for(int i=0;i<keys.size();i++){
    		if(i!=0){
    			sb.append("&");
    		}
    		sb.append(keys.get(i)).append("=").append(map.get(keys.get(i)));
    	}
		return sb.toString();
    }

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
