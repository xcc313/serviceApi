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
    <title>打赏</title>
    <style type="text/css">

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
                        $.alert("打赏成功，感谢您的支持！",function(){
                            WeixinJSBridge.call('closeWindow');
                        });
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
                var rewardAmount = $.trim($("#rewardAmount").val());
                if(isNullOrEmpty(rewardAmount)){
                    $("#rewardAmountDiv").addClass("weui_cell_warn");
                    $.toast("请输入打赏金额", "forbidden");
                    return;
                }
                var INT_REG = /^(([1-9]{1}\d*)|([0]{1}))(\.(\d){0,2})?$/;
                if (!rewardAmount.match(INT_REG)) {
                    $.toast("请填写正确的金额", "forbidden");
                    return ;
                }
                var userNo = "${userMap.user_no}";
                if(userNo=="" || userNo==null || userNo=="null"){
                    $.toast("请重新进入", "forbidden");
                    return;
                }
                $.showLoading("打赏中...");
                $.ajax({
                    url:'${ctx}/wx/weixinRechargePay',
                    type:'POST',
                    data:{'userNo':userNo,'rechargeAmount':rewardAmount,"channel":1},
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
            window.location.href='${ctx}/user/inviteCode?userNo=${userMap.user_no}';
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
        <h1 class="demos-title" style="font-size: 24px;">打赏</h1>
        <p class='demos-sub-title'>感谢您的支持~!~</p>
    </header>
    <div class="weui_cells">
        <div id="rewardAmountDiv" class="weui_cell">
            <div class="weui_cell_hd"><label class="weui_label">打赏金额</label></div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="rewardAmount" name="rewardAmount" class="weui_input" type="text" placeholder="请输入金额">
            </div>
        </div>
    </div>
    <div class="weui_btn_area" style="margin-bottom: 25px;">
        <a href="javascript:toPay();"  class="weui_btn weui_btn_primary">确定</a>
    </div>

</body>
</html>