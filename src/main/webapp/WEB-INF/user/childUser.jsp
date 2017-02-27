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
    <title>我的下级</title>
    <style type="text/css">
    </style>
    <script type="text/javascript">

    </script>
</head>
<body style="font-family: 微软雅黑;">
<div>
    <div class="weui_panel weui_panel_access">
        <div class="weui_panel_hd">下级用户(只显示下一级)</div>
        <div class="weui_panel_bd" id="historyDiv">
            <c:forEach items="${childUserList}" var="childUserMap">
            <div class="weui_media_box weui_media_text">
                <div class="weui_media_title">
                    <span style="float: left;">${childUserMap.real_name}</span>
                </div>
                <div class="weui_media_desc">
                    <span style="">手机号:${childUserMap.mobile_no}</span>
                    <span style="float: right;">商户名:${childUserMap.merchant_name}</span>
                </div>
            </div>
            </c:forEach>
        </div>
    </div>
</div>

</body>
</html>