<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="user-scalable=no, width=device-width, initial-scale=1.0" />
    <c:set var="ctx" value="${pageContext.request.contextPath}" />
    <script type="text/javascript" src="${ctx}/js/jquery-1.9.1.min.js" charset="utf-8"></script>
    <link href="${ctx}/css/weui.min.css" rel="stylesheet">
    <link href="${ctx}/css/jquery-weui.min.css" rel="stylesheet">
    <link href="${ctx}/css/demos.css" rel="stylesheet">
    <%--<script src="${ctx}/js/jquery-weui-0.7.2.min.js"></script>--%>
    <script src="${ctx}/js/jquery-weui.min.js" charset="utf-8"></script>
    <%--<script src="${ctx}/js/city-picker-0.7.2.min.js" charset="utf-8"></script>--%>
    <script src="${ctx}/js/city-picker.min.js" charset="utf-8"></script>
    <title>邮编查询</title>
    <style type="text/css">
        .surePayText{
            color: #fd8d2c !important;
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

        function zipcodeResult(){
            var cityPicker = $('#city-picker').val();
            var searchKey = $.trim($('#searchKey').val());
            var userNo = "${userNo}";
            if(isNullOrEmpty(cityPicker)){
                $("#cityPickerDiv").addClass("weui_cell_warn");
                $.toast("请选择省市区", "forbidden");
                return;
            }
            if(isNullOrEmpty(searchKey)){
                $("#searchKeyDiv").addClass("weui_cell_warn");
                $.toast("请输入关键词", "forbidden");
                return;
            }
            $.modal({
                title: "确认提示",
                text: "此查询将消耗2微币",
                buttons: [
                    { text: "确认查询", className:"surePayText", onClick: function(){ sureCheck(cityPicker,searchKey); } },
                    { text: "微币充值", onClick: function(){ /*window.location.href='${ctx}/user/rechargeCoin?userNo='+userNo*/ $("#rechargePopup").popup(); } },
                    { text: "取消", className: "default", onClick: function(){ } },
                ]
            });

        }

        function sureCheck(cityPicker,searchKey){
            var userNo = "${userNo}";
            $.showLoading("正在查询...");
            $.ajax({
                url:"${ctx}/api/zipcodeResult",
                type:"GET",
                data:{"cityPicker":cityPicker,"searchKey":searchKey,"userNo":userNo},
                success:function(data){
                    $.hideLoading();
                    if(!jQuery.isEmptyObject(data)){
                        if(data.head.result_code == 'FAIL'){
                            $.alert(data.head.result_msg);
                        }else{
                            var content = data.content;
                            if(!jQuery.isEmptyObject(content)){
                                $("#zipcodeResultDiv").html("");
                                $("#zipcodeResultDiv").append("<div class=\"weui_panel_hd\" style=\"text-align: center;font-size: 17px;\">查询结果:"+cityPicker+" "+searchKey+"</div><div class=\"weui_panel_bd\">");
                                var id,item,province,city,town,address,zipcode;
                                for(var i=0, l=content.length; i<l; i++) {
                                    item = content[i];
                                    id = item.id;
                                    province = item.province;
                                    city = item.city;
                                    town = item.town;
                                    address = item.address;
                                    zipcode = item.zipcode;
                                    $("#zipcodeResultDiv").append("<div class=\"weui_media_box weui_media_text\"> <h4 class=\"weui_media_title\">邮编："+zipcode+"</h4> <p class=\"weui_media_desc\">地址："+province+city+town+address+"</p> </div>");
                                }
                                $("#zipcodeResultDiv").append("</div>");

                            }else{
                                $.modal({
                                    title: "查询结果:"+cityPicker+" "+searchKey,
                                    text: "<p style='color:#0bb20c;'>没有信息！</p>",
                                    buttons: [
                                        { text: "朕知道了", className: "default" },
                                        { text: "打赏一下", onClick: function(){ window.location.href='${ctx}/user/reward?userNo=${userNo}'; } },
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

    </script>
</head>
<body>
<header class='demos-header'>
    <h1 class="demos-title" style="font-size: 24px;">邮编查询</h1>
    <p class='demos-sub-title'>全国30多个省市县的邮编号码查询,数据权威准确</p>
</header>
<div>
    <div class="weui_cells">
        <div id="cityPickerDiv" class="weui_cell">
            <div class="weui_cell_hd"><label class="weui_label">省市区</label></div>
            <div class="weui_cell_bd weui_cell_primary">
                <input type="text" id='city-picker' name="city-picker" class="weui_input" value="广东省 深圳市 南山区" />
                <script>
                    $("#city-picker").cityPicker({
                        title: "请选择省市区"
                    });
                </script>
            </div>
        </div>
        <div id="searchKeyDiv" class="weui_cell">
            <div class="weui_cell_hd"><label class="weui_label">地址关键词</label></div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="searchKey" name="searchKey" class="weui_input" type="text" placeholder="请输入详细地址关键词">
            </div>
        </div>
    </div>
    <div class="weui_btn_area" style="margin-bottom: 40px;">
        <a href="javascript:zipcodeResult();"  class="weui_btn weui_btn_primary">确定</a>
    </div>
    <div class="weui_panel weui_panel_access" id="zipcodeResultDiv">
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