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
	<script src="${ctx}/js/jquery-weui.min.js"></script>
<title>二维码优汇</title>
	<style type="text/css">
		.fontCss{
			font-family: '微软雅黑';
			color:#666;
		}
		.liImage{
			margin:2% 2% ;
			list-style:none;
			height: 80px;
		}
		.liFont{
			overflow:hidden;
			text-overflow:ellipsis;
			white-space:nowrap;
		}
	</style>
<script type="text/javascript">
var zxPage = 1;
$(function(){
	var qrcodeListNum = '${fn:length(qrCodeMapList)}';
	var userId = '${userMap.id}';
	if(qrcodeListNum>8){
		var loading = false;  //状态标记
		$(document.body).infinite(100).on("infinite", function() {
			if(loading) return;
			loading = true;
			setTimeout(function() {
				zxPage++;
				$.ajax({
					url : '${ctx}/qrcode/loadUserCodeData',
					data:"index="+zxPage+"&userId="+userId,
					async: false,
					dataType : 'json',
					cache: true, //设置缓存
					success: function(json) {
						if (json!=null && json.length > 0) {
							var state = {
								title: "二维码优汇22",
								url: "${ctx}/qrcode/userQrCode?userId=55&index="+zxPage
							};
							window.history.pushState({}, document.title, "${ctx}/qrcode/userQrCode?userId="+userId+"&index="+zxPage);
							//window.history.pushState(document.title, "${ctx}/qrcode/userQrCode?userId=66&index="+zxPage);
							//window.history.replaceState("123123", "${ctx}/qrcode/userQrCode?userId=1&index="+zxPage);
							var items = [];
							var item,id,mainPicture,qrcodeTitle,qrcodeType,viewNum;
							for(var i=0, l=json.length; i<l; i++){
								item = json[i];
								id = item.id;
								mainPicture = item.main_picture;
								qrcodeTitle = item.qrcode_title;
								qrcodeType = item.qrcode_type;
								viewNum = item.view_num;
								$("#qrCodeListDiv").append("<ul class=\"weui-row weui-no-gutter\" style=\"border-bottom:1px solid #BCBCBC;\" onclick=\"toQrDetail('"+id+"')\">" +
								"<li class=\"weui-col-40 liImage\" style=\"width:35%;background:url('${ctx}/upload/"+userId+"/"+mainPicture+"') no-repeat 50%;background-size: cover;\"></li>" +
								"<li class=\"weui-col-40 liImage\"><p class=\"liFont\">类别:"+qrcodeType+"</p><p class=\"liFont\">标题:"+qrcodeTitle+"</p><p class=\"liFont\">浏览量:"+viewNum+"</p></li>" +
								"<li class=\"weui-col-10 liImage\" style=\"background:url('${ctx}/images/arrow.png') center no-repeat;background-size: 100%;\"></li></ul>");
								/*
								$("#qrCodeListDiv").append("<li class=\"weui-col-40 liImage\" style=\"width:35%;background:url('${ctx}/upload/"+userId+"/"+mainPicture+"') no-repeat 50%;background-size: cover;\"></li>");
								$("#qrCodeListDiv").append("<li class=\"weui-col-40 liImage\"><p class=\"liFont\">类别2:"+qrcodeType+"</p><p class=\"liFont\">标题:"+qrcodeTitle+"</p><p class=\"liFont\">浏览量:"+viewNum+"</p></li>");
								$("#qrCodeListDiv").append("<li class=\"weui-col-10 liImage\" style=\"background:url('${ctx}/images/arrow.png') center no-repeat;background-size: 100%;\"></li></ul>");
							    */
							}
							loading = false;
						} else {
							$(document.body).destroyInfinite();  //销毁插件
							$("#loadingDiv").css("display","none");
							return;
						}
					},
					error: function() {
						$.alert('请求数据失败，请稍后再试');
						$(document.body).destroyInfinite();  //销毁插件
						$("#loadingDiv").css("display","none");
					}
				});//end ajax
			}, 1500);   //模拟延迟
		});
	}else{
		$(document.body).destroyInfinite();  //销毁插件
		$("#loadingDiv").css("display","none");
	}

})

function toQrDetail(id){
	window.location.href = "${ctx}/qrcode/qrcodeDesc?qrcodeId="+id;
}
	
</script>
</head>
<body>
<%@include file="../layouts/head.jsp" %>
<div>
	<div class="content-padded" id="qrCodeListDiv">
		<c:forEach items="${qrCodeMapList }" var="qrCodeMap">
			<ul class="weui-row weui-no-gutter" style="border-bottom:1px solid #BCBCBC;" onclick="toQrDetail('${qrCodeMap.id}')">
				<li class="weui-col-40 liImage" style="width:35%;background:url('${ctx}/upload/${qrCodeMap.publisher_id}/${qrCodeMap.main_picture}') no-repeat 50%;background-size: cover;">
				</li>
				<li class="weui-col-40 liImage">
					<p class="liFont">类别:${qrCodeMap.qrcode_type }</p>
					<p class="liFont">标题:${qrCodeMap.qrcode_title }</p>
					<p class="liFont">浏览量:${qrCodeMap.view_num }</p>
				</li>
				<li class="weui-col-10 liImage" style="background:url('${ctx}/images/arrow.png') center no-repeat;background-size: 100%;">
				</li>
			</ul>
		</c:forEach>
	</div>
	<div class="weui-infinite-scroll" id="loadingDiv">
		<div class="infinite-preloader"></div>
		正在加载...
	</div>
</div>
</body>
</html>