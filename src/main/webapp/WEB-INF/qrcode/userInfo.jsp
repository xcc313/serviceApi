<%@ page language="java" import="com.lzj.op.*" contentType="text/html; charset=UTF-8"
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
    <title>二维码优汇</title>
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

        function refreshInfo(){
            $.ajax({
                url:"${ctx}/wx/refreshUserInfo",
                type:"GET",
                data:{"openid":"${userMap.openid}"},
                success:function(data){
                    if(!jQuery.isEmptyObject(data)){
                        var params = eval(data);
                        var nickname = params.nickname;
                        var headimgurl = params.headimgurl;
                        $("#headimgurl").attr("src",headimgurl);
                        $("#nickname").text(nickname);
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
            var nowLevel = '${nowLevel}';
            if(parseInt(nowLevel)<5){
                $.alert("等级低于5级,暂未获得邀请选权限");
            }else{
                window.location.href='${ctx}/user/inviteCode?userId=${userMap.id}';
            }
        }

    </script>
</head>
<body>
<%@include file="../layouts/head.jsp" %>
<header class='demos-header' style="padding: 20px 0;">
    <h1 class="demos-title"><img id="headimgurl" src="${userMap.headimgurl}" height="60px" width="60px" /></h1>
    <p class='demos-sub-title' id="nickname">${userMap.nickname}</p>
</header>
<div class="weui_grids">
    <a href="javascript:;" class="weui_grid js_grid" data-id="cell">
        <div class="weui_grid_icon">
            <img src="${ctx}/images/level/level-${nowLevel}.png" alt="等级">
        </div>
        <div class="weui_grid_label">
            <div class="weui_progress">
                <div class="weui_progress_bar" style="height: 8px;border-radius:20%;">
                    <div class="weui_progress_inner_bar js_progress" style="width: ${percent}%;border-top-left-radius:20%;border-bottom-left-radius:20%;"></div>
                </div>
                <p class="demos-sub-title" style="font-size: 14px;">${userMap.exper_num}/${levelMap.level_exper_end+1}</p>
            </div>
        </div>
    </a>
    <%--<a href="javascript:;" class="weui_grid js_grid" data-id="cell">
        <div class="weui_grid_icon">
            <img src="${ctx}/images/balance-1.png" alt="余额">
        </div>
        <p class="weui_grid_label">
            余额:${userMap.balance}
        </p>
    </a>--%>
    <a href="javascript:;" class="weui_grid js_grid" data-id="cell">
        <div class="weui_grid_icon">
            <img src="${ctx}/images/lu_coin.png" alt="卢币">
        </div>
        <p class="weui_grid_label">
            卢币:${userMap.lu_coin }
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


    <%--<a href="javascript:void(0);" class="weui_grid js_grid" data-id="cell">
        <div class="weui_grid_icon">
            <img src="${ctx}/images/auth-3.png" alt="认证">
        </div>
        <c:if test="${userMap.is_auth eq 1 }">
            <p class="weui_grid_label">
                认证ID:${userMap.auth_id }
            </p>
        </c:if>
        <c:if test="${userMap.is_auth ne 1 }">
            <p class="weui_grid_label" onclick="onDevelop()">
                去认证
            </p>
        </c:if>
    </a>--%>

    <a href="javascript:toInviteCode()" class="weui_grid js_grid" data-id="cell">
        <div class="weui_grid_icon">
            <img src="${ctx}/images/inviteCode.png" alt="我的邀请码">
        </div>
        <p class="weui_grid_label">
            我的邀请码
        </p>
    </a>

    <a href="javascript:window.location.href='${ctx}/qrcode/toPublishQrcode?userId=${userMap.id}';" class="weui_grid js_grid" data-id="cell">
        <div class="weui_grid_icon">
            <img src="${ctx}/images/publish.png" alt="发布二维码">
        </div>
        <p class="weui_grid_label">
            发布二维码
        </p>
    </a>

    <a href="javascript:window.location.href='${ctx}/qrcode/userQrCode?userId=${userMap.id}';" class="weui_grid js_grid" data-id="cell">
        <div class="weui_grid_icon">
            <img src="${ctx}/images/qrcode-1.png" alt="我的二维码">
        </div>
        <p class="weui_grid_label">
            我的二维码
        </p>
    </a>

</div>
</body>
</html>