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
    <script src="${ctx}/js/city-picker.min.js"></script>
    <title>失信查询</title>
    <style type="text/css">
        .surePayText{
            color: #fd8d2c !important;
        }
        .weui_dialog{
            top:20%;
            max-height: 70%;
            overflow-y: auto;
        }
    </style>
    <script type="text/javascript">
        $(function(){

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

        function dishonestyResult(){
            var userNo = "${userNo}";
            var sxName = $.trim($('#sxName').val());
            if(isNullOrEmpty(sxName)){
                $("#sxNameDiv").addClass("weui_cell_warn");
                $.toast("请输入关键字", "forbidden");
                return;
            }

            $.modal({
                title: "确认提示",
                text: "此查询将消耗18微币",
                buttons: [
                    { text: "确认查询", className:"surePayText", onClick: function(){ sureCheck(sxName); } },
                    { text: "微币充值", onClick: function(){ /*window.location.href='${ctx}/user/rechargeCoin?userNo='+userNo*/ $("#rechargePopup").popup(); } },
                    { text: "取消", className: "default", onClick: function(){ } },
                ]
            });

        }

        function sureCheck(sxName){
            var userNo = "${userNo}";
            $.showLoading("正在查询");
            $.ajax({
                url:"${ctx}/api/dishonestyResult",
                type:"GET",
                data:{"sxName":sxName,"userNo":userNo},
                success:function(data){
                    $.hideLoading();
                    if(!jQuery.isEmptyObject(data)){
                        if(data.head.result_code == 'FAIL'){
                            $.alert(data.head.result_msg);
                        }else{
                            var content = data.content;
                            if(!jQuery.isEmptyObject(content)){
                                $("#dishonestyResultDiv").html("");
                                $("#dishonestyResultDiv").append("<div class=\"weui_panel_hd\" style=\"text-align: center;font-size: 17px;\">查询结果:"+sxName+"</div><div class=\"weui_panel_bd\">");
                                var id,item,sx_actionremark,sx_publicdate;
                                for(var i=0, l=content.length; i<l; i++) {
                                    item = content[i];
                                    id = item.id;
                                    sx_actionremark = item.sx_actionremark;
                                    sx_publicdate = item.sx_publicdate;
                                    $("#dishonestyResultDiv").append("<div class=\"weui_media_box weui_media_text\" onclick='showDishonestyDetail("+id+");'> <h4 class=\"weui_media_title\">"+ChangeTimestampToString(sx_publicdate)+"</h4> <p class=\"weui_media_desc\">"+sx_actionremark+"</p> </div>");
                                }
                                $("#dishonestyResultDiv").append("</div>");

                            }else{
                                $.modal({
                                    title: "失信查询结果:"+sxName,
                                    text: "<p style='color:#0bb20c;'>无失信记录，信用良好！</p>",
                                    buttons: [
                                        { text: "朕知道了", className: "default" },
                                        { text: "打赏一下", onClick: function(){ window.location.href='${ctx}/user/reward?userNo='+userNo; } },
                                    ]
                                });
                            }
                        }
                    }else{
                        $.alert("查询异常，请稍后重试");
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

        function ChangeTimestampToString(timestamp) {
            var d = new Date(timestamp);    //根据时间戳生成的时间对象
            var date = (d.getFullYear()) + "-" + (d.getMonth() + 1) + "-" + (d.getDate());
            return date;
        }

        function showDishonestyDetail(id){
            $.ajax({
                url:"${ctx}/api/loadDishonestyDetail",
                type:"GET",
                data:{"id":id},
                success:function(data){
                    if(!jQuery.isEmptyObject(data)){
                        var sx_name,sx_actionremark,sx_age,sx_anno,sx_executestatus,sx_publicdate,sx_ownername,sx_yiwu,sx_sexy,sx_isperson,sx_executegov;
                        sx_name = data.sx_name;
                        sx_actionremark = data.sx_actionremark;
                        sx_age = data.sx_age;
                        sx_anno = data.sx_anno;
                        sx_executestatus = data.sx_executestatus;
                        sx_publicdate = data.sx_publicdate;
                        sx_ownername = data.sx_ownername;
                        sx_executegov = data.sx_executegov;
                        sx_yiwu = data.sx_yiwu;
                        sx_sexy = data.sx_sexy;
                        sx_isperson = data.sx_isperson;
                        var ownernameText = "";
                        var sexyText = "";
                        var ageText = "";
                        /*if(sx_isperson=='0'){
                            //公司
                            ownernameText = "<p style='text-align: left;'><span style='font-weight: bold;'>所有者：</span><span>"+sx_ownername+"<span></p>";
                        }else{
                            //个人
                            if(!isNullOrEmpty(sx_sexy)){
                                sexyText = "<p style='text-align: left;'><span style='font-weight: bold;'>性别：</span><span>"+sx_sexy+"<span></p>";
                            }
                            if(sx_age!=0){
                                ageText = "<p style='text-align: left;'><span style='font-weight: bold;'>年龄：</span><span>"+sx_age+"<span></p>";
                            }
                        }*/
                        ownernameText = "<p style='text-align: left;'><span style='font-weight: bold;'>所有者：</span><span>"+sx_ownername+"<span></p>";
                        if(!isNullOrEmpty(sx_sexy)){
                            sexyText = "<p style='text-align: left;'><span style='font-weight: bold;'>性别：</span><span>"+sx_sexy+"<span></p>";
                        }
                        if(sx_age!=0){
                            ageText = "<p style='text-align: left;'><span style='font-weight: bold;'>年龄：</span><span>"+sx_age+"<span></p>";
                        }
                        $.modal({
                            title: "失信详情",
                            text: "<p style='text-align: left;'><span style='font-weight: bold;'>失信者：</span><span>"+sx_name+"<span></p>"+ownernameText+sexyText+ageText+
                            "<p style='text-align: left;'><span style='font-weight: bold;'>行为：</span><span>"+sx_actionremark+"<span></p>" +
                            "<p style='text-align: left;'><span style='font-weight: bold;'>执行机构：</span><span>"+sx_executegov+"<span></p>" +
                            "<p style='text-align: left;'><span style='font-weight: bold;'>义务：</span><span>"+sx_yiwu+"<span></p>" +
                            "<p style='text-align: left;'><span style='font-weight: bold;'>公布时间：</span><span>"+ChangeTimestampToString(sx_publicdate)+"<span></p>" +
                            "<p style='text-align: left;'><span style='font-weight: bold;'>执行结果：</span><span>"+sx_executestatus+"<span></p>" +
                            "<p style='text-align: left;'><span style='font-weight: bold;'>批号：</span><span>"+sx_anno+"<span></p>",
                            buttons: [
                                { text: "朕知道了", className: "default" },
                                { text: "打赏一下", onClick: function(){ window.location.href='${ctx}/user/reward?userNo=${userNo}'; } },
                            ]
                        });
                    }else{
                        $.alert("详细信息查询异常，请稍后重试");
                    }
                },
                error:function(){
                    $.alert("网络异常，请稍后重试");
                }
            })
        }

    </script>
</head>
<body>
<header class='demos-header'>
    <h1 class="demos-title" style="font-size: 24px;">失信查询</h1>
    <p class='demos-sub-title'>企业/个人失信、被执行信息检索</p>
</header>
<div>
    <div class="weui_cells">
        <div id="sxNameDiv" class="weui_cell">
            <div class="weui_cell_hd"><label class="weui_label">关键字</label></div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="sxName" name="sxName" class="weui_input" type="text" placeholder="请输入查询关键字">
            </div>
        </div>
    </div>
    <div class="weui_btn_area" style="margin-bottom: 40px;">
        <a href="javascript:dishonestyResult();"  class="weui_btn weui_btn_primary">确定</a>
    </div>
    <div class="weui_panel weui_panel_access" id="dishonestyResultDiv">
    </div>

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