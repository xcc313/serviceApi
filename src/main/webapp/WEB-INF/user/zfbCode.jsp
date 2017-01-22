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
    <title>支付宝收款码</title>
    <style type="text/css">

    </style>
    <script type="text/javascript">
        $(function(){
            var zfbCodeUrl = "${zfbCodeUrl}";
            if(isNullOrEmpty(zfbCodeUrl)){
                $("#haveZFBCode").hide();
                $("#firstGetZFBCode").show();
            }else{
                $("#haveCodeImg").attr("src",zfbCodeUrl);
                $("#firstGetZFBCode").hide();
                $("#haveZFBCode").show();
            }
        })

        function getZFBCode(){
            $.showLoading("正在生成...");
            $.ajax({
                url:"${ctx}/user/getZFBCode",
                type:"GET",
                data:{"userNo":"${userMap.user_no}"},
                success:function(data){
                    $.hideLoading();
                    if(!jQuery.isEmptyObject(data)){
                        if(data.head.result_code == 'FAIL'){
                            $.alert(data.head.result_msg);
                        }else{
                            var zfbCodeUrl = data.content;
                            $("#codeImg").attr("src",zfbCodeUrl);
                            $("#linkGet").hide();
                            $("#codeImg").show();
                        }
                    }else{
                        $.alert("生成异常，请稍后重试");
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
        <h1 class="demos-title" style="font-size: 24px;">支付宝收款码</h1>
        <p class='demos-sub-title'>无第三方平台，支付宝直接交易结算，资金完全保障</p>
    </header>
    <article class="weui_article"id="firstGetZFBCode">
        <section>
            <h2 class="title" style="font-weight: bolder">首次使用，简单两步生成收款码：</h2>
            <section>
                <h3>1、打开支付宝APP，扫描下方邀请码，填写联系人手机和地址信息，成功开通支付宝收款</h3>
                <p style="text-align: center;">
                    <img id="headimgurl" src="${ctx}/images/zfb/scfw.png" <%--height="60px" width="60px"--%> />
                </p>
            </section>
            <section>
                <h3>2、紧接第一步，支付宝扫描专属激活码激活（<span style="color: red;font-weight: bolder;">激活后该激活码即为您的收款二维码</span>）</h3>
                <div id="jihuoDiv">
                    <a id="linkGet" href="javascript:getZFBCode();" class="weui_btn weui_btn_plain_primary" style="margin: 37px 0px">点击生成激活码</a>
                    <img id="codeImg" src="" style="display: none;" alt="支付宝收款码">
                </div>
            </section>
            <section>
                <h3 style="font-weight: bolder">注意事项：</h3>
                <p class="demos-sub-title" style="text-align: left;">1、支持信用卡，收取收款方收款费率：0.55%</p>
                <p class="demos-sub-title" style="text-align: left;">2、17年3月激活的用户，返收款方0.2%手续费，次月25号直接返到收款方支付宝帐号</p>
                <p class="demos-sub-title" style="text-align: left;">3、个人商家信用卡收款额度为5000元/日，企业商家不受影响</p>
            </section>
        </section>
    </article>

    <article class="weui_article"id="haveZFBCode">
        <section>
            <section>
                <div>
                    <img id="haveCodeImg" src="" alt="支付宝收款码">
                </div>
            </section>
            <section>
                <h3 style="font-weight: bolder">注意事项：</h3>
                <p class="demos-sub-title" style="text-align: left;">1、支持信用卡，收取收款方收款费率：0.55%</p>
                <p class="demos-sub-title" style="text-align: left;">2、17年3月激活的用户，返收款方0.2%手续费，次月25号直接返到收款方支付宝帐号</p>
                <p class="demos-sub-title" style="text-align: left;">3、个人商家信用卡收款额度为5000元/日，企业商家不受影响</p>
            </section>
        </section>
    </article>
</body>
</html>