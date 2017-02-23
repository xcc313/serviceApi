<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="user-scalable=no, width=device-width, initial-scale=1.0" />
    <c:set var="ctx" value="${pageContext.request.contextPath}" />
    <script type="text/javascript" src="${ctx}/js/jquery-1.9.1.min.js"></script>
    <link href="${ctx}/css/weui.min.css" rel="stylesheet">
    <link href="${ctx}/css/jquery-weui.min.css" rel="stylesheet">
    <link href="${ctx}/css/demos.css" rel="stylesheet">
    <script src="${ctx}/js/jquery-weui.min.js"></script>
    <title>商户信息修改</title>
    <style type="text/css">
    </style>
    <script type="text/javascript">

        function modify(){
            var encryptUserNo = "${encryptUserNo}";
            var merchantName = $.trim($('#merchantName').val());
            var bankcard = $.trim($('#bankcard').val());
            if(isNullOrEmpty(merchantName) && isNullOrEmpty(bankcard)){
                $.toast("请输入修改信息", "forbidden");
                return;
            }
            $.showLoading("正在修改...");
            $.ajax({
                url:"${ctx}/pay/merchantEdit",
                type:"POST",
                data:{"userNo":encryptUserNo,"merchantName":merchantName,"bankcard":bankcard},
                success:function(data){
                    $.hideLoading();
                    if(!jQuery.isEmptyObject(data)){
                        var params = eval(data);
                        var success = params.success;
                        var msg = params.msg;
                        if(success){
                            $.toast("修改成功",function(){
                                window.location.href = msg;
                            });
                        }else{
                            $.alert(msg)
                        }
                    }else{
                        $.alert("修改异常，请稍后重试");
                    }
                },
                error:function(){
                    $.hideLoading();
                    $.alert("网络异常，请稍后重试");
                }
            })
        }

        function onDevelop(){
            $.alert("正在开发中...");
        }

        function isNullOrEmpty(strVal) {
            if (strVal == '' || strVal == null || strVal == undefined) {
                return true;
            } else {
                return false;
            }
        }

    </script>
</head>
<body style="font-family: 微软雅黑;background-color: #f3f3f3">
<div>
    <div class="weui_cells_title">商户信息</div>
    <div class="weui_cells">
        <div class="weui_cell">
            <div class="weui_cell_bd weui_cell_primary">
                <p>商户名称</p>
            </div>
            <div class="weui_cell_ft">
                <p>${userMap.merchant_name}</p>
            </div>
        </div>
        <div class="weui_cell">
            <div class="weui_cell_bd weui_cell_primary">
                <p>真实姓名</p>
            </div>
            <div class="weui_cell_ft">
                <p>${userMap.real_name}</p>
            </div>
        </div>
        <div class="weui_cell">
            <div class="weui_cell_bd weui_cell_primary">
                <p>身份证号码</p>
            </div>
            <div class="weui_cell_ft">
                <p>${userMap.id_card_no}</p>
            </div>
        </div>
        <div class="weui_cell">
            <div class="weui_cell_bd weui_cell_primary">
                <p>手机号码</p>
            </div>
            <div class="weui_cell_ft">
                <p>${userMap.mobile_no}</p>
            </div>
        </div>
        <div class="weui_cell">
            <div class="weui_cell_bd weui_cell_primary">
                <p>结算卡号</p>
            </div>
            <div class="weui_cell_ft">
                <p>${userMap.bank_no}</p>
            </div>
        </div>
        <div class="weui_cell">
            <div class="weui_cell_bd weui_cell_primary">
                <p>交易手续费(微信、支付宝)</p>
            </div>
            <div class="weui_cell_ft">
                <p>${userMap.wx_trans_fee_rate}%</p>
            </div>
        </div>
        <div class="weui_cell">
            <div class="weui_cell_bd weui_cell_primary">
                <p>提现手续费</p>
            </div>
            <div class="weui_cell_ft">
                <p>${userMap.extraction_fee} 元/次</p>
            </div>
        </div>
    </div>
    <div class="weui_cells_title">修改信息</div>
    <div class="weui_cells weui_cells_form">
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
    </div>
    <div class="weui_btn_area" style="margin-bottom: 40px;">
        <a href="javascript:modify();"  class="weui_btn weui_btn_primary">确认修改</a>
    </div>
</div>

</body>
</html>