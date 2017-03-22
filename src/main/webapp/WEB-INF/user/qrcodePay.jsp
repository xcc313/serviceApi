<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-cmn-Hans">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="user-scalable=no, width=device-width, initial-scale=1.0" />
    <c:set var="ctx" value="${pageContext.request.contextPath}" />
    <link href="${ctx}/css/weui.min.css" rel="stylesheet">
    <link href="${ctx}/css/jquery-weui.min.css" rel="stylesheet">
    <link href="${ctx}/css/demos.css" rel="stylesheet">
    <link href="${ctx}/css/pay.css" rel="stylesheet">
    <title>扫码支付</title>
    <script src="https://cdn.staticfile.org/jquery/1.9.1/jquery.min.js"></script>
    <script>window.jQuery || document.write('<script src="${ctx}/js/jquery-1.9.1.min.js">')</script>
    <script type="text/javascript" src="${ctx}/js/jquery-weui.min.js"></script>
    <script src="https://cdn.staticfile.org/jquery.qrcode/1.0/jquery.qrcode.min.js"></script>

    <script type="text/javascript">
        $(function () {
            $("#qrContainer").qrcode({width:200,height:200,correctLevel:0,text:'${codeUrl}'});
            var canvas=$("#qrContainer").find("canvas").get(0);
            $("#imgQr").attr("src",canvas.toDataURL("image/png"));
        })
    </script>

    <style>

        h4{
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
<div id="qrContainer" style="display: none;"></div>
<div style="margin-top: 30px;text-align: center;border-bottom: 1px dashed #666">
    <div style="font-family: 微软雅黑;">
        <p style="font-weight: bold;font-size: 22px;line-height: 35px;">${merchantName}</p>
        <p style="font-size: 15px;line-height: 25px;color: #666666;">商户电话：${mobileNo}</>
        <p style="font-size: 15px;line-height: 25px;color: #666666;">支付金额：￥${amount}元</p>
        <p class="page_desc" style="margin: 20px 0px;"><img id="imgQr" src="" alt=""></p>
    </div>
</div>
<div style="text-align: center;margin-top: 10px;font-family: 微软雅黑;font-size: 15px;">
    <c:if test="${scanCodeWay eq 'wxNative'}">
        <p style="line-height: 35px;color: #666666;">请使用微信扫码或长按识别二维码付款</p>
    </c:if>
    <c:if test="${scanCodeWay eq 'alipay'}">
        <p style="line-height: 35px;">请使用支付宝扫码付款</p>
    </c:if>
    <p style="color: red;line-height: 35px;font-weight: bold;">提示：确认收款方真实性，请勿向陌生人支付。</p>
</div>
</body>
</html>
