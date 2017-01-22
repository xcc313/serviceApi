<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="user-scalable=no, width=device-width, initial-scale=1.0" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<script type="text/javascript" src="${ctx}/js/jquery-1.9.1.min.js"></script>
<script src="${ctx}/js/bootstrap.min.js"></script>
<link href="${ctx}/css/bootstrap.min.css" rel="stylesheet">
<title>微群二维码集合</title>
<script type="text/javascript">
var timeStamp="",packageStr="",paySign="",appId="",signType="",nonceStr="",qrCodeId="";
	$(function(){

	})


	
	function onBridgeReady(){
	   WeixinJSBridge.invoke(
	       'getBrandWCPayRequest', {
	           "appId" : appId,     //公众号名称，由商户传入     
	           "timeStamp":timeStamp,         //时间戳，自1970年以来的秒数     
	           "nonceStr" : nonceStr, //随机串     
	           "package" : packageStr,     
	           "signType" : signType,         //微信签名方式：     
	           "paySign" : paySign //微信签名 
	       },
	       function(res){
			   alert(res.err_desc);
	    	// 使用以上方式判断前端返回,微信团队郑重提示：res.err_msg将在用户支付成功后返回    ok，但并不保证它绝对可靠。
	           if(res.err_msg == "get_brand_wcpay_request:ok" ) {
	        	   alert("成功");
	           }else{
	        	   alert("失败");
	           }      
	       }
	   ); 
	}
		
	/*function toPay(){
		if (typeof WeixinJSBridge == "undefined"){
			alert("请通过微信支付");
		   if( document.addEventListener ){
		       document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
		   }else if (document.attachEvent){
		       document.attachEvent('WeixinJSBridgeReady', onBridgeReady); 
		       document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
		   }
		}else{
			var amount = $("#amount").val();
			$.ajax({
				url:'${ctx}/testAction/testWeixinPay.do',
				type:'POST',
				data:{'amount':amount},
				dataType:'json',
				success:function(data){
					if(jQuery.isEmptyObject(data)){
						alert("系统异常");
					}else{
						var params = eval(data);
						timeStamp = params.timeStamp;
						packageStr = params.package;
						paySign = params.paySign;
						appId = params.appId;
						signType = params.signType;
						nonceStr = params.nonceStr;
						onBridgeReady();
					}
				},
				error:function(){
					alert("网络异常");
				}
			})
		}
		
	}*/

function toPay(){
	/*if (typeof WeixinJSBridge == "undefined"){
		alert("请通过微信支付");
		if( document.addEventListener ){
			document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
		}else if (document.attachEvent){
			document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
			document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
		}
	}else{*/
		$("#noticeText").html("生成二维码中");
		var amount = $("#amount").val();
		$.ajax({
			url:'${ctx}/testAction/testWeixinPay.do',
			type:'POST',
			data:{'amount':amount},
			dataType:'json',
			success:function(data){
				if(jQuery.isEmptyObject(data)){
					alert("系统异常");
				}else{
					var params = eval(data);
					var code_url = params.code_url;
					var resultURL = params.resultURL;
					$("#noticeText").html("生成二维码成功，请扫描支付");
					$("#imgTmp").attr('src',"${ctx}/testAction/getQRCode.do?code_url="+code_url);
					$("#wapPay").attr('href',resultURL);
					//onBridgeReady();
				}
			},
			error:function(){
				alert("网络异常");
			}
		})
	/*}*/

}

	
</script>
</head>
<body>
<br/><br/><br/><br/><br/><br/>
	<div>
		输入支付金额:<input type="text" id="amount">
		<input type="button" value="生成二维码" onclick="toPay();">
	</div>
<br/>
<div><span id="noticeText">请输入金额后点击生成二维码</span></div>
<img id="imgTmp" src="" style="width:300px;height:300px;"/>
<br/><br/>
weixin://wap/pay?appid%3Dwx2421b1c4370ec43b%26noncestr%3DPszBO1tFkrfsI0fo%26package%3DWAP%26prepayid%3Dwx20160709123057cf591a5c8f0667613590%26timestamp%3D1468038657%26sign%3D2BDE67B963DE039E7731BC8C1A9A4BCF
<a href="weixin://wap/pay?appid%3Dwx2421b1c4370ec43b%26noncestr%3DPszBO1tFkrfsI0fo%26package%3DWAP%26prepayid%3Dwx20160709123057cf591a5c8f0667613590%26timestamp%3D1468038657%26sign%3D2BDE67B963DE039E7731BC8C1A9A4BCF" id="wapPay">点我支付</a>
<br/><br/><br/><br/><br/><br/>
<%--<div>
	<a href="weixin://wap/pay?appid%3Dwx2421b1c4370ec43b%26noncestr%3D7HCXMqJMGtszgbxr%26package%3DWAP%26prepayid%3Dwx201607081706341baae58c640604128830%26timestamp%3D1467968794%26sign%3DCDF149676EC648459FAFCA75F8350D49">23213</a>
</div>--%>

</body>
</html>