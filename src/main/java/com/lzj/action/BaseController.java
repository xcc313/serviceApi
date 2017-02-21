package com.lzj.action;

import com.alibaba.fastjson.JSON;
import com.lzj.utils.DESPlus;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;



/**
 * 提供获取request,session,转换json等常用方法
 *
 * 
 */
public class BaseController {

	// header 常量定义
	private static final String DEFAULT_ENCODING = "UTF-8";
	private static final boolean DEFAULT_NOCACHE = true;
	// Content Type 常量定义
	public static final String TEXT_TYPE = "text/plain";
	public static final String JSON_TYPE = "application/json";
	public static final String XML_TYPE = "text/xml";
	public static final String HTML_TYPE = "text/html";
	public static final String JS_TYPE = "text/javascript";
	public static final String EXCEL_TYPE = "application/vnd.ms-excel";
	public static int PAGE_NUMERIC = 20;

	// 构造结果返回
	String buildJson(String errorCode, Object obj, String msg) {
		Map<String,Object> head = new HashMap<String,Object>();
		head.put("result_code",errorCode);
		head.put("result_msg",msg);
		Map<String,Object> result = new HashMap<String,Object>();
		result.put("head",head);
		result.put("content",obj);
		String json = JSON.toJSONString(result);
		return json;
	}


	/**
	 * 直接输出内容的简便函数.
	 */
	protected void render(final String contentType, final String content,
			final HttpServletResponse response) {
		HttpServletResponse resp = initResponseHeader(contentType, response);
		try {
			resp.getWriter().write(content);
			resp.getWriter().flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 直接输出文本.
	 */
	protected void outText(final String text, final HttpServletResponse response) {
		render(TEXT_TYPE, text, response);
	}

	/**
	 * 直接输出HTML.
	 */
	protected void outHtml(final String html, final HttpServletResponse response) {
		render(HTML_TYPE, html, response);
	}

	/**
	 * 直接输出XML.
	 */
	protected void outXml(final String xml, final HttpServletResponse response) {
		render(XML_TYPE, xml, response);
	}

	/**
	 * 输出JSON,可以接收参数如： [{'name':'www'},{'name':'www'}],['a','b'],new
	 * String[]{'a','b'},合并如下：jsonString = "{TOTALCOUNT:" + totalCount + ",
	 * ROOT:" + jsonString + "}";
	 *
	 *            json字符串.
	 * 
	 */
	protected void outJson(final String json, final HttpServletResponse response) {
		render(JSON_TYPE, json, response);
	}

	/**
	 * 设置让浏览器弹出下载对话框的Header.
	 * 
	 * @param fileName
	 *            下载后的文件名.
	 */
	protected void setFileDownloadHeader(HttpServletResponse response,
			String fileName) {
		try {
			// 中文文件名支持
			String encodedfileName = new String(fileName.getBytes(),
					"ISO8859-1");
			response.setHeader("Content-Disposition", "attachment; filename=\""
					+ encodedfileName + "\"");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 分析并设置contentType与headers.
	 */
	protected HttpServletResponse initResponseHeader(final String contentType,
			final HttpServletResponse response) {
		// 分析headers参数
		String encoding = DEFAULT_ENCODING;
		boolean noCache = DEFAULT_NOCACHE;
		// 设置headers参数
		String fullContentType = contentType + ";charset=" + encoding;
		response.setContentType(fullContentType);
		if (noCache) {
			setNoCacheHeader(response);
		}

		return response;
	}

	/**
	 * 设置客户端无缓存Header.
	 */
	protected void setNoCacheHeader(HttpServletResponse response) {
		// Http 1.0 header
		response.setDateHeader("Expires", 0);
		response.addHeader("Pragma", "no-cache");
		// Http 1.1 header
		response.setHeader("Cache-Control", "no-cache");
	}
	
	/**
	 * 去除参数值中的前后空格
	 *  @param params
	 *  @author LJ
	 */
	protected void paramsTrim(Map<String, String> params) {
		 Set set = params.keySet();
		 for (Object key : set) {
			 String value = params.get(key);
			 value = (value==null) ? null : value.trim();
			 params.put(key.toString(), value);
		 }
	}

	/**
	 * 解密商户号
	 * @param secretUserNo
	 * @return
	 * @throws Exception
	 */
	public String decryptUserNo(String secretUserNo) throws Exception{
		return new DESPlus("luyaolilzjsuchafuwu").decrypt(secretUserNo);
	}

	/**
	 * 加密商户号
	 * @param userNo
	 * @return
	 * @throws Exception
	 */
	public String encryptUserNo(String userNo) throws Exception{
		return new DESPlus("luyaolilzjsuchafuwu").encrypt(userNo);
	}

	public boolean isEmpty(String str) {
		return str == null || str.length() == 0 || "null".equals(str);
	}

	public boolean isNotEmpty(String str){
		return !isEmpty(str);
	}
  
	
}
