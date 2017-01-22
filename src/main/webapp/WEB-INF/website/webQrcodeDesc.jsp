<%@ page language="java"  contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>二维码集合</title>
	<meta name="keywords" content="二维码,福利群,红包群,二维码集合,微信群,微群二维码集合,二维码优汇" />
	<meta name="description" content="这里有福利群、红包群、驴友群、股票群……，自由发布，自由分享二维码，这里是微信群二维码集合" />
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<c:set var="ctx" value="${pageContext.request.contextPath}" />
	<link rel="shortcut icon" type="image/x-icon" href="${ctx}/images/mainIcon.ico" />
	<link href="${ctx}/css/font-awesome.min.css" rel="stylesheet" type="text/css">
	<link href="${ctx}/css/magnific-popup.css" rel="stylesheet">
	<link href="${ctx}/css/templatemo_style.css" rel="stylesheet" type="text/css">

	<script type="text/javascript" src="${ctx}/js/jquery-1.9.1.min.js"></script>
	<script type="text/javascript" src="${ctx}/js/jquery.easing.1.3.js"></script>
	<script type="text/javascript" src="${ctx}/js/modernizr-2.6.2.min.js"></script>
	<script type="text/javascript" src="${ctx}/js/jquery.magnific-popup.min.js"></script>
	<script type="text/javascript" src="${ctx}/js/templatemo_script.js"></script>
	<style type="text/css">
		.tagCss{
			font-family: '微软雅黑';
			color:#666;
			font-size: 13px;
		}
		#newQrcodeDiv span{
			font-family: '微软雅黑';
			color:#666;
			font-size: 13px;
		}
		.descTitle{
			font-size: 18px;
			font-family: '微软雅黑';
			color:#666;
			font-weight:bold;
		}
		.descContext{
			font-size: 16px;
			font-family: '微软雅黑';
			color:#666;
			overflow:hidden;
			text-overflow:ellipsis;
		}
		#recommendDiv span{
			font-family: '微软雅黑';
			color:#666;
			font-size: 13px;
		}
	</style>

	<script type="text/javascript">

		$(function () {
			$('#recommendPage li').click(function recommendChangePage(event) {
				var pageNo = $(this).html();

				$('.portfolio-page2').hide();
				$('#page2-' + pageNo).fadeIn();
				$('#recommendPage li').removeClass('active');
				$(this).addClass('active');
			});
		});

		var qrcodeId;
		function toQrcodeDesc(qrcodeId){
			window.open("${ctx}/website/codeDesc?id="+qrcodeId);
		}

	</script>

