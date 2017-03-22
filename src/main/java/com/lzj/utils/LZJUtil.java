package com.lzj.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class LZJUtil {

	private static final String ACCEPT = "text/html,application/xhtml+xml,application/xml,image/png,image/*;q=0.8,*/*;q=0.5";
	private static final String ACCEPT_LANGUAGE = "zh-cn,zh;q=0.5";
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; rv:8.0) Gecko/20100101 Firefox/8.0";
	private static final String ACCEPT_CHARSET = "GB2312,utf-8;q=0.7,*;q=0.7";
	private static final Integer CONNECTION_TIMEOUT = 60000;
	private static final Integer READ_TIMEOUT = 60000;
	public static final String ENCODING_UTF8 = "UTF-8";
	
	/**
	 * json型字符串转map
	 * @param jsonStr
	 * @return
	 */
	public static Map<String, Object> jsonToMap(String jsonStr){
		Map<String, Object> map = new HashMap<String, Object>();  
        //最外层解析  
        JSONObject json = JSONObject.fromObject(jsonStr);  
        for(Object k : json.keySet()){  
            Object v = json.get(k);   
            //如果内层还是数组的话，继续解析  
            if(v instanceof JSONArray){  
                List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();  
                Iterator<JSONObject> it = ((JSONArray)v).iterator();  
                while(it.hasNext()){  
                    JSONObject json2 = it.next();  
                    list.add(jsonToMap(json2.toString()));  
                }  
                map.put(k.toString(), list);  
            } else {  
                map.put(k.toString(), v);  
            }  
        }  
        return map;
	}
	
	/**
	 * xml转map
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	public static Map<String, String> parseXml(String xml) throws Exception { 
		Map<String, String> map = new HashMap<String, String>(); 
		Document document = DocumentHelper.parseText(xml); 
		Element root = document.getRootElement(); 
		List<Element> elementList = root.elements(); 
		for (Element e : elementList){ 
			map.put(e.getName(), e.getText());
		} 
		return map;
	}

	/**
	 * 发送http的Get请求
	 * @param url
	 * @param encode
	 * @return
	 */
	public static String sendGet(String url, String encode){
		  System.out.println("url------>"+url);
		  HttpMethod method = null;
	      try {
	    	    method = new GetMethod(url);
	      	 	HttpClient httpclient=new HttpClient();  
	      	 	httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(60*1000);
				httpclient.getHttpConnectionManager().getParams().setSoTimeout(60 * 1000);
			    System.setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(60000));// （单位：毫秒）
			    System.setProperty("sun.net.client.defaultReadTimeout", String.valueOf(60000)); // （单位：毫秒）
			    httpclient.getParams().setIntParameter("http.socket.timeout", 60000);
		        //HttpMethod method = getMethod(url);  
//		        HttpClient httpClient = new HttpClient();  
//		        httpClient.getHostConfiguration().setHost(host, 8080, "http");          
//		        HttpMethod method = getMethod(url, param);  
				Header header=new Header("Content-type", "application/x-www-form-urlencoded; charset=" + encode);
				method.setRequestHeader(header);
				//httpclient.setConnectionTimeout(30*1000);
//				String sign = MD5.MD5Str(MD5.MD5Str("xc4PGNmkIfbQrU2PO5Eorr949pfqxidi").toLowerCase()).toLowerCase();
//				method.addRequestHeader("gwj-cid", "GWJ0000JFJF0022");
//				method.addRequestHeader("gwj-sign", sign);
				httpclient.executeMethod(method); 
				byte[] body=method.getResponseBody();
			    return new String(body,0,body.length,"UTF-8");
			    
			} catch (Exception e) {  
	      	 e.printStackTrace(); 
	      	  return "异步通知连接异常";
			} finally{  
	          //释放  
				method.releaseConnection();  
			}  
	  }

	public static Map<String, String> getDefHeaders() {
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", ACCEPT);
		headers.put("Accept-Language", ACCEPT_LANGUAGE);
		headers.put("User-Agent", USER_AGENT);
		headers.put("Accept-Charset", ACCEPT_CHARSET);
		return headers;
	}

	public static void buildConnection(HttpURLConnection conn, Map<String, String> headers) {
		conn.setConnectTimeout(CONNECTION_TIMEOUT);// 连接超时 单位毫秒
		conn.setReadTimeout(READ_TIMEOUT);// 读取超时 单位毫秒
		conn.setDoOutput(true);
		conn.setDoInput(true);
		Set<Map.Entry<String, String>> entries = headers.entrySet();
		for (Map.Entry<String, String> entry:entries) {
			String entryValue = String.valueOf(entry.getValue());
			conn.setRequestProperty(entry.getKey(), entryValue);
		}
		/*headers.each {
			conn.setRequestProperty(it.key, it.value);
		}*/
	}

	public static String doGet(String url) throws MalformedURLException, IOException,SocketTimeoutException {
		String encoding = ENCODING_UTF8;
		return doGet(url, encoding, getDefHeaders());
	}

	public static String doGet(String url, String encoding, Map<String, String> headers) throws MalformedURLException, IOException,SocketTimeoutException {
		System.out.println("doGet,url------>" + url);
		StringBuilder sb = new StringBuilder();
		HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
		Set<Map.Entry<String, String>> entries = getDefHeaders().entrySet();
		for (Map.Entry<String, String> entry:entries) {
			String entryValue = String.valueOf(entry.getValue());
			headers.put(entry.getKey(),entryValue);
		}
		buildConnection(con, headers);

		BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), encoding));
		String s = null;
		while ((s = br.readLine()) != null) {
			sb.append(s);
		}
		con.disconnect();
		return sb.toString();
	}

	  /**
	   * 发送http的post请求,参数为map
	   * @param url
	   * @param params
	   * @param encode
	   * @return
	   */
	  public static String sendPost(String url,Map<String,Object> params, String encode){
			
		  PostMethod postmethod = null;
	      try {
	    	postmethod = new PostMethod(url);
	      	HttpClient httpclient=new HttpClient();  
	      	//post请求  
	      	NameValuePair[] postData=new NameValuePair[params.size()];  
	      	int index=0;
	      	for(String key:params.keySet()){
	      		postData[index]=new NameValuePair(key,params.get(key)+"");  
	      		index++;
	      	}
				
				Header header=new Header("Content-type", "application/x-www-form-urlencoded; charset=" + encode);
				postmethod.setRequestHeader(header);
				postmethod.addParameters(postData);  
				httpclient.executeMethod(postmethod);  
			    
			    byte[] body=postmethod.getResponseBody();
			    
			    return new String(body,0,body.length,"UTF-8");
			    
			} catch (Exception e) {  
	      	 e.printStackTrace(); 
			} finally{  
	          //释放  
	          postmethod.releaseConnection();  
	      }  
		return null;
	}
	  
	  /**
		 * POST请求，字符串形式数据
		 * @param url 请求地址
		 * @param param 请求数据
		 * @param charset 编码方式
		 */
		public static String sendPostUrl(String url, String param, String charset) {

			PrintWriter out = null;
			BufferedReader in = null;
			String result = "";
			try {
				URL realUrl = new URL(url);
				// 打开和URL之间的连接
				URLConnection conn = realUrl.openConnection();
				// 设置通用的请求属性
				conn.setRequestProperty("accept", "*/*");
				conn.setRequestProperty("connection", "Keep-Alive");
				conn.setRequestProperty("user-agent",
						"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
				conn.setRequestProperty("Accept-Charset", "UTF-8");
				conn.setRequestProperty("contentType", "UTF-8");
				conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=UTF-8");
				// 发送POST请求必须设置如下两行
				conn.setDoOutput(true);
				conn.setDoInput(true);
				// 获取URLConnection对象对应的输出流
				out = new PrintWriter(conn.getOutputStream());
				// 发送请求参数
				System.out.println("------------param--------"+param);
				out.print(param);
				// flush输出流的缓冲
				out.flush();
				// 定义BufferedReader输入流来读取URL的响应
				in = new BufferedReader(new InputStreamReader(
						conn.getInputStream(), charset));
				String line;
				while ((line = in.readLine()) != null) {
					result += line;
				}
			} catch (Exception e) {
				System.out.println("发送 POST 请求出现异常！" + e);
				e.printStackTrace();
			}
			// 使用finally块来关闭输出流、输入流
			finally {
				try {
					if (out != null) {
						out.close();
					}
					if (in != null) {
						in.close();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			return result;
		}
	  
    public static <T> String getObjectToXml(T object) throws IOException{
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try
        {
                JAXBContext context = JAXBContext.newInstance(object.getClass());
                // 将对象转变为xml Object------XML
                // 指定对应的xml文件
                Marshaller marshaller = context.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);//是否格式化生成的xml串  
                marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);//是否省略xml头信息
                
                // 将对象转换为对应的XML文件
                marshaller.marshal(object, byteArrayOutputStream);
        }
        catch (JAXBException e)
        {
                
                e.printStackTrace();
        }
        //转化为字符串返回
        String xmlContent = new String(byteArrayOutputStream.toByteArray(), "UTF-8");
        return xmlContent;
    }


	/**
	 * 将字符串转成unicode
	 * @param str 待转字符串
	 * @return unicode字符串
	 */
	public static String stringToUnicode(String str) {
		str = (str == null ? "" : str);
		String tmp;
		StringBuffer sb = new StringBuffer(1000);
		char c;
		int i, j;
		sb.setLength(0);
		for (i = 0; i < str.length(); i++)
		{
			c = str.charAt(i);
			sb.append("\\u");
			j = (c >>>8); //取出高8位
			tmp = Integer.toHexString(j);
			if (tmp.length() == 1)
				sb.append("0");
			sb.append(tmp);
			j = (c & 0xFF); //取出低8位
			tmp = Integer.toHexString(j);
			if (tmp.length() == 1)
				sb.append("0");
			sb.append(tmp);

		}
		return (new String(sb));
	}

	/**
	 * 将unicode 字符串
	 * @param str 待转字符串
	 * @return 普通字符串
	 */
	public static String unicodeToString(String str) {
		str = (str == null ? "" : str);
		if (str.indexOf("\\u") == -1)//如果不是unicode码则原样返回
			return str;

		StringBuffer sb = new StringBuffer(1000);

		for (int i = 0; i < str.length() - 6;)
		{
			String strTemp = str.substring(i, i + 6);
			String value = strTemp.substring(2);
			int c = 0;
			for (int j = 0; j < value.length(); j++)
			{
				char tempChar = value.charAt(j);
				int t = 0;
				switch (tempChar)
				{
					case 'a':
						t = 10;
						break;
					case 'b':
						t = 11;
						break;
					case 'c':
						t = 12;
						break;
					case 'd':
						t = 13;
						break;
					case 'e':
						t = 14;
						break;
					case 'f':
						t = 15;
						break;
					default:
						t = tempChar - 48;
						break;
				}

				c += t * ((int) Math.pow(16, (value.length() - j - 1)));
			}
			sb.append((char) c);
			i = i + 6;
		}
		return sb.toString();
	}

	public static String genKey (int length) {
		if(length < 1) {
			return null;
		}
		StringBuffer buffer = new StringBuffer("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
		StringBuffer sb = new StringBuffer();
		Random r = new Random();
		int range = buffer.length();
		for(int i=0;i<length;i++){
			sb.append(buffer.charAt(r.nextInt(range)));
		}
		return  sb.toString();
	}

	public static void main(String[] args) {
		System.out.println(genKey(5));

	}

}
