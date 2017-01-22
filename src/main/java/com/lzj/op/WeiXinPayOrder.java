package com.lzj.op;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="xml")
public class WeiXinPayOrder {
	private String appid;//公众账号ID(必须)
	private String mch_id;//商户号(必须)
	private String out_trade_no;//商户订单号(必须)
	private String body;//商品描述(必须)
	private int total_fee;//总金额(必须,订单总金额，单位为分)
	private String notify_url;//通知地址(必须)
	private String trade_type;//交易类型(必须,取值如下：JSAPI，NATIVE，APP)
	private String spbill_create_ip;//终端IP(必须,APP和网页支付提交用户端ip，Native支付填调用微信支付API的机器IP)
	private String nonce_str;//随机字符串(必须)
	private String sign;//签名(必须)
	
	private String device_info;//设备号
	private String detail;//商品详情
	private String attach;//附加数据
	private String fee_type;//货币类型
	private String time_start;//交易起始时间,订单生成时间，格式为yyyyMMddHHmmss
	private String time_expire;//交易结束时间,订单失效时间，格式为yyyyMMddHHmmss,注意：最短失效时间间隔必须大于5分钟
	private String goods_tag;//商品标记
	private String product_id;//商品ID,trade_type=NATIVE时此参数必传。此id为二维码中包含的商品ID，商户自行定义
	private String limit_pay;//指定支付方式，no_credit--指定不能使用信用卡支付
	private String openid;//用户标识，trade_type=JSAPI时此参数必传，用户在商户appid下的唯一标识
	
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public String getMch_id() {
		return mch_id;
	}
	public void setMch_id(String mch_id) {
		this.mch_id = mch_id;
	}
	public String getOut_trade_no() {
		return out_trade_no;
	}
	public void setOut_trade_no(String out_trade_no) {
		this.out_trade_no = out_trade_no;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public int getTotal_fee() {
		return total_fee;
	}
	public void setTotal_fee(int total_fee) {
		this.total_fee = total_fee;
	}
	public String getNotify_url() {
		return notify_url;
	}
	public void setNotify_url(String notify_url) {
		this.notify_url = notify_url;
	}
	public String getTrade_type() {
		return trade_type;
	}
	public void setTrade_type(String trade_type) {
		this.trade_type = trade_type;
	}
	public String getSpbill_create_ip() {
		return spbill_create_ip;
	}
	public void setSpbill_create_ip(String spbill_create_ip) {
		this.spbill_create_ip = spbill_create_ip;
	}
	public String getNonce_str() {
		return nonce_str;
	}
	public void setNonce_str(String nonce_str) {
		this.nonce_str = nonce_str;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public String getDevice_info() {
		return device_info;
	}
	public void setDevice_info(String device_info) {
		this.device_info = device_info;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public String getAttach() {
		return attach;
	}
	public void setAttach(String attach) {
		this.attach = attach;
	}
	public String getFee_type() {
		return fee_type;
	}
	public void setFee_type(String fee_type) {
		this.fee_type = fee_type;
	}
	public String getTime_start() {
		return time_start;
	}
	public void setTime_start(String time_start) {
		this.time_start = time_start;
	}
	public String getTime_expire() {
		return time_expire;
	}
	public void setTime_expire(String time_expire) {
		this.time_expire = time_expire;
	}
	public String getGoods_tag() {
		return goods_tag;
	}
	public void setGoods_tag(String goods_tag) {
		this.goods_tag = goods_tag;
	}
	public String getProduct_id() {
		return product_id;
	}
	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}
	public String getLimit_pay() {
		return limit_pay;
	}
	public void setLimit_pay(String limit_pay) {
		this.limit_pay = limit_pay;
	}
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
}
