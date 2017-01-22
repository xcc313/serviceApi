<%@ page language="java"  contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta name="baidu-site-verification" content="wBL3Uce8wt" />
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
		.marqueeleft ul{float:left;}
		.marqueeleft li{float:left;margin:0 5px;display:inline;overflow:hidden;}
		#newQrcodeDiv span{
			font-family: '微软雅黑';
			color:#666;
			font-size: 13px;
		}
		#hotQrcodeDiv span{
			font-family: '微软雅黑';
			color:#666;
			font-size: 13px;
		}
		#authUserDiv span{
			font-family: '微软雅黑';
			color:#666;
			font-size: 13px;
		}
	</style>

	<script>window._bd_share_config={"common":{"bdSnsKey":{},"bdText":"","bdMini":"2","bdMiniList":false,"bdPic":"","bdStyle":"0","bdSize":"16"},"slide":{"type":"slide","bdImg":"7","bdPos":"left","bdTop":"57.5"},"image":{"viewList":["qzone","tsina","tqq","renren","weixin"],"viewText":"分享到：","viewSize":"16"},"selectShare":{"bdContainerClass":null,"bdSelectMiniList":["qzone","tsina","tqq","renren","weixin"]}};with(document)0[(getElementsByTagName('head')[0]||body).appendChild(createElement('script')).src='http://bdimg.share.baidu.com/static/api/js/share.js?v=89860593.js?cdnversion='+~(-new Date()/36e5)];</script>

	<script type="text/javascript">

		$(function () {
			/*$('.pagination li').click(changePage);*/
			$('#hotPage li').click(changePage);
			$('.portfolio-item').magnificPopup({
				type: 'image',
				gallery:{
					enabled:true
				}
			});

			$('#authPage li').click(function authChangePage(event) {
				var pageNo = $(this).html();

				$('.portfolio-page2').hide();
				$('#page2-' + pageNo).fadeIn();
				$('#authPage li').removeClass('active');
				$(this).addClass('active');
			});
		});

		//js无缝滚动代码
		function marquee(i, direction){
			var obj = document.getElementById("marquee" + i);
			var obj1 = document.getElementById("marquee" + i + "_1");
			var obj2 = document.getElementById("marquee" + i + "_2");
			if (direction == "up"){
				if (obj2.offsetTop - obj.scrollTop <= 0){
					obj.scrollTop -= (obj1.offsetHeight + 20);
				}else{
					var tmp = obj.scrollTop;
					obj.scrollTop++;
					if (obj.scrollTop == tmp){
						obj.scrollTop = 1;
					}
				}
			}else{
				if (obj2.offsetWidth - obj.scrollLeft <= 0){
					obj.scrollLeft -= obj1.offsetWidth;
				}else{
					obj.scrollLeft++;
				}
			}
		}

		function marqueeStart(i, direction){
			var obj = document.getElementById("marquee" + i);
			var obj1 = document.getElementById("marquee" + i + "_1");
			var obj2 = document.getElementById("marquee" + i + "_2");

			obj2.innerHTML = obj1.innerHTML;
			var marqueeVar = window.setInterval("marquee("+ i +", '"+ direction +"')", 20);
			obj.onmouseover = function(){
				window.clearInterval(marqueeVar);
			}
			obj.onmouseout = function(){
				marqueeVar = window.setInterval("marquee("+ i +", '"+ direction +"')", 20);
			}
		}

		var qrcodeId;
		function toQrcodeDesc(qrcodeId){
			window.open("${ctx}/website/codeDesc?id="+qrcodeId);
		}

		/*function showMainImage(){
			//alert("test");
			$("#iframeDiv").css("width","100%");
			$("#authUserDiv").css("display","none");
		}
		function hiddenMainImage(){
			//alert("test");
			$("#iframeDiv").css("width","68%");
			$("#authUserDiv").css("display","block");
		}*/


	</script>

