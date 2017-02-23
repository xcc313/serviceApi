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
    <link rel="stylesheet" href="https://cdn.staticfile.org/weui/1.0.2/style/weui.css"/>
    <link href="${ctx}/css/weui.min.css" rel="stylesheet">
    <link href="${ctx}/css/jquery-weui.min.css" rel="stylesheet">
    <link href="${ctx}/css/demos.css" rel="stylesheet">
    <title>我的收款码</title>
    <script src="https://cdn.staticfile.org/jquery/1.9.1/jquery.min.js"></script>
    <script>window.jQuery || document.write('<script src="${ctx}/js/jquery-1.9.1.min.js">')</script>
    <script type="text/javascript" src="${ctx}/js/jquery-weui.min.js"></script>
    <script src="https://cdn.staticfile.org/jquery.qrcode/1.0/jquery.qrcode.min.js"></script>
    <script src="http://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>
    <script type="text/javascript">
        $(function () {
            $("#qrContainer").qrcode({width:200,height:200,correctLevel:0,text:'${url}'});
            var canvas=$("#qrContainer").find("canvas").get(0);
            $("#imgQr").attr("src",canvas.toDataURL("image/png"));
        })


        function onBridgeReady() {
            WeixinJSBridge.call('hideOptionMenu');
        }

        if (typeof WeixinJSBridge == "undefined") {
            if (document.addEventListener) {
                document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
            } else if (document.attachEvent) {
                document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
                document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
            }
        } else {
            onBridgeReady();
        }

        wx.config({
            debug: false,
            appId: '${appId}',
            timestamp: ${timestamp},
            nonceStr: '${noncestr}',
            signature: '${sign}',
            jsApiList: [
                'scanQRCode'
            ]
        });

        wx.ready(function () {

        });

        wx.error(function (res) {
            alert(res.errMsg);
            // config信息验证失败会执行error函数，如签名过期导致验证失败，具体错误信息可以打开config的debug模式查看，也可以在返回的res参数中查看，对于SPA可以在这里更新签名。

        });

        function saoyisao(){
            wx.scanQRCode({
                needResult: 1, // 默认为0，扫描结果由微信处理，1则直接返回扫描结果，
                scanType: ["qrCode","barCode"], // 可以指定扫二维码还是一维码，默认二者都有
                success: function (res) {
                    var result = res.resultStr; // 当needResult 为 1 时，扫码返回的结果
                    $.showLoading("正在绑定...");
                    $.ajax({
                        url:"${ctx}/pay/bandParentNo",
                        type:"POST",
                        data:{"userNo":"${userNo}","parentUrl":result},
                        success:function(data){
                            $.hideLoading();
                            if(!jQuery.isEmptyObject(data)){
                                var params = eval(data);
                                var success = params.success;
                                var msg = params.msg;
                                if(success){
                                    //$.toast(msg);
                                    $.alert(msg,function(){
                                        window.location.href="${ctx}/pay/toMyPayCode?userNo=${userNo}";
                                    })
                                }else{
                                    $.alert(msg)
                                }
                            }else{
                                $.alert("绑定异常，请稍后重试");
                            }
                        },
                        error:function(){
                            $.hideLoading();
                            $.alert("网络异常，请稍后重试");
                        }
                    })
                }
            });
        }
    </script>

    <style>

        h4{
            margin-bottom: 20px;
        }
        a{color:#18A1CC;text-decoration:underline;}
    </style>
</head>
<body>
<div id="qrContainer" style="display: none;"></div>
<div style="margin-top: 5px;">
    <header class='demos-header' style="padding: 20px 0;">
        <h1 class="demos-title"><img id="headimgurl" src="${headimgurl}" height="60px" width="60px" /></h1>
        <p class='demos-sub-title' style="color: black;font-family: 微软雅黑;" id="merchantName">${merchantName}</p>
    </header>
    <div style="text-align: center">
        <p class="page_desc"><img id="imgQr" src="" alt=""></p>
        <p style="color: #888;margin: 20px 0px;">用微信或支付宝扫描二维码付款或</p>
        <a href="${url}" class="weui_btn weui_btn_primary" style="margin:14px;">识别二维码,微信支付</a>
        <c:if test="${empty parentNo || parentNo=='null'}">
            <a href="javascript:saoyisao();" class="weui_btn weui_btn_plain_primary" style="margin:14px;">点此扫描推荐人收款码,费率再降</a>
        </c:if>
    </div>
</div>

</body>
</html>
