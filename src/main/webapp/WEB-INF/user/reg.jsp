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
    <title>注册</title>
    <script type="text/javascript" src="${ctx}/js/jquery-1.9.1.min.js"></script>
    <script src="${ctx}/js/jquery-weui.min.js"></script>
    <script>
        function reg() {
            var encryptUserNo = "${userNo}";
            var merchantName = $.trim($('#merchantName').val());
            var bankcard = $.trim($('#bankcard').val());
            var idcard = $.trim($('#idcard').val());
            var realname = $.trim($('#realname').val());
            var mobileNo = $.trim($('#mobileNo').val());
            if(isNullOrEmpty(merchantName)){
                $("#merchantNameDiv").addClass("weui_cell_warn");
                $.toast("请输入商户名称,作为收款展示", "forbidden");
                return;
            }
            if(isNullOrEmpty(bankcard)){
                $("#bankcardDiv").addClass("weui_cell_warn");
                $.toast("请输入银行卡号", "forbidden");
                return;
            }
            if(isNullOrEmpty(idcard)){
                $("#idcardDiv").addClass("weui_cell_warn");
                $.toast("请输入身份证号", "forbidden");
                return;
            }
            if(isNullOrEmpty(realname)){
                $("#realnameDiv").addClass("weui_cell_warn");
                $.toast("请输入真实姓名", "forbidden");
                return;
            }
            if(isNullOrEmpty(mobileNo)){
                $("#mobileNoDiv").addClass("weui_cell_warn");
                $.toast("请输入手机号码", "forbidden");
                return;
            }
            if(!verifyMobileNo(mobileNo)){
                $("#mobileNoDiv").addClass("weui_cell_warn");
                $.toast("请输入合法手机号码", "forbidden");
                return;
            }
            $.showLoading("正在注册...");
            $.ajax({
                url:"${ctx}/pay/reg",
                type:"POST",
                data:{"userNo":encryptUserNo,"merchantName":merchantName,"bankcard":bankcard,"idcard":idcard,"realname":realname,"mobileNo":mobileNo},
                success:function(data){
                    $.hideLoading();
                    if(!jQuery.isEmptyObject(data)){
                        var params = eval(data);
                        var success = params.success;
                        var msg = params.msg;
                        if(success){
                            $.toast("注册成功",function(){
                                window.location.href = "${ctx}/pay/toMyPayCode?userNo="+encryptUserNo;
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


    </script>
    <style type="text/css">

        .weui_cells{
            line-height: 10px;
        }
    </style>
</head>
<body <%-- style="background-color: #f5f5f5"--%>>

<header class='demos-header'>
    <h1 class="demos-title" style="font-size: 24px;">收款码注册</h1>
    <%--<p class='demos-sub-title'>请务必输入真实信息，以便资金结算</p>--%>
</header>
<div>
    <div class="weui_cells">
        <div id="merchantNameDiv" class="weui_cell">
            <div class="weui_cell_hd"><label class="weui_label">商户名称</label></div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="merchantName" name="merchantName" class="weui_input" type="text" placeholder="商户中文名称">
            </div>
        </div>
        <div id="bankcardDiv" class="weui_cell">
            <div class="weui_cell_hd"><label class="weui_label">银行卡号</label></div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="bankcard" name="bankcard" class="weui_input" type="number" pattern="[0-9]*" placeholder="结算银行卡号">
            </div>
        </div>
        <div id="mobileNoDiv" class="weui_cell">
            <div class="weui_cell_hd"><label class="weui_label">手机号码</label></div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="mobileNo" name="mobileNo" class="weui_input" type="number" placeholder="结算银行卡预留手机号码">
            </div>
        </div>
        <div id="realnameDiv" class="weui_cell">
            <div class="weui_cell_hd"><label class="weui_label">真实姓名</label></div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="realname" name="realname" class="weui_input" type="text" placeholder="结算银行卡账户名">
            </div>
        </div>
        <div id="idcardDiv" class="weui_cell">
            <div class="weui_cell_hd"><label class="weui_label">身份证号</label></div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="idcard" name="idcard" class="weui_input" type="text" placeholder="与结算名对应的身份证号码">
            </div>
        </div>
    </div>
    <div class="weui_btn_area" style="margin-bottom: 40px;">
        <a href="javascript:reg();"  class="weui_btn weui_btn_primary">开始注册</a>
    </div>
</div>


<%--<div class="container" id="container">
    <div class="bd">
        <div class="weui_cells_title">手机验证</div>
        <div class="weui_cells weui_cells_form">
            <div class="weui_cell">
                <div class="weui_cell_hd"><label class="weui_label">手机号码</label></div>
                <div class="weui_cell_bd weui_cell_primary">
                    <input class="weui_input" id="mobileNo" type="number" pattern="[0-9]*"   placeholder="请输入结算银行卡预留手机号码">
                </div>
            </div>
        </div>
        <div class="weui_cells_title">商户信息</div>
        <div class="weui_cells weui_cells_form">
            <div class="weui_cell">
                <div class="weui_cell_hd"><label class="weui_label">商户名称</label></div>
                <div class="weui_cell_bd weui_cell_primary">
                    <input class="weui_input" id="merchantName"   placeholder="商户中文名称">
                </div>
            </div>

            <div class="weui_cell">
                <div class="weui_cell_hd"><label class="weui_label">姓名</label></div>
                <div class="weui_cell_bd weui_cell_primary">
                    <input class="weui_input" id="realName"   placeholder="结算银行卡账户名">
                </div>
            </div>
            <div class="weui_cell">
                <div class="weui_cell_hd"><label class="weui_label">结算卡号</label></div>
                <div class="weui_cell_bd weui_cell_primary">
                    <input class="weui_input" id="bankNo" type="number"   pattern="[0-9]*" placeholder="结算银行卡号">
                </div>
            </div>
            <div class="weui_cell">
                <div class="weui_cell_hd"><label class="weui_label">身份证号码</label></div>
                <div class="weui_cell_bd weui_cell_primary">
                    <input class="weui_input" id="idCardNo"  placeholder="与结算名对应的身份证号码">
                </div>
            </div>
        </div>

        <div class="weui_btn_area">
            <a class="weui_btn weui_btn_primary" onclick="reg();" id="showTooltips">开始注册</a>
        </div>
    </div>

</div>--%>


</body>
</html>