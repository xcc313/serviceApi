<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
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
    <title>个人中心</title>
    <style type="text/css">

    </style>
    <script type="text/javascript">
        $(function(){
            $("#showDialog1").click(function(){
                //审核通过
                $.confirm("刷新个人信息，如头像，名称等请按确定", function() {
                    refreshInfo();
                }, function() {
                    //$.alert("你点击了取消按钮");
                });

            });
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
                        document.location.reload();
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
                var userNo = "${userMap.user_no}";
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

        function refreshInfo(){
            $.ajax({
                url:"${ctx}/user/refreshUserInfo",
                type:"GET",
                data:{"openid":"${userMap.openid}"},
                success:function(data){
                    if(!jQuery.isEmptyObject(data)){
                        var params = eval(data);
                        var nickname = params.nickname;
                        var headimgurl = params.headimgurl;
                        $("#headimgurl").attr("src",headimgurl);
                        $("#nickname").text(nickname);
                        $.alert("更新成功");
                    }else{
                        $.alert("更新异常，请稍后重试");
                    }
                },
                error:function(){
                    $.alert("网络异常，请稍后重试");
                }
            })
        }

        function onDevelop(){
            $.alert("正在开发中...");
        }

        function toInviteCode(){
            //window.location.href='${ctx}/user/inviteCode?userNo=${userMap.user_no}';
            $.alert("正在开发中...");
        }

        function toZFBCode(){
            window.location.href='${ctx}/user/zfbCode?userNo=${userMap.user_no}';
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
    <header class='demos-header' style="padding: 20px 0;">
        <h1 class="demos-title"><img id="headimgurl" src="${userMap.headimgurl}" height="60px" width="60px" /></h1>
        <p class='demos-sub-title' id="nickname">${userMap.nickname}</p>
    </header>
    <div class="weui_grids">
        <a href="javascript:;" class="weui_grid js_grid open-popup" data-id="cell" data-target="#rechargePopup">
            <div class="weui_grid_icon">
                <img src="${ctx}/images/lu_coin.png" alt="微币">
            </div>
            <p class="weui_grid_label">
                微币:${userMap.wei_coin }
            </p>
        </a>
        <a href="javascript:void(0);" class="weui_grid js_grid" data-id="cell" id="showDialog1">
            <div class="weui_grid_icon">
                <img src="${ctx}/images/refresh-1.png" alt="刷新个人信息">
            </div>
            <p class="weui_grid_label">
                个人信息更新
            </p>
        </a>
        <%--<a href="javascript:toInviteCode()" class="weui_grid js_grid" data-id="cell">
            <div class="weui_grid_icon">
                <img src="${ctx}/images/inviteCode.png" alt="我的邀请码">
            </div>
            <p class="weui_grid_label">
                我的邀请码
            </p>
        </a>--%>
        <a href="javascript:toZFBCode()" class="weui_grid js_grid" data-id="cell">
            <div class="weui_grid_icon">
                <img src="${ctx}/images/zfb.png" alt="支付宝收款码">
            </div>
            <p class="weui_grid_label">
                我的收款码
            </p>
        </a>
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
            <a href="${ctx}/user/rechargeCoin?userNo=${userMap.user_no}">查看微币规则详情</a>
        </div>
    </div>
<%--
    <div style="position:fixed;bottom:0;">
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
        <div class="weui_msg">
            <div class="weui_extra_area">
                <a href="">查看微币规则详情</a>
            </div>
        </div>
    </div>--%>
</body>
</html>