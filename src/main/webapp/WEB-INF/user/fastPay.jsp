<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
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
    <title>快捷支付</title>
    <script type="text/javascript" src="${ctx}/js/jquery-1.9.1.min.js"></script>
    <script src="${ctx}/js/jquery-weui.min.js"></script>
    <script>

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

        function fastPay() {
            var encryptUserNo = "${userNo}";
            var fastpayCardId = $.trim($('#fastpayCard').val());
            var expDate = $.trim($('#expDate').val());
            var cvn2 = $.trim($('#cvn2').val());
            var smsCode = $.trim($('#smsCode').val());
            if(isNullOrEmpty(fastpayCardId)){
                $("#fastpayCardDiv").addClass("weui_cell_warn");
                $.toast("请选择支付卡", "forbidden");
                return;
            }
            if(isNullOrEmpty(expDate)){
                $("#expDateDiv").addClass("weui_cell_warn");
                $.toast("请输入卡有效期", "forbidden");
                return;
            }
            if(isNullOrEmpty(cvn2)){
                $("#cvn2Div").addClass("weui_cell_warn");
                $.toast("请输入卡背面末三位", "forbidden");
                return;
            }
            if(isNullOrEmpty(smsCode)){
                $("#smsCodeDiv").addClass("weui_cell_warn");
                $.toast("请输入短信验证码", "forbidden");
                return;
            }
            $.showLoading("正在提交支付...");
            $.ajax({
                url:"${ctx}/pay/payCreateOrder",
                type:"POST",
                data:{"userNo":encryptUserNo,"amount":'${amount}',"payType":"fastPay","smsCode":smsCode,"cvn2":cvn2,"expDate":expDate,"fastPayCardId":fastpayCardId},
                success:function(data){
                    $.hideLoading();
                    if(!jQuery.isEmptyObject(data)){
                        var params = eval(data);
                        var success = params.success;
                        var msg = params.msg;
                        if(success){
                            $.toast("支付成功",function(){
                                WeixinJSBridge.call('closeWindow');
                            });
                        }else{
                            $.alert(msg)
                        }
                    }else{
                        $.alert("注册异常，请稍后重试");
                    }
                },
                error:function(){
                    $.hideLoading();
                    $.alert("网络异常，请稍后重试");
                }
            })
        }

        function isNullOrEmpty(strVal) {
            if (strVal == '' || strVal == null || strVal == undefined) {
                return true;
            } else {
                return false;
            }
        }

        function verifyMobileNo(strVal){
            var isMobile = /^(((13[0-9]{1})|(15[0-9]{1})|(18[0-9]{1})|(17[0-9]{1})|(14[0-9]{1}))+\d{8})$/;
            if (!isMobile.exec(strVal) || strVal.length != 11) {
                return false;
            }else{
                return true;
            }
        }

        var count = 60;
        var timer;
        function sendVerifySms(){
            var sendBtn = $("#sendSmsBotton");
            var encryptUserNo = "${userNo}";
            $.showLoading("正在发送...");
            $.ajax({
                async:false,
                type: "POST",
                url:'${ctx}/api/sendSmsCode',
                data: {
                    userNo:encryptUserNo
                },
                success: function(data) {
                    $.hideLoading();
                    if(!jQuery.isEmptyObject(data)){
                        var params = eval(data);
                        resultSuccess = params.success;
                        resultMsg = params.msg;
                        if(resultSuccess){
                            $.toast("验证码发送成功");
                            timer = setInterval(function(){
                                count--;
                                sendBtn.html(count + 's');
                                if(count == 0){
                                    clearInterval(timer);
                                    timer = undefined;
                                    count = 60;
                                    sendBtn.html('获取验证码');
                                }
                            }, 1000);
                        }else{
                            $.toast(resultMsg, "forbidden");
                        }
                    }else{
                        $.toast("发送短信异常", "forbidden");
                    }
                },
                error: function(request) {
                    $.hideLoading();
                    $.toast("发送短信网络异常", "forbidden");
                }
            });
        }

        function sureBind(){
            var encryptUserNo = "${userNo}";
            var accountNo = $.trim($('#accountNo').val());
            var picVerify = $.trim($('#picVerify').val());
            if(isNullOrEmpty(accountNo)){
                $("#accountNoDiv").addClass("weui_cell_warn");
                $.toast("请输入信用卡卡号", "forbidden");
                return;
            }
            if(isNullOrEmpty(picVerify)){
                $("#picVerifyDiv").addClass("weui_cell_warn");
                $.toast("请输入图形验证码", "forbidden");
                return;
            }
            $.showLoading("正在添加...");
            $.ajax({
                url:"${ctx}/pay/bindFastpayCard",
                type:"POST",
                data:{"userNo":encryptUserNo,"accountNo":accountNo,"picVerify":picVerify},
                success:function(data){
                    $.hideLoading();
                    if(!jQuery.isEmptyObject(data)){
                        var params = eval(data);
                        var success = params.success;
                        var msg = params.msg;
                        if(success){
                            $.toast("添加成功",function(){
                                $("#fastpayCard").html("");
                                var id,item,bank_name,account_no;
                                for(var i=0, l=msg.length; i<l; i++) {
                                    item = msg[i];
                                    id = item.id;
                                    bank_name = item.bank_name;
                                    account_no = item.account_no;
                                    //alert("id="+id+",bank_name="+bank_name+",account_no="+account_no);
                                    $("#fastpayCard").append("<option  value=\""+id+"\">"+bank_name+"  ("+account_no+")</option>");
                                }
                                closePopup();
                            });
                        }else{
                            $.alert(msg,function(){
                                refreshCaptcha();
                            })
                        }
                    }else{
                        $.alert("添加异常，请稍后重试",function(){
                            refreshCaptcha();
                        });
                    }
                },
                error:function(){
                    $.hideLoading();
                    $.alert("网络异常，请稍后重试",function(){
                        refreshCaptcha();
                    });
                }
            })
        }

        function refreshCaptcha() {
            var _captcha_id = document.getElementById("img_captcha");
            _captcha_id.src="${ctx }/servlet/captchaCode?t=" + Math.random();
        }

        function openPopup(){
            $("#bindCardDiv").popup();
        }

        function closePopup(){
            $.closePopup();
        }


    </script>
    <style type="text/css">

        .weui_cells{
            line-height: 10px;
        }
    </style>
