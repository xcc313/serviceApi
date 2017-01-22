<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="user-scalable=yes, width=device-width, initial-scale=1.0" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<script type="text/javascript" src="${ctx}/js/jquery-1.9.1.min.js"></script>
	<script src="${ctx}/js/jquery-weui.min.js"></script>
	<link href="${ctx}/css/weui.min.css" rel="stylesheet">
	<link href="${ctx}/css/jquery-weui.min.css" rel="stylesheet">
<title>二维码优汇</title>
	<style type="text/css">
		.borderCss{
			border: 1px solid #BCBCBC;
		}
		.codeTitle{
			overflow:hidden;
			text-overflow:ellipsis;
			white-space:nowrap;
		}
	</style>
<script type="text/javascript">
$(function(){

})
</script>
</head>
<body>
<%@include file="../layouts/head.jsp" %>
<div style="margin-top: 20px;">
	<div class="weui_cells">
		<div class="weui_cell">
			<div class="weui_cell_bd" style="width: 85px;">
				<p style="width: 85px;">【${qrcodeMap.qrcode_type}】</p>
			</div>
			<div class="weui_cell_ft codeTitle" style="text-align: left;color: #080808">
				${qrcodeMap.qrcode_title}
			</div>
		</div>
		<div class="weui_cell">
			<div class="weui-row weui-no-gutter">
				<div class="weui_cell_hd">
					<img src="${ctx}/images/time.png" alt="icon" style="width:20px;margin-right:5px;display:block">
				</div>
				<div class="weui_cell_ft">
					${qrcodeMap.publishTime}
				</div>
				<div class="weui_cell_hd" style="padding-left: 10px;">
					<img src="${ctx}/images/browse.png" alt="icon" style="width:20px;margin-right:5px;display:block">
				</div>
				<div class="weui_cell_ft">
					${qrcodeMap.view_num}
				</div>
			</div>
		</div>
	</div>
	<div class="weui-row weui-no-gutter" style="padding-left: 10px;padding-right: 10px;padding-top: 20px;">
		<div class="weui-col-100" style="text-align: center;">
			<p>加我时请说是从二维码优汇中看到的</p>
			<img src="${ctx}/upload/${qrcodeMap.publisherId}/${qrcodeMap.qrcode_url}" style="max-width: 300px;" alt="微信群二维码集合"/>
		</div>
	</div>
	<div class="weui_cells">
		<div class="weui_cell">
			<div class="weui_cell_bd weui_cell_primary" style="min-width: 20%">
				<p>标题</p>
			</div>
			<div class="weui_cell_ft codeTitle">
				${qrcodeMap.qrcode_title}
			</div>
		</div>
		<div class="weui_cell">
			<div class="weui_cell_bd weui_cell_primary">
				<p>类别</p>
			</div>
			<div class="weui_cell_ft">
				${qrcodeMap.qrcode_type}
			</div>
		</div>
		<div class="weui_cell">
			<div class="weui_cell_bd weui_cell_primary">
				<p>标签</p>
			</div>
			<div class="weui_cell_ft">
				${qrcodeMap.category_code}
				.
			</div>
		</div>
		<div class="weui_cell">
			<div class="weui_cell_bd weui_cell_primary">
				<p>城市</p>
			</div>
			<div class="weui_cell_ft">
				${qrcodeMap.city}
			</div>
		</div>
		<div class="weui_cell">
			<div class="weui_cell_bd weui_cell_primary">
				<p>发布者微信号</p>
			</div>
			<div class="weui_cell_ft">
				${qrcodeMap.weixinhao}
			</div>
		</div>
		<div class="weui_cell">
			<div class="weui_cell_bd weui_cell_primary">
				<p>发布时间</p>
			</div>
			<div class="weui_cell_ft">
				${qrcodeMap.publishDescTime}
			</div>
		</div>
		<div class="weui_cell" style="padding-top: 0px;padding-bottom: 0px;;"></div>
		<div class="weui_cells_title" style="color: black">详细描述</div>
		<div class="weui_cells weui_cells_form" style="border-top: 0px !important;">
			<div class="weui_cell">
				<div class="weui_cell_bd weui_cell_primary">
					<div class="weui_textarea_counter">加我时请说是从二维码优汇中看到的。${qrcodeMap.qrcode_desc}</div>
				</div>
			</div>
		</div>
	</div>
</div>

<div style="height: 50px;"></div>

</body>
</html>