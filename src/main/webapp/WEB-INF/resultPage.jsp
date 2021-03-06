<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
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
  <title>成功</title>
  <script type="text/javascript">
    function closeWin(){
      WeixinJSBridge.call('closeWindow');
    }
  </script>
</head>
<body>
<div class="weui_msg">
  <div class="weui_icon_area">
    <c:if test="${result eq 'SUCCESS'}">
      <i class="weui_icon_success weui_icon_msg"></i>
    </c:if>
    <c:if test="${result ne 'SUCCESS'}">
      <i class="weui_icon_warn weui_icon_msg"></i>
    </c:if>
  </div>
  <div class="weui_text_area">
    <h2 class="weui_msg_title">${resultTitle}</h2>
    <p class="weui_msg_desc" id="autoClose">${resultDesc}</p>
  </div>
  <div class="weui_opr_area">
    <p class="weui_btn_area">
      <a href="javascript:closeWin();" class="weui_btn weui_btn_primary">关闭</a>
    </p>
  </div>
</div>
</body>
</html>
