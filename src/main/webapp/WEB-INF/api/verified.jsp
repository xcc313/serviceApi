<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>
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
    <title>银行卡实名认证</title>
    <style type="text/css">
        .surePayText{
            color: #fd8d2c !important;
        }
    </style>
    <script type="text/javascript">
        $(function(){
            $("#idcardDiv").hide();
            $("#mobileNoDiv").hide();
            $("#selectIcon").click(function(){
                $("#verifiedType").select("open");
            })
        })

        var timeStamp="",packageStr="",paySign="",appId="",signType="",nonceStr="",qrCodeId="";
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
                        //alert(res.err_desc);
                        // 使用以上方式判断前端返回,微信团队郑重提示：res.err_msg将在用户支付成功后返回    ok，但并不保证它绝对可靠。
                        if(res.err_msg == "get_brand_wcpay_request:ok" ) {
                            //document.location.reload();
                            $.alert("充值成功");
                            $.closePopup();
                            //$('#surePayModal').modal('toggle');
                            //$('#surePayModal').popover('hide');
                        }
                    }
            );
        }

        function toPay(){
            if (typeof WeixinJSBridge == "undefined"){
                $.toast("请通过微信支付", "forbidden");
                if( document.addEventListener ){
                    document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
                }else if (document.attachEvent){
                    document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
                    document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
                }
            }else{
                var rechargeAmount = $.trim($("#rechargeAmount").val());
                if(isNullOrEmpty(rechargeAmount)){
                    $("#rechargeAmountDiv").addClass("weui_cell_warn");
                    $.toast("请输入充值金额", "forbidden");
                    return;
                }
                var INT_REG = /^[1-9][0-9]*$/;//正整数
                if (!rechargeAmount.match(INT_REG)) {
                    $.toast("请填写正确的充值金额", "forbidden");
                    return ;
                }
                var userNo = "${userNo}";
                if(userNo=="" || userNo==null || userNo=="null"){
                    $.toast("请重新进入", "forbidden");
                    return;
                }
                $.showLoading("支付中...");
                $.ajax({
                    url:'${ctx}/wx/weixinRechargePay',
                    type:'POST',
                    data:{'userNo':userNo,'rechargeAmount':rechargeAmount,'channel':0},
                    dataType:'json',
                    success:function(data){
                        $.hideLoading();
                        if(!jQuery.isEmptyObject(data)){
                            if(data.head.result_code == 'FAIL'){
                                $.alert(data.head.result_msg);
                            }else{
                                //var params = eval(data);
                                timeStamp = data.content.timeStamp;
                                packageStr = data.content.package;
                                paySign = data.content.paySign;
                                appId = data.content.appId;
                                signType = data.content.signType;
                                nonceStr = data.content.nonceStr;
                                onBridgeReady();
                            }
                        }else{
                            $.alert("系统异常");
                        }
                    },
                    error:function(){
                        $.hideLoading();
                        $.alert("网络异常，请稍后重试");
                    }
                })
            }

        }

        function verifiedResult(){
            var userNo = "${userNo}";
            var verifiedType = $.trim($('#verifiedType').val());
            var bankcard = $.trim($('#bankcard').val());
            var idcard = $.trim($('#idcard').val());
            var realname = $.trim($('#realname').val());
            var mobileNo = $.trim($('#mobileNo').val());
            if(isNullOrEmpty(bankcard)){
                $("#bankcardDiv").addClass("weui_cell_warn");
                //$.alert("请输入银行卡号");
                $.toast("请输入银行卡号", "forbidden");
                return;
            }
            if(verifiedType!='二元素' && isNullOrEmpty(idcard)){
                $("#idcardDiv").addClass("weui_cell_warn");
                $.toast("请输入身份证号", "forbidden");
                return;
            }
            if(isNullOrEmpty(realname)){
                $("#realnameDiv").addClass("weui_cell_warn");
                $.toast("请输入真实姓名", "forbidden");
                return;
            }
            if(verifiedType=='四元素' && isNullOrEmpty(mobileNo)){
                $("#mobileNoDiv").addClass("weui_cell_warn");
                $.toast("请输入手机号码", "forbidden");
                return;
            }
            $.modal({
                title: "确认提示",
                text: "此查询将消耗10微币",
                buttons: [
                    { text: "确认查询", className:"surePayText", onClick: function(){ sureCheck(bankcard,idcard,realname,mobileNo); } },
                    { text: "微币充值", onClick: function(){ /*window.location.href='${ctx}/user/rechargeCoin?userNo='+userNo*/ $("#rechargePopup").popup(); } },
                    { text: "取消", className: "default", onClick: function(){ } },
                ]
            });
        }

        function sureCheck(bankcard,idcard,realname,mobileNo){
            var userNo = "${userNo}";
            $.showLoading("正在查询...");
            $.ajax({
                url:"${ctx}/api/verifiedResult",
                type:"GET",
                data:{"bankcard":bankcard,"idcard":idcard,"realname":realname,"mobileNo":mobileNo,"userNo":userNo},
                success:function(data){
                    $.hideLoading();
                    if(!jQuery.isEmptyObject(data)){
                        //var params = eval(data);
                        if(data.head.result_code == 'FAIL'){
                            $.alert(data.head.result_msg);
                        }else{
                            var isok = data.content.isok;
                            var code = data.content.code;
                            var idcard = data.content.idcard;
                            var mobile_no = data.content.mobile_no;
                            var bankname = data.content.bankname;
                            var cardname = data.content.cardname;
                            var idcardText = "";
                            var mobileNoText = "";
                            var banknameText = "";
                            var cardnameText = "";
                            if(!isNullOrEmpty(idcard)){
                                idcardText = "<p>身份证号:"+idcard+"</p>";
                            }
                            if(!isNullOrEmpty(mobile_no)){
                                mobileNoText = "<p>手机号码:"+mobile_no+"</p>";
                            }
                            if(!isNullOrEmpty(bankname)){
                                banknameText = "<p>银行名称:"+bankname+"</p>";
                            }
                            if(!isNullOrEmpty(cardname)){
                                cardnameText = "<p>卡名称:"+cardname+"</p>";
                            }
                            if(isok=='1' && code=='1'){
                                $.modal({
                                    title: "实名查询结果",
                                    text: "<p style='color:#0bb20c;'>恭喜您，信息校验一致！</p><p>银行卡号:"+data.content.bankcard+"</p><p>真实姓名:"+data.content.realname+"</p>"+idcardText+mobileNoText+banknameText+cardnameText,
                                    buttons: [
                                        { text: "朕知道了", className: "default", onClick: function(){ WeixinJSBridge.call('closeWindow'); } },
                                        { text: "打赏一下", onClick: function(){ window.location.href='${ctx}/user/reward?userNo=${userNo}'; } },
                                    ]
                                });
                            }else{
                                $.modal({
                                    title: "实名查询结果",
                                    text: "<p style='color:#f6383a;'>信息校验不一致！</p><p>银行卡号:"+data.content.bankcard+"</p><p>真实姓名:"+data.content.realname+"</p>"+idcardText+mobileNoText,
                                    buttons: [
                                        { text: "朕知道了", className: "default", onClick: function(){ WeixinJSBridge.call('closeWindow'); } },
                                        { text: "打赏一下", onClick: function(){ window.location.href='${ctx}/user/reward?userNo=${userNo}'; } },
                                    ]
                                });
                            }

                        }

                    }else{
                        $.alert("更新异常，请稍后重试");
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
<body>
<header class='demos-header'>
    <h1 class="demos-title" style="font-size: 24px;">实名认证</h1>
    <p class='demos-sub-title'>极速查询银行卡信息是否一致，支持二三四元素</p>
</header>
<div>
    <div class="weui_cells">
        <div id="verifiedTypeDiv" class="weui_cell">
            <div class="weui_cell_hd"><label class="weui_label">验证元素</label></div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="verifiedType" name="verifiedType" class="weui_input" type="text" placeholder="请选择银行卡验证元素" value readonly>
            </div>
            <div class="weui_cell_ft" id="selectIcon">
                <img src="${ctx}/images/choose.png" width="20px;">
            </div>
            <script>
                $("#verifiedType").select({
                    title: "请选择验证元素",
                    input: "二元素",
                    items: ["二元素", "三元素", "四元素"],
                    onClose: function changeVerifiedType(){
                        var verifiedType = $.trim($('#verifiedType').val());
                        if(verifiedType=='二元素'){
                            $("#idcardDiv").hide();
                            $("#mobileNoDiv").hide();
                        }else if(verifiedType=='三元素'){
                            $("#idcardDiv").show();
                            $("#mobileNoDiv").hide();
                        }else if(verifiedType=='四元素'){
                            $("#idcardDiv").show();
                            $("#mobileNoDiv").show();
                        }
                    }
                });
            </script>
        </div>
        <div id="bankcardDiv" class="weui_cell">
            <div class="weui_cell_hd"><label class="weui_label">银行卡号</label></div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="bankcard" name="bankcard" class="weui_input" type="number" pattern="[0-9]*" placeholder="请输入银行卡号">
            </div>
        </div>
        <div id="realnameDiv" class="weui_cell">
            <div class="weui_cell_hd"><label class="weui_label">真实姓名</label></div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="realname" name="realname" class="weui_input" type="text" placeholder="请输入真实姓名">
            </div>
        </div>
        <div id="idcardDiv" class="weui_cell">
            <div class="weui_cell_hd"><label class="weui_label">身份证号</label></div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="idcard" name="idcard" class="weui_input" type="text" placeholder="请输入身份证号">
            </div>
        </div>
        <div id="mobileNoDiv" class="weui_cell">
            <div class="weui_cell_hd"><label class="weui_label">手机号码</label></div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="mobileNo" name="mobileNo" class="weui_input" type="number" placeholder="请输入手机号码">
            </div>
        </div>
    </div>
    <div class="weui_btn_area" style="margin-bottom: 40px;">
        <a href="javascript:verifiedResult();"  class="weui_btn weui_btn_primary">确定</a>
    </div>
    <%--<div class="weui_cells" id="lotteryHistoryDiv">
        <div class="weui_cell">
            <div class="weui_cell_bd weui_cell_primary">
                <p style="color: #888;">历史查询记录</p>
            </div>
        </div>
    </div>--%>

    <div id="rechargePopup" class="weui-popup-container popup-bottom">
        <div class="weui-popup-overlay"></div>
        <div class="weui-popup-modal">
            <div class="toolbar">
                <div class="toolbar-inner">
                    <a href="javascript:;" class="picker-button close-popup">关闭</a>
                    <h1 class="title">微币充值</h1>
                </div>
            </div>
            <div class="modal-content">
                <div class="weui_cells_title" style="text-align: center;">
                    <span style="font-size: 16px;font-weight: bold;">1元=10微币</span><span>&nbsp;(首充送10微币)</span>
                </div>
                <div class="weui_cells">
                    <div id="rechargeAmountDiv" class="weui_cell">
                        <div class="weui_cell_hd"><label class="weui_label">微币充值</label></div>
                        <div class="weui_cell_bd weui_cell_primary">
                            <input id="rechargeAmount" name="rechargeAmount" class="weui_input" type="text" placeholder="请输入金额">
                        </div>
                    </div>
                </div>
                <div class="weui_btn_area" style="margin-bottom: 40px;">
                    <a href="javascript:toPay();"  class="weui_btn weui_btn_primary">确定</a>
                </div>
            </div>
        </div>
    </div>

    <div class="weui_msg">
        <div class="weui_extra_area">
            <a href="${ctx}/user/rechargeCoin?userNo=${userNo}">查看微币规则详情</a>
        </div>
    </div>
</div>

</body>
</html>