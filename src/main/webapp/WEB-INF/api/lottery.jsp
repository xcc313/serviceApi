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
    <title>中奖查询</title>
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

        function lotteryResult(){
            var userNo = "${userNo}";
            var lotteryType = $.trim($('#lotteryType').val());
            var expect = $.trim($('#expect').val());
            if(isNullOrEmpty(lotteryType)){
                $("#lotteryTypeDiv").addClass("weui_cell_warn");
                //$.alert("请选择彩票种类");
                $.toast("请选择彩票种类", "forbidden");
                return;
            }
            $.modal({
                title: "确认提示",
                text: "此查询将消耗2微币",
                buttons: [
                    { text: "确认查询", className:"surePayText", onClick: function(){ sureCheck(lotteryType,expect); } },
                    { text: "微币充值", onClick: function(){ /*window.location.href='${ctx}/user/rechargeCoin?userNo='+userNo*/ $("#rechargePopup").popup(); } },
                    { text: "取消", className: "default", onClick: function(){ } },
                ]
            });

        }

        function sureCheck(lotteryType,expect){
            var userNo = "${userNo}";
            $.showLoading("正在查询...");
            $.ajax({
                url:"${ctx}/api/lotteryResult",
                type:"GET",
                data:{"lotteryName":lotteryType,"expect":expect,"userNo":userNo},
                success:function(data){
                    $.hideLoading();
                    if(!jQuery.isEmptyObject(data)){
                        //var params = eval(data);
                        if(data.head.result_code == 'FAIL'){
                            $.alert(data.head.result_msg);
                        }else{
                            $.modal({
                                title: "第"+data.content.expect+"期"+data.content.lottery_name+"开奖结果",
                                text: data.content.open_code,
                                buttons: [
                                    { text: "朕知道了", className: "default" },
                                    { text: "打赏一下", onClick: function(){ window.location.href='${ctx}/user/reward?userNo=${userNo}'; } },
                                ]
                            });
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
    <h1 class="demos-title" style="font-size: 24px;">中奖查询</h1>
    <p class='demos-sub-title'>给未来一点未知，给生活一点期待</p>
</header>
<div>
    <div class="weui_cells">
        <div id="lotteryTypeDiv" class="weui_cell">
            <div class="weui_cell_hd"><label class="weui_label">彩种</label></div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="lotteryType" name="lotteryType" class="weui_input" type="text" placeholder="请选择种类"  readonly>
                <script>
                    $("#lotteryType").picker({
                        title: "请选择种类",
                        cols: [
                            {
                                textAlign: 'center',
                                values: ['双色球','超级大乐透','福彩3D', '排列3','排列5','七乐彩','七星彩']
                            }
                        ],
                        onClose:function closePicker(){
                            var lotteryName = $.trim($('#lotteryType').val());
                            $.ajax({
                                url:"${ctx}/api/loadLotteryHistory",
                                type:"GET",
                                data:{"lotteryName":lotteryName},
                                success:function(data){
                                    if(!jQuery.isEmptyObject(data)){
                                        $("#lotteryHistoryDiv").html("");
                                        $("#lotteryHistoryDiv").append("<div class=\"weui_cell\"><div class=\"weui_cell_bd weui_cell_primary\"><p style=\"color: #888;\">历史开奖记录</p> </div> <div class=\"weui_cell_ft\" style=\"color: black;\">"+lotteryName+" </div> </div>");
                                        var item,expect,open_code;
                                        for(var i=0, l=data.length; i<l; i++) {
                                            item = data[i];
                                            expect = item.expect;
                                            open_code = item.open_code;
                                            $("#lotteryHistoryDiv").append("<div class=\"weui_cell\"> <div class=\"weui_cell_bd weui_cell_primary\"> <p style=\"color: #888;\">"+expect+"</p> </div> <div class=\"weui_cell_ft\">"+open_code+" </div> </div>");
                                        }

                                    }else{
                                        $("#lotteryHistoryDiv").html("");
                                    }
                                },
                                error:function(){
                                    $.alert("网络异常，请稍后重试");
                                }
                            })
                        }
                    });

                </script>
            </div>
        </div>
        <div id="expectDiv" class="weui_cell">
            <div class="weui_cell_hd"><label class="weui_label">期数</label></div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="expect" name="expect" class="weui_input" type="text" placeholder="不填则查最新">
            </div>
        </div>
    </div>
    <div class="weui_btn_area" style="margin-bottom: 40px;">
        <a href="javascript:lotteryResult();"  class="weui_btn weui_btn_primary">确定</a>
    </div>
    <div class="weui_cells" id="lotteryHistoryDiv">
        <div class="weui_cell">
            <div class="weui_cell_bd weui_cell_primary">
                <p style="color: #888;">历史开奖记录</p>
            </div>
            <div class="weui_cell_ft" style="color: black;">
                双色球
            </div>
        </div>
        <c:forEach items="${lotteryList}" var="lotteryMap">
            <div class="weui_cell">
                <div class="weui_cell_bd weui_cell_primary">
                    <p style="color: #888;">${lotteryMap.expect}</p>
                </div>
                <div class="weui_cell_ft">
                        ${lotteryMap.open_code}
                </div>
            </div>
        </c:forEach>
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

</div>

</body>
</html>