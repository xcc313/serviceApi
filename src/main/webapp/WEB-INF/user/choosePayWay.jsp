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
    <title>选择支付方式</title>
    <style type="text/css">
    </style>
    <script type="text/javascript">

        $(function(){
            var unipayCardListSize = '${unipayCardListSize}';
            if(unipayCardListSize>0){
                showCardSelect();
            }
            $("[type=radio]").bind("click",function(){
                var radioVal = $(this).val();
                if(radioVal=="unipay"){
                    $("#accountNoCellsDiv").show();
                }else{
                    $("#accountNoCellsDiv").hide();
                }
            })
            $("#fastpayCardSelect").bind("change",function(){
                var chooseVal = $(this).val();
                if(chooseVal=="otherCard"){
                    hideCardSelect();
                }
            })
        })

        function surePayWay(){
            var userNo = "${userNo}";
            var payType = $('input[name="payType"]:checked').val();
            var amount = "${amount}";
            var unipayCard = "";
            var isReme = "";
            if(payType=="unipay"){
                if($("#fastpayCardSelect").is(":hidden") && !$("#accountNo").is(":hidden")){
                    var accountNo = $.trim($("#accountNo").val());
                    if(isNullOrEmpty(accountNo)){
                        $.alert("请填写银联卡");
                        return;
                    }
                    unipayCard = accountNo;
                    isReme = $("#isReme").is(":checked");
                }else if(!$("#fastpayCardSelect").is(":hidden") && $("#accountNo").is(":hidden")){
                    var fastpayCardSelectVal = $("#fastpayCardSelect").val();
                    if(fastpayCardSelectVal=="otherCard"){
                        $.alert("请选择银联卡");
                        return;
                    }
                    unipayCard = fastpayCardSelectVal;
                }else{
                    $.alert("出现异常，请反馈至公众号");
                    return;
                }
                console.log(unipayCard+"---------"+isReme);
                if(unipayCard.length<15){
                    $.toast("请输入正确的银联卡", "forbidden");
                    return;
                }
            }
            $.showLoading("正在加载...");
            var i = Math.random() * 4;
            var abc = parseInt(i);
            $.ajax({
                url:"${ctx}/pay/payCreateOrder",
                type: "POST",
                cache : false,
                data: {userNo: '${userNo}', payType: payType, amount: amount, unipayCard: unipayCard, isReme: isReme, abc:Math.random()},
                success:function (data) {
                    $.hideLoading();
                    if(!jQuery.isEmptyObject(data)){
                        console.log(data);
                        var params = eval(data);
                        var success = params.success;
                        var msg = params.msg;
                        if(success){
                            if(payType=="unipay"){
                                window.location.href = msg;
                            }else{
                                window.location.href = "${ctx}/pay/toQrcodePay?codeUrl="+msg+"&amount="+amount+"&userNo=${userNo}&scanCodeWay="+payType;
                            }
                        }else{
                            $.alert(msg)
                        }
                    }else{
                        $.alert("下单异常，请稍后重试");
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

        function showCardSelect(){
            $("#accountNo").hide();
            $("#fastpayCardSelect").show();
            $("#accountNoDiv").addClass("weui_cell_select");
            $("#remeCardDiv").hide();
        }
        function hideCardSelect(){
            $("#accountNo").show();
            $("#remeCardDiv").show();
            $("#fastpayCardSelect").hide();
            $("#accountNoDiv").removeClass("weui_cell_select");
        }

    </script>
</head>
<body style="font-family: 微软雅黑;background-color: #f3f3f3">
<div>
    <div class="weui_cells_title">收款信息</div>
    <div class="weui_cells">
        <div class="weui_cell">
            <div class="weui_cell_bd weui_cell_primary">
                <p>商户名称</p>
            </div>
            <div class="weui_cell_ft">
                <p>${merchantName}</p>
            </div>
        </div>
        <div class="weui_cell">
            <div class="weui_cell_bd weui_cell_primary">
                <p>联系电话</p>
            </div>
            <div class="weui_cell_ft">
                <p>${mobileNo}</p>
            </div>
        </div>
        <div class="weui_cell">
            <div class="weui_cell_bd weui_cell_primary">
                <p>交易金额</p>
            </div>
            <div class="weui_cell_ft">
                <p>${amount}元</p>
            </div>
        </div>
    </div>
    <div class="weui_cells_title">选择支付方式</div>
    <div class="weui_cells weui_cells_radio">
        <label class="weui_cell weui_check_label" for="x11">
            <div class="weui_cell_hd">
                <img src="${ctx}/images/wxPay.png" alt="icon" style="width:25px;margin-right:10px;display:block;">
            </div>
            <div class="weui_cell_bd weui_cell_primary">
                <p>微信</p>
                <p style="color: #999;font-size: 13px;">微信扫码支付(建议3000元以下交易)</p>
            </div>
            <div class="weui_cell_ft">
                <input type="radio" class="weui_check" name="payType" id="x11" value="wxNative">
                <span class="weui_icon_checked"></span>
            </div>
        </label>
        <label class="weui_cell weui_check_label" for="x12">
            <div class="weui_cell_hd">
                <img src="${ctx}/images/alipay.png" alt="icon" style="width:25px;margin-right:10px;display:block;">
            </div>
            <div class="weui_cell_bd weui_cell_primary">
                <p>支付宝</p>
                <p style="color: #999;font-size: 13px;">使用支付宝支付</p>
            </div>
            <div class="weui_cell_ft">
                <input type="radio" name="payType" class="weui_check" id="x12" checked="checked" value="alipay">
                <span class="weui_icon_checked"></span>
            </div>
        </label>
        <label class="weui_cell weui_check_label" for="x13">
            <div class="weui_cell_hd">
                <img src="${ctx}/images/unionPay.png" alt="icon" style="width:25px;margin-right:10px;display:block;">
            </div>
            <div class="weui_cell_bd weui_cell_primary">
                <p>银行卡支付</p>
                <p style="color: #999;font-size: 13px;">使用带有银联标志的银行卡付款(大额建议)</p>
            </div>
            <div class="weui_cell_ft">
                <input type="radio" name="payType" class="weui_check" id="x13" checked="checked" value="unipay">
                <span class="weui_icon_checked"></span>
            </div>
        </label>
        <%--<label class="weui_cell weui_check_label" for="x13">
            <div class="weui_cell_hd">
                <img src="${ctx}/images/unionPay.png" alt="icon" style="width:25px;margin-right:10px;display:block;">
            </div>
            <div class="weui_cell_bd weui_cell_primary">
                <p>快捷支付</p>
                <p style="color: #999;font-size: 13px;">使用带有银联标志的信用卡付款(大额建议)</p>
            </div>
            <div class="weui_cell_ft">
                <input type="radio" name="payType" class="weui_check" id="x13" checked="checked" value="fastPay">
                <span class="weui_icon_checked"></span>
            </div>
        </label>--%>
    </div>
    <div class="weui_cells" style="margin-top: 0px;" id="accountNoCellsDiv">
        <div class="weui_cell <%--weui_cell_select--%>" style="padding-left: 15px;" id="accountNoDiv">
            <div class="weui_cell_hd"><label class="weui_label">银行卡号</label></div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="accountNo" name="accountNo" class="weui_input" type="number" pattern="[0-9]*" placeholder="带有银联标志的银行卡" maxlength="20">
                <select class="weui_select" id="fastpayCardSelect" style="display: none">
                    <c:forEach items="${unipayCardList}" var="unipayCard" >
                        <option  value="${unipayCard.account_no}">${unipayCard.account_no}</option>
                    </c:forEach>
                    <option  value="otherCard">其它银行卡</option>
                </select>
            </div>
        </div>
        <div class="weui_cell weui_cell_switch" id="remeCardDiv">
            <div class="weui_cell_hd weui_cell_primary">记住卡号</div>
            <div class="weui_cell_ft">
                <input id="isReme" class="weui_switch" type="checkbox" checked>
            </div>
        </div>
    </div>
    <div class="weui_btn_area" style="margin-bottom: 40px;">
        <a href="javascript:surePayWay();"  class="weui_btn weui_btn_primary">确认支付</a>
    </div>
</div>

</body>
</html>