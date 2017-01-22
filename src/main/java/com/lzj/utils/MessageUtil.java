package com.lzj.utils;

import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.lzj.op.TextMessage;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class MessageUtil {
	
//	private static MessageUtil messageUtil = new MessageUtil();
//	private MessageUtil(){
//		
//	}
//	public static MessageUtil getMessageUtil(){
//		synchronized(messageUtil){};
//		return messageUtil;
//	}
	
	/**
	 * 移付宝的测试
	 */
//	public static final String appID = "wxae81716c5f585bbb";
//	public static final String appsecret = "250efc1e8a2a45f2e46fdf00c47743d5";
	//移付宝-会中彩的正式的
//	public static final String appID = "wx7a05819f400df149";
//	public static final String appsecret = "f416f2bf84dac006abd22fdcc621247b";
	//移付宝-会中彩的测试的
//	public static final String appID = "wxae81716c5f585bbb";
//	public static final String appsecret = "250efc1e8a2a45f2e46fdf00c47743d5";
	//移付宝的
//	public static final String appID = "wx61415d048d834387";
//	public static final String appsecret = "79a14118e0769cb348c44051a82e46cb";
	
	/**
	 * 请求/返回消息类型：文本
	 */
	public static final String RESP_MESSAGE_TYPE_TEXT = "text";
	public static final String REQ_MESSAGE_TYPE_TEXT = "text";
	/**
	 * 请求消息类型：推送
	 */
	public static final String REQ_MESSAGE_TYPE_EVENT = "event"; 
	/**
	 * 事件类型：subscribe(订阅)
	 */
	public static final String EVENT_TYPE_SUBSCRIBE = "subscribe";

	/**
	 * 事件类型：unsubscribe(取消订阅)
	 */
	public static final String EVENT_TYPE_UNSUBSCRIBE = "unsubscribe";

	/**
	 * 事件类型：CLICK(自定义菜单点击事件)
	 */
	public static final String EVENT_TYPE_CLICK = "CLICK";
	/**
	 * 图文消息
	 */
	public static final String RESP_MESSAGE_TYPE_NEWS = "news";
	/**
	 * 客服消息
	 */
	public static final String EVENT_TYPE_CUSTOMER = "transfer_customer_service";
	
	/**
	 * 解析微信发来的请求（XML）
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> parseXml(HttpServletRequest request) throws Exception {
		// 将解析结果存储在HashMap中
		Map<String, String> map = new HashMap<String, String>();

		// 从request中取得输入流
		InputStream inputStream = request.getInputStream();
		// 读取输入流
		SAXReader reader = new SAXReader();
		Document document = reader.read(inputStream);
		// 得到xml根元素
		Element root = document.getRootElement();
		// 得到根元素的所有子节点
		List<Element> elementList = root.elements();

		// 遍历所有子节点
		for (Element e : elementList) {
			map.put(e.getName(), e.getText());
		}
		System.out.println("map.toString: " + map.toString());	

		// 释放资源
		inputStream.close();
		//inputStream = null;

		return map;
	}

	/**
	 * 文本消息对象转换成xml
	 * 
	 * @param textMessage 文本消息对象
	 * @return xml
	 */
	public static String textMessageToXml(TextMessage textMessage) {
		xstream.alias("xml", textMessage.getClass());
		return xstream.toXML(textMessage);
	}
	/**
	 * Obj对象转换成xml
	 * 
	 * @param object 对象
	 * @return xml
	 */
	public static <T> String objToXml(T object) {
		xstream.alias("xml", object.getClass());
		return xstream.toXML(object);
	}
	/**
	 * 扩展xstream，使其支持CDATA块
	 * 
	 * @date 2013-05-19
	 */
	private static XStream xstream = new XStream(new XppDriver() {
		public HierarchicalStreamWriter createWriter(Writer out) {
			return new PrettyPrintWriter(out) {
				// 对所有xml节点的转换都增加CDATA标记
				boolean cdata = true;

				@SuppressWarnings("unchecked")
				public void startNode(String name, Class clazz) {
					if(name.contains("MessageItem")){
						name = "item";
					}
					super.startNode(name, clazz);
				}

				protected void writeText(QuickWriter writer, String text) {
					if (cdata) {
						writer.write("<![CDATA[");
						writer.write(text);
						writer.write("]]>");
					} else {
						writer.write(text);
					}
				}
			};
		}
	});
}
