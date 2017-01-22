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
<script src="${ctx}/js/js.cookie.js"></script>
	<link href="${ctx}/css/weui.min.css" rel="stylesheet">
	<link href="${ctx}/css/jquery-weui.min.css" rel="stylesheet">
	<script src="${ctx}/js/jquery-weui.min.js"></script>
	<title>二维码优汇</title>
	<style type="text/css">
		.fontCss{
			font-size: 14px;
			font-family: '微软雅黑';
			color:#666;
		}
		.liImage{
			width:46%;
			margin:2% 2% ;
			list-style:none;
			height: 200px;
		}
		.liFont{
			margin-top: 174px;
			height:26px;
			text-align: center;
			background-color:black;
			color: white;
			overflow:hidden;
			text-overflow:ellipsis;
			white-space:nowrap;
		}
	</style>
	<script type="text/javascript">
		var zxPage = 1;
		$(function(){
			var qrcodeListNum = '${fn:length(qrCodeList)}';
			var categoryCode = $("#categoryCode").val();
			if(qrcodeListNum>8){
				var loading = false;  //状态标记
				$(document.body).infinite(100).on("infinite", function() {
					if(loading) return;
					loading = true;
					setTimeout(function() {
						$.ajax({
							url : '${ctx}/qrcode/loadData',
							data:"index="+zxPage+"&categoryCode="+categoryCode,
							async: false,
							dataType : 'json',
							//cache: true, //设置缓存
							success: function(json) {
								if (json!=null && json.length > 0) {
									var items = [];
									var item,id,mainPicture,qrcodeTitle,createTime,qrcodeDesc,isTop,isAuth,city,publisherId;
									for(var i=0, l=json.length; i<l; i++){
										item = json[i];
										id = item.id;
										mainPicture = item.main_picture;
										qrcodeTitle = item.qrcode_title;
										createTime = item.create_time;
										qrcodeDesc = item.qrcode_desc;
										isTop = item.is_top;
										isAuth = item.is_auth;
										city = item.city;
										publisherId = item.publisher_id
										$("#imageList").append("<li class=\"liImage\" onclick=\"toQrDetail('"+id+"')\" style=\"background:url('${ctx}/upload/"+publisherId+"/"+mainPicture+"') no-repeat 50%;background-size: cover;\"><p class=\"liFont\">"+qrcodeTitle+"</p></li>");
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
						zxPage++;
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
<div id="codeDiv">
	<div class="content-padded">
		<div  style="display:none;"><input id="categoryCode" type="text" value="${categoryCode }" disabled="disabled"></div>
		<ul class="weui-row" id="imageList">
			<c:forEach items="${qrCodeList}" var="qrCodeMap">
				<li class="liImage" onclick="toQrDetail('${qrCodeMap.id}')" style="background:url('${ctx}/upload/${qrCodeMap.publisher_id}/${qrCodeMap.main_picture}') no-repeat 50%;background-size: cover;">
					<p class="liFont">${qrCodeMap.qrcode_title}</p>
				</li>
			</c:forEach>
		</ul>
	</div>
	<div class="weui-infinite-scroll" id="loadingDiv">
		<div class="infinite-preloader"></div>
		正在加载...
	</div>
</div>


<%--<div class="weui_tab" id='page-infinite-navbar'>
	<div class="weui_navbar">
		<a href='#tab1' class="weui_navbar_item weui_bar_item_on">
			首页
		</a>
		<a href='http://www.baidu.com' class="weui_navbar_item">
			选项二
		</a>
	</div>
	<div class="weui_tab_bd">
		<div id="tab1" class="weui_tab_bd_item weui_tab_bd_item_active">
			&lt;%&ndash;<h1 class="doc-head">页面一</h1>&ndash;%&gt;
			<div class="content-padded">
				<div  style="display:none;"><input id="categoryCode" type="text" value="${categoryCode }" disabled="disabled"></div>
				<ul class="weui-row" id="imageList">
					<c:forEach items="${qrCodeList}" var="qrCodeMap">
						<li class="liImage" style="background:url('${ctx}/upload/${publisherId}/${qrCodeMap.main_picture}') no-repeat 50%;background-size: cover;"></li>
					</c:forEach>
				</ul>
			</div>
			<div class="weui-infinite-scroll" id="loadingDiv">
				<div class="infinite-preloader"></div>
				正在加载...
			</div>
		</div>
	</div>
</div>--%>
</body>
</html>