</head>
<body>
	<div class="main-container" style="background-color: #eeeeee;box-shadow:0 0 0px rgba(107, 110, 112, 0.4);">
		<%@include file="../layouts/websiteHead.jsp" %>
		<div class="content-container" style="margin-top: 15px;box-sizing:inherit;width:1200px;padding:0px;max-width: 1200px;">
			<div style="padding: 0px 15px;background-color: white;">
				<div id="marquee1" class="marqueeleft" style="overflow:hidden;">
					<div style="width:8000px;">
						<%--<c:if test="${empty isScrollQrcodeList || isScrollQrcodeList==null || fn:length(isScrollQrcodeList)<7}">--%>
						<ul id="marquee1_1" style="float:left;">
							<c:forEach items="${recommendQrcodeList}" var="recommendQrcodeMap">
								<li>
									<a class="pic" href="${ctx}/website/codeDesc?id=${recommendQrcodeMap.id}" target="_blank"><img width="135" height="104" src="${ctx}/upload/${recommendQrcodeMap.publisher_id}/${recommendQrcodeMap.main_picture}" alt="二维码集合"></a>
									<div class="txt" style="width:135px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;"><a style="font-family: '微软雅黑';color:#666;" href="${recommendQrcodeMap.qrcode_url}">${recommendQrcodeMap.qrcode_title}</a></div>
								</li>
							</c:forEach>
						</ul>
						<c:if test="${fn:length(recommendQrcodeList)>6}">
							<ul id="marquee1_2"></ul>
						</c:if>
					</div>
				</div><!--marqueeleft end-->
				<script type="text/javascript">marqueeStart(1, "left");</script>
			</div>

			<div id="hotQrcodeDiv" style="width: 800px;height: 518px;margin-top: 10px;background-color: white;float: left;box-shadow:0 0 5px rgba(107, 110, 112, 0.4);">
				<div style="text-align: center;vertical-align: middle;background-color: #169ADA;height: 40px;width: 100px;">
					<span style="color: white;line-height: 40px;">热门</span>
				</div>
				<div class="main-container" style="box-shadow:0 0 0px rgba(107, 110, 112, 0.4);">
					<div class="content-container" style="padding:0px 0px 10px 0px;">
						<div id="portfolio-content" class="center-text">
							<c:if test="${hotPages>0}">
								<c:forEach items="${isHotQrcodeList}" varStatus="status" begin="0" end="${hotPages*8}" step="8">
									<%--<c:if test="${status.count==1}">
									<div class="portfolio-page" id="page-${status.count}" style="text-align: left;">
									</c:if>--%>

									<div class="portfolio-page" id="page-${status.count}" style="text-align: left;<c:if test="${status.count!=1}">display:none;</c:if>">

									<c:forEach items="${isHotQrcodeList}" var="isHotQrcodeMap" varStatus="hotStatus" begin="${(status.count-1)*8}" end="${status.count*8-1}">
										<div class="portfolio-group">
											<a class="portfolio-item" href="${ctx}/upload/${isHotQrcodeMap.publisher_id}/${isHotQrcodeMap.qrcode_url}">
												<img src="${ctx}/upload/${isHotQrcodeMap.publisher_id}/${isHotQrcodeMap.main_picture}" alt="二维码集合">
												<div class="detail">
													<h3 style="margin:0px 0px;text-align: center;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${isHotQrcodeMap.qrcode_title}</h3>
													<p style="height: 109px;overflow:hidden;">${isHotQrcodeMap.qrcode_deec}</p>
													<span class="btn" style="text-align: center;">View</span>
												</div>
											</a>
										</div>
									</c:forEach>
									</div>
								</c:forEach>
								<div id="hotPage" class="pagination" style="text-align: center;">
									<ul class="nav">
										<c:forEach items="${isHotQrcodeList}" varStatus="status" begin="0" end="${hotPages*8}" step="8">
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
					</div>
				</div>
			</div>

			<%--<div id="authUserDiv" style="width: 390px;height: 518px;float: left;overflow-y:auto;overflow-x:hidden;margin-left: 8px;margin-top: 10px;background-color: white;box-shadow:0 0 5px rgba(107, 110, 112, 0.4);">--%>
			<div id="authUserDiv" style="width: 390px;height: 518px;float: left;margin-left: 8px;margin-top: 10px;background-color: white;box-shadow:0 0 5px rgba(107, 110, 112, 0.4);">
				<div style="text-align: center;vertical-align: middle;background-color: #169ADA;height: 40px;width: 100px;">
					<span style="color: white;line-height: 40px;">认证用户</span>
				</div>
				<c:if test="${authPages>0}">
					<c:forEach items="${authPublisherList}" varStatus="status" begin="0" end="${authPages*3}" step="3">
						<%--<c:if test="${status.count==1}">
						<div class="portfolio-page2" id="page2-${status.count}" style="text-align: left;">
						</c:if>--%>

						<div class="portfolio-page2" id="page2-${status.count}" style="text-align: left;<c:if test="${status.count!=1}">display:none;</c:if>">

							<c:forEach items="${authPublisherList}" var="authPublisherMap" varStatus="authStatus" begin="${(status.count-1)*3}" end="${status.count*3-1}">
								<div style="padding:16px 10px;border-bottom: 1px solid #BCBCBC;">
									<div style="float: left;width: 100px;">
										<img src="${authPublisherMap.headimgurl}" alt="二维码集合" style="height: 100px;width: 100px;cursor: pointer;">
									</div>
									<div style="float: left;padding-left: 10px;width: 222px;">
										<div style="height:25px;"><span style="font-size: 14px;font-family: '微软雅黑';color:#666;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${authPublisherMap.nickname}</span></div>
										<div style="height:25px;"><span style="font-size: 14px;font-weight:bold;font-family: '微软雅黑';color:#666;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">认证ID:${authPublisherMap.auth_id}</span></div>
											<%--<div style="height:40px;vertical-align: middle;"><span style="line-height: 40px;font-family: '微软雅黑';color:#666;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">包含二维码类别:${authPublisherMap.categorySB}</span></div>--%>
										<div style="height:25px;"><span style="font-size: 14px;font-family: '微软雅黑';color:#666;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">总浏览量:${authPublisherMap.qrcodeViewNum}</span></div>
									</div>
									<div style="clear: both;"></div>
								</div>
							</c:forEach>
						</div>
					</c:forEach>
					<div id="authPage" class="pagination" style="text-align: center;">
						<ul class="nav">
							<c:forEach items="${authPublisherList}" varStatus="status" begin="0" end="${authPages*3}" step="3">
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

			<div id="newQrcodeDiv" style="width: 1200px;/*border-top: 1px solid #BCBCBC;*/margin-top: 10px;background-color: white;box-shadow: 0 0 5px rgba(107, 110, 112, 0.4);">

				<div style="text-align: center;vertical-align: middle;background-color: #169ADA;height: 40px;width: 100px;">
					<span style="color: white;line-height: 40px;">最新</span>
				</div>
				<div style="padding:0px 10px;">
					<c:forEach items="${isNewQrcodeList}" var="newQrcode">
						<div style="width: 380px;height: 106px;border-bottom: 1px solid #E0E0E0;display: inline-block;padding:10px 0px;" onclick="toQrcodeDesc('${newQrcode.id}')">
							<div style="width: 110px;display: inline-block;">
								<img width="106px" height="104px" src="${ctx}/upload/${newQrcode.publisher_id}/${newQrcode.main_picture}" alt="二维码集合">
							</div>
							<div style="width: 240px;display: inline-block;">
								<div style="height: 20px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;"><span style="font-weight:bold;">【${newQrcode.qrcode_type}】</span><span>${newQrcode.qrcode_title}</span></div>
								<div style="height: 53px;padding: 2px 0px;overflow:hidden;"><span>介绍:${newQrcode.qrcode_desc}</span></div>
								<div style="height: 20px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;"><span>浏览量:${newQrcode.view_num}</span></div>
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
				<div style="vertical-align:middle;border-top: 1px solid #E0E0E0;padding-left: 20px;">
					<span style="line-height: 40px;">友情链接：
						<a href="http://asd.red">旦腾家园</a><span>&nbsp;&nbsp;&nbsp;经典绝版网页纯文字游戏，期待你的体验</span>
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