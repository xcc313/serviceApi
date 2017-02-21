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
    <title>收款</title>
    <script type="text/javascript" src="${ctx}/js/jquery-1.9.1.min.js"></script>
    <script src="${ctx}/js/jquery-weui.min.js"></script>
    <style type="text/css">
        .weui_dialog, .weui_toast {
            top: 20%;
        }
    </style>

    <script type="text/javascript">
        var testMer=new Array("111");


        var number;
        $(function () {

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


            function numberClick() {
                var val = $(this).text();
                var d = $("#inputAmount");
                var t = d.text();
                if (val == "←") {
                    d.text(t.substring(0, t.length - 1))
                }else if ($(this).hasClass("confirm_pay")) {
                    var payConfirm = $(".confirm_pay");
                    if (!payConfirm.data("payIng")) {
                        payConfirm.data("payIng", true);
                    } else {
                        return;
                    }
                    if (t < 1) {
                        $.alert('单笔金额需在1元以上');
                        payConfirm.data("payIng", false);
                        return;
                    }
                    toPay(t);

                } else if (/^[1-9]\d{0,6}\.?\d{0,2}$/.test(t + val) || /^(0||0\.||0\.\d||0\.0[1-9]||0\.[1-9]\d)$/.test(t + val)) {
                    d.text(t + val);
                }
            }

            function toPay(t){
                var payConfirm = $(".confirm_pay");
                $.showLoading("正在加载...");
                var i = Math.random() * 4;
                var abc = parseInt(i);
                $.getJSON("${ctx}/pay/payCreateOrder", {
                    userNo: '${userNo}',
                    payType: '${payType}',
                    amount: t
                }, function (data) {
                    $.hideLoading();
                    payConfirm.data("payIng", false);
                    if(!jQuery.isEmptyObject(data)){
                        var params = eval(data);
                        var success = params.success;
                        var msg = params.msg;
                        if(success){
                            window.location.href = "${ctx}/pay/toQrcodePay?codeUrl="+msg+"&amount="+t+"&userNo=${userNo}&payType=${payType}";
                        }else{
                            $.alert(msg)
                        }
                    }else{
                        $.alert("下单异常，请稍后重试");
                    }
                })
            }
            $(".table td").on("touchstart", numberClick);
        });

    </script>
    <style type="text/css">
        .amountText{
            font-size: 18px;
            color: #888;
        }
    </style>
</head>
<body>


<div class="shop_title">
    <img id="headimgurl" src="${headimgurl}" style="width: 40px;height: 40px;border-radius: 100%;" />
    <span style="font-size: 20px;line-height: 40px;">${merchantName}</span>
</div>
<div style="clear: both"></div>
<div class="weui_cells weui_cells_access" style="margin:0px 20px;border: 1px solid #cccccc;border-radius:8px">
    <a class="weui_cell" href="javascript:;" style="line-height: 36px;font-weight: bold;font-size: 25px;font-family: 微软雅黑;">
        <div class="weui_cell_hd">
            ￥
        </div>
        <div class="weui_cell_bd weui_cell_primary" style="text-align: right;">
            <p id="inputAmount"></p>
        </div>
    </a>
    <a class="weui_cell" href="javascript:;">
        <div class="weui_cell_bd weui_cell_primary" style="text-align: right;">
            <p style="color: #888;">支付改变生活</p>
        </div>
    </a>
</div>
<div class="bd fixed_bottom">
    <div class="align_center">
        <table class="table dis_select" style="background-color: white;color: #101010;">
            <tbody>
            <tr>
                <td>1</td>
                <td>2</td>
                <td>3</td>
                <td style="background-color:white;color:black;font-size:33px;font-family: '微软雅黑'; ">&larr;</td>
            </tr>
            <tr>
                <td>4</td>
                <td>5</td>
                <td>6</td>
                <td rowspan="3" class="confirm_pay" style="font-size: 24px !important;background-color:#04be02;color:white" >
                    确认<br />支付
                </td>
            </tr>
            <tr>
                <td>7</td>
                <td>8</td>
                <td>9</td>
            </tr>
            <tr>
                <td colspan="2">0</td>
                <td>.</td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
