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
    <title>我的邀请码</title>
    <style type="text/css">

    </style>
    <script type="text/javascript">
        $(function(){

        })

        function shareInviteCode(){
            $.alert("点击右上角操作按钮分享");
        }


    </script>
</head>
<body>
<div class="weui_msg">
    <div class="weui_icon_area"><i class="weui_icon_success weui_icon_msg"></i></div>
    <div class="weui_text_area">
        <p class="weui_msg_desc">您的专属邀请码为：</p>
        <h2 class="weui_msg_title">${inviteCode}</h2>
        <%--<p class="weui_msg_desc">该邀请码最多能邀请10人</p>--%>
    </div>
    <div class="weui_opr_area">
        <p class="weui_btn_area">
            <a href="javascript:shareInviteCode();" class="weui_btn weui_btn_primary">分享</a>
            <a href="javascript:history.back(-1);" class="weui_btn weui_btn_default">返回</a>
        </p>
    </div>
    <div class="weui_extra_area">
        <a href="">查看规则详情</a>
    </div>
</div>
</body>
</html>