</head>
<body style="background-color: #f3f3f3">
    <div>
        <div style="margin-top: 30px;text-align: center;border-bottom: 1px dashed #666">
            <div style="font-family: 微软雅黑;">
                <p style="font-weight: bold;font-size: 22px;line-height: 35px;">支付金额：￥${amount}元</p>
            </div>
        </div>
        <div class="weui_cells_title">
            <span>选择银行卡&nbsp;&nbsp;&nbsp;</span>
            <a  href="javascript:openPopup();" class="weui_btn weui_btn_mini weui_btn_primary" style="vertical-align: middle;">添加</a>
        </div>
        <div class="weui_cells">
            <div class="weui_cell weui_cell_select" id="fastpayCardDiv">
                <div class="weui_cell_bd weui_cell_primary">
                    <select class="weui_select" id="fastpayCard">
                        <c:forEach items="${fastpayCardList}" var="fastpayCard" >
                            <option  value="${fastpayCard.id}">${fastpayCard.bank_name}  (${fastpayCard.account_no})</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div class="weui_cell">
                <div class="weui_cell_hd"><label class="weui_label">卡有效期</label></div>
                <div class="weui_cell_bd weui_cell_primary">
                    <input class="weui_input" type="text" placeholder="示例：09/15，输入0915" maxlength="5" id="expDate">
                </div>
            </div>
            <div class="weui_cell" id="cvn2Div">
                <div class="weui_cell_hd"><label class="weui_label">CVN2</label></div>
                <div class="weui_cell_bd weui_cell_primary">
                    <input class="weui_input" type="text" placeholder="卡背面末三位" maxlength="3" id="cvn2">
                </div>
            </div>
            <div class="weui_cell">
                <div class="weui_cell_hd"><label class="weui_label">手机号码</label></div>
                <div class="weui_cell_bd weui_cell_primary">
                    <input class="weui_input" type="text" value="${mobileNo}" disabled>
                </div>
            </div>
            <div id="smsCodeDiv" class="weui_cell">
                <div class="weui_cell_hd"><label class="weui_label">短信验证码</label></div>
                <div class="weui_cell_bd weui_cell_primary">
                    <input id="smsCode" name="smsCode" class="weui_input" type="text" placeholder="请输入短信验证码">
                </div>
                <div class="weui_cell_ft">
                    <a id="sendSmsBotton" href="javascript:sendVerifySms();" style="border:0px;border-left: 1px solid #d9d9d9;" class="weui_btn weui_btn_plain_primary">免费获取</a>
                </div>
            </div>
        </div>
        <div class="weui_btn_area" style="margin-bottom: 40px;">
            <a href="javascript:fastPay();"  class="weui_btn weui_btn_primary">确认支付</a>
        </div>
    </div>

    <div id="bindCardDiv" class="weui-popup-container">
        <div class="weui-popup-overlay"></div>
        <div class="weui-popup-modal">
            <header class='demos-header'>
                <h1 class="demos-title" style="font-size: 24px;">添加快捷支付银行卡</h1>
                <%--<p class='demos-sub-title'>请务必输入真实信息，以便资金结算</p>--%>
            </header>
            <div class="weui_cells">
                <div id="accountNameDiv" class="weui_cell">
                    <div class="weui_cell_hd"><label class="weui_label">账户名</label></div>
                    <div class="weui_cell_bd weui_cell_primary">
                        <input id="accountName" name="accountName" class="weui_input" type="text" value="${realName}" disabled>
                    </div>
                </div>
                <div id="idcardDiv" class="weui_cell">
                    <div class="weui_cell_hd"><label class="weui_label">身份证号</label></div>
                    <div class="weui_cell_bd weui_cell_primary">
                        <input id="idcard" name="idcard" class="weui_input" type="text" value="${idCardNo}" disabled>
                    </div>
                </div>
                <div id="mobileNoDiv" class="weui_cell">
                    <div class="weui_cell_hd"><label class="weui_label">手机号码</label></div>
                    <div class="weui_cell_bd weui_cell_primary">
                        <input id="mobileNo" name="mobileNo" class="weui_input" type="text" value="${mobileNo}" disabled>
                    </div>
                </div>
                <div id="accountNoDiv" class="weui_cell">
                    <div class="weui_cell_hd"><label class="weui_label">银行卡号</label></div>
                    <div class="weui_cell_bd weui_cell_primary">
                        <input id="accountNo" name="accountNo" class="weui_input" type="number" pattern="[0-9]*" placeholder="信用卡卡号">
                    </div>
                </div>
                <div id="picVerifyDiv" class="weui_cell">
                    <div class="weui_cell_hd"><label class="weui_label">验证码</label></div>
                    <div class="weui_cell_bd weui_cell_primary">
                        <input id="picVerify" name="picVerify" class="weui_input" placeholder="图形验证码">
                    </div>
                    <div class="weui_cell_ft">
                        <img title="点击更换" style="width: 120px;" id="img_captcha"  onclick="javascript:refreshCaptcha();" src="${ctx }/servlet/captchaCode" />
                    </div>
                </div>
            </div>
            <div class="weui_btn_area" style="margin-bottom: 40px;">
                <a href="javascript:sureBind();"  class="weui_btn weui_btn_primary">确认添加</a>
                <a href="javascript:closePopup();" class="weui_btn weui_btn_default">返回</a>
            </div>
        </div>
    </div>

</body>
</html>