</head>
<body>
	<div class="main-container" style="background-color: #eeeeee;box-shadow:0 0 0px rgba(107, 110, 112, 0.4);">
		<%@include file="../layouts/websiteHead.jsp" %>
		<div class="content-container" style="margin-top: 15px;box-sizing:inherit;width:1200px;padding:0px;max-width: 1200px;">
			<div style="padding: 0px 15px;background-color: white;width: 1170px;display: inline-block;height: 100%;vertical-align: middle;">
				<span>
					<span style="font-weight: bold;font-family: '微软雅黑';color:#666;font-size: 14px;">您的位置：</span>
					<span style="font-family: '微软雅黑';color:#666;">首页 > </span>
					<span style="font-family: '微软雅黑';color:#666;">${qrcodeMap.qrcode_title}</span>
				</span>
			</div>

			<div id="" style="width: 800px;height: 518px;margin-top: 10px;background-color: white;float: left;box-shadow:0 0 5px rgba(107, 110, 112, 0.4);">
				<div style="text-align: center;vertical-align: middle;background-color: #169ADA;height: 40px;width: 100px;">
					<span class="tagCss" style="color: white;line-height: 40px;">${qrcodeMap.qrcode_type}</span>
				</div>
				<div class="" style="box-shadow:0 0 0px rgba(107, 110, 112, 0.4);">
					<div style="float: left;padding: 10px 10px;width: 420px;">
						<img src="${ctx}/upload/${qrcodeMap.publisher_id}/${qrcodeMap.qrcode_url}" alt="二维码集合" style="height: 400px;width: 400px;">
					</div>
					<div style="float: left;padding: 10px 10px;width: 300px;">
						<div style="height:50px;"><span class="descTitle">标题:</span><span class="descContext">${qrcodeMap.qrcode_title}</span></div>
						<div style="height:50px;"><span class="descTitle">二维码类型:</span><span class="descContext">${qrcodeMap.qrcode_type}</span></div>
						<div style="height:50px;"><span class="descTitle">发布者微信号:</span><span class="descContext">${qrcodeMap.weixinhao}</span></div>
						<div style="height:50px;"><span class="descTitle">总浏览量:</span><span class="descContext">${qrcodeMap.view_num}</span></div>
						<div style="height:200px;overflow:hidden;"><span class="descTitle">详细介绍:</span><span class="descContext" style="font-size: 14px;">${qrcodeMap.qrcode_desc}</span></div>
					</div>
				</div>
			</div>

			<%--<div id="authUserDiv" style="width: 390px;height: 518px;float: left;overflow-y:auto;overflow-x:hidden;margin-left: 8px;margin-top: 10px;background-color: white;box-shadow:0 0 5px rgba(107, 110, 112, 0.4);">--%>
			<div id="recommendDiv" style="width: 390px;height: 518px;float: left;margin-left: 8px;margin-top: 10px;background-color: white;box-shadow:0 0 5px rgba(107, 110, 112, 0.4);">
				<div style="text-align: center;vertical-align: middle;background-color: #169ADA;height: 40px;width: 100px;">
					<span style="color: white;line-height: 40px;">推荐</span>
				</div>
				<c:if test="${recommendPages>0}">
					<c:forEach items="${recommendQrcodeList}" varStatus="status" begin="0" end="${recommendPages*3}" step="3">
						<%--<c:if test="${status.count==1}">
							<div class="portfolio-page2" id="page2-${status.count}" style="text-align: left;">
						</c:if>--%>

						<div class="portfolio-page2" id="page2-${status.count}" style="text-align: left;<c:if test="${status.count!=1}">display:none;</c:if>">
							<c:forEach items="${recommendQrcodeList}" var="recommendQrcodeMap" varStatus="recommendStatus" begin="${(status.count-1)*3}" end="${status.count*3-1}">
								<div style="padding:16px 10px;border-bottom: 1px solid #BCBCBC;" onclick="toQrcodeDesc('${recommendQrcodeMap.id}')">
									<div style="float: left;width: 100px;">
										<img src="${ctx}/upload/${recommendQrcodeMap.publisher_id}/${recommendQrcodeMap.main_picture}" alt="二维码集合" style="height: 100px;width: 100px;cursor: pointer;">
									</div>
									<div style="float: left;padding-left: 10px;width: 222px;">
										<div style="height:25px;overflow:hidden;"><span style="font-size: 14px;font-family: '微软雅黑';color:#666;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${recommendQrcodeMap.qrcode_title}</span></div>
										<div style="height:25px;"><span style="font-size: 14px;font-weight:bold;font-family: '微软雅黑';color:#666;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">类型:${recommendQrcodeMap.qrcode_type}</span></div>
										<div style="height:25px;"><span style="font-size: 14px;font-family: '微软雅黑';color:#666;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">总浏览量:${recommendQrcodeMap.view_num}</span></div>
									</div>
									<div style="clear: both;"></div>
								</div>
							</c:forEach>
						</div>
					</c:forEach>
					<div id="recommendPage" class="pagination" style="text-align: center;">
						<ul class="nav">
							<c:forEach items="${recommendQrcodeList}" varStatus="status" begin="0" end="${recommendPages*3}" step="3">
								<c:if test="${status.count==1}">
									<li class="active">1</li>
								</c:if>
								<c:if test="${status.count!=1}">
									<li>${status.count}</li>
								</c:if>
							</c:forEach>
						</ul>
					</div>
				</c:if>
			</div>

			<div style="clear: both;"></div>

			<div id="isHotQrcodeDiv" style="width: 1200px;/*border-top: 1px solid #BCBCBC;*/margin-top: 10px;background-color: white;box-shadow: 0 0 5px rgba(107, 110, 112, 0.4);">
				<div style="text-align: center;vertical-align: middle;background-color: #169ADA;height: 40px;width: 100px;">
					<span class="tagCss" style="color: white;line-height: 40px;">最热</span>
				</div>
				<div style="padding:0px 10px;">
					<c:forEach items="${isHotQrcodeList}" var="isHotQrcode">
						<div style="width: 380px;height: 106px;border-bottom: 1px solid #E0E0E0;display: inline-block;padding:10px 0px;" onclick="toQrcodeDesc('${isHotQrcode.id}')">
							<div style="width: 110px;display: inline-block;">
								<img width="106px" height="104px" src="${ctx}/upload/${isHotQrcode.publisher_id}/${isHotQrcode.main_picture}" alt="二维码集合">
							</div>
							<div style="width: 240px;display: inline-block;">
								<div style="height: 20px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;"><span style="font-weight:bold;">【${isHotQrcode.qrcode_type}】</span><span>${isHotQrcode.qrcode_title}</span></div>
								<div style="height: 53px;padding: 2px 0px;overflow:hidden;"><span>介绍:${isHotQrcode.qrcode_desc}</span></div>
								<div style="height: 20px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;"><span>浏览量:${isHotQrcode.view_num}</span></div>
							</div>
						</div>
					</c:forEach>
				</div>
				<div style="vertical-align:middle;border-top: 1px solid #E0E0E0;padding-left: 20px;">
				<span style="line-height: 40px;">网站黑名单(微信号)：
					<c:forEach items="${blackWeixinhaoList}" var="blackWeixinhao">
						${blackWeixinhao.weixinhao}&nbsp;&nbsp;&nbsp;
					</c:forEach>
				</span>
				</div>
			</div>
		</div>

		<footer>
			<p style="text-align: center;float: none">微信公众号：二维码优汇(qrcodevip),扫描下方二维码关注公众号</p>
			<div style="text-align: center">
				<img src="${ctx}/images/qrcodeMain.jpg" alt="二维码集合" style="width:120px;height: 120px;">
				<img src="${ctx}/images/qrcode.png" alt="二维码集合" style="width:120px;height: 120px;margin-left: 30px;">
			</div>
		</footer>
	</div>

</body>
</html>