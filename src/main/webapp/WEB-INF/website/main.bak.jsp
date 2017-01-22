<%@ page language="java"  contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>二维码集合</title>
	<meta name="keywords" content="" />
	<meta name="description" content="" />
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<c:set var="ctx" value="${pageContext.request.contextPath}" />
	
	<link href="${ctx}/css/font-awesome.min.css" rel="stylesheet" type="text/css">
	<link href="${ctx}/css/magnific-popup.css" rel="stylesheet">
	<link href="${ctx}/css/templatemo_style.css" rel="stylesheet" type="text/css">

	<%--<script src="${ctx}/js/image.scroll.js"></script>--%>
	<script type="text/javascript" src="${ctx}/js/jquery-1.9.1.min.js"></script>
	<script type="text/javascript" src="${ctx}/js/jquery.easing.1.3.js"></script>
	<script type="text/javascript" src="${ctx}/js/modernizr-2.6.2.min.js"></script>
	<script type="text/javascript" src="${ctx}/js/jquery.magnific-popup.min.js"></script>
	<script type="text/javascript" src="${ctx}/js/templatemo_script.js"></script>
	<style type="text/css">
		.marqueeleft ul{float:left;}
		.marqueeleft li{float:left;margin:0 5px;display:inline;overflow:hidden;}
	</style>

	<script type="text/javascript">
		$(function () {
			$('.pagination li').click(changePage);
			$('.portfolio-item').magnificPopup({
				type: 'image',
				gallery:{
					enabled:true
				}
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


	</script>

</head>
<body>
	<div class="main-container">
		<%@include file="../layouts/websiteHead.jsp" %>
		<div class="content-container">
			<header>
				<div id="marquee1" class="marqueeleft" style="overflow:hidden;">
					<div style="width:8000px;">
						<ul id="marquee1_1" style="float:left;">
							<li>
								<a class="pic" href="#"><img width="135" height="104" src="${ctx}/images/1-small.jpg"></a>
								<div class="txt"><a href="#">南方国际大厦1</a></div>
							</li>
							<li>
								<a class="pic" href="#"><img width="135" height="104" src="${ctx}/images/2-small.jpg"></a>
								<div class="txt"><a href="#">南方国际大厦2</a></div>
							</li>
							<li>
								<a class="pic" href="#"><img width="135" height="104" src="${ctx}/images/3-small.jpg"></a>
								<div class="txt"><a href="#">南方国际大厦3</a></div>
							</li>
							<li>
								<a class="pic" href="#"><img width="135" height="104" src="${ctx}/images/4-small.jpg"></a>
								<div class="txt"><a href="#">南方国际大厦4</a></div>
							</li>
							<li>
								<a class="pic" href="#"><img width="135" height="104" src="${ctx}/images/5-small.jpg"></a>
								<div class="txt"><a href="#">南方国际大厦5</a></div>
							</li>
							<li>
								<a class="pic" href="#"><img width="135" height="104" src="${ctx}/images/6-small.jpg"></a>
								<div class="txt"><a href="#">南方国际大厦6</a></div>
							</li>
							<li>
								<a class="pic" href="#"><img width="135" height="104" src="${ctx}/images/7-small.jpg"></a>
								<div class="txt"><a href="#">南方国际大厦7</a></div>
							</li>
							<li>
								<a class="pic" href="#"><img width="135" height="104" src="${ctx}/images/8-small.jpg"></a>
								<div class="txt"><a href="#">南方国际大厦8</a></div>
							</li>
						</ul>
						<ul id="marquee1_2"></ul>
					</div>
				</div><!--marqueeleft end-->
				<script type="text/javascript">marqueeStart(1, "left");</script>

			</header>
			<div id="portfolio-content" class="center-text" style="width: 70%;text-align: left;margin: 0 0;">
				<div class="portfolio-page" id="page-1" style="text-align: left;">
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/1-large.jpg">
							<img src="${ctx}/images/1-small.jpg" alt="image 1">
							<div class="detail">
								<%--<h3>Wavy Road</h3>--%>
								<p>Duis ac laoreet mi. Maecenas non lorem sed elit molestie tincidunt. Vestibulum tincidunt libero urna, ut dignissim purus accumsan nec.</p>
								<span class="btn" style="text-align: center;">View</span>
							</div>
						</a>
					</div>
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/2-large.jpg">
							<img src="${ctx}/images/2-small.jpg" alt="image 2">
							<div class="detail">
								<h3>Rocky Mountain</h3>
								<p>Sed in molestie lectus. Curabitur non est neque. Maecenas id luctus ligula. Duis ac laoreet mi. Maecenas non lorem sed elit molestie tincidunt.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/3-large.jpg">
							<img src="${ctx}/images/3-small.jpg" alt="image 3">
							<div class="detail">
								<h3>Clear River</h3>
								<p>Duis ac laoreet mi. Maecenas non lorem sed elit molestie tincidunt. Vestibulum tincidunt libero urna, ut dignissim purus accumsan nec.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/4-large.jpg">
							<img src="${ctx}/images/4-small.jpg" alt="image 4">
							<div class="detail">
								<h3>Rounded Flower</h3>
								<p>Sed in molestie lectus. Curabitur non est neque. Maecenas id luctus ligula. Mauris dignissim ante eu arcu ultricies, at sodales orci aliquet.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/5-large.jpg">
							<img src="${ctx}/images/5-small.jpg" alt="image 5">
							<div class="detail">
								<h3>Bustling City</h3>
								<p>Duis ac laoreet mi. Vestibulum tincidunt libero urna, ut dignissim purus accumsan nec. Sed in molestie lectus. Curabitur non est neque.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/6-large.jpg">
							<img src="${ctx}/images/6-small.jpg" alt="image 6">
							<div class="detail">
								<h3>Retired Leaves</h3>
								<p>Sed in molestie lectus. Curabitur non est neque. Maecenas id luctus ligula. Mauris dignissim ante eu arcu ultricies, at sodales orci aliquet.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/7-large.jpg">
							<img src="${ctx}/images/7-small.jpg" alt="image 7">
							<div class="detail">
								<h3>Clean Design</h3>
								<p>Vestibulum tincidunt libero urna, ut dignissim purus accumsan nec. Maecenas id luctus ligula. Mauris dignissim ante eu arcu ultricies, at sodales orci aliquet.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/8-large.jpg">
							<img src="${ctx}/images/8-small.jpg" alt="image 8">
							<div class="detail">
								<h3>Rock Solid</h3>
								<p>Duis ac laoreet mi. Maecenas non lorem sed elit molestie tincidunt. Mauris dignissim ante eu arcu ultricies, at sodales orci aliquet.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
				</div>
				<div class="portfolio-page" id="page-2" style="display:none;">
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/8-large.jpg">
							<img src="${ctx}/images/8-small.jpg" alt="image 8" style="width:120px;height: 120px;">
							<div class="detail">
								<h3>Wavy Road</h3>
								<p>Duis ac laoreet mi. Maecenas non lorem sed elit molestie tincidunt. Sed in molestie lectus. Curabitur non est neque.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/7-large.jpg">
							<img src="${ctx}/images/7-small.jpg" alt="image 7" style="width:120px;height: 120px;">
							<div class="detail">
								<h3>Rocky Mountain</h3>
								<p>Sed in molestie lectus. Curabitur non est neque. Maecenas id luctus ligula. Mauris dignissim ante eu arcu ultricies, at sodales orci aliquet. Duis ac laoreet mi.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/6-large.jpg">
							<img src="${ctx}/images/6-small.jpg" alt="image 6" style="width:120px;height: 120px;">
							<div class="detail">
								<h3>Clear River</h3>
								<p>Duis ac laoreet mi. Maecenas non lorem sed elit molestie tincidunt. Vestibulum tincidunt libero urna, ut dignissim purus accumsan nec. Sed in molestie lectus.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/5-large.jpg">
							<img src="${ctx}/images/5-small.jpg" alt="image 5" style="width:120px;height: 120px;">
							<div class="detail">
								<h3>Rounded Flower</h3>
								<p>Sed in molestie lectus. Curabitur non est neque. Maecenas id luctus ligula. Mauris dignissim ante eu arcu ultricies, at sodales orci aliquet.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/4-large.jpg">
							<img src="${ctx}/images/4-small.jpg" alt="image 4" style="width:120px;height: 120px;">
							<div class="detail">
								<h3>Bustling City</h3>
								<p>Duis ac laoreet mi. Maecenas non lorem sed elit molestie tincidunt. Vestibulum tincidunt libero urna, ut dignissim purus accumsan nec.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>

					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/3-large.jpg">
							<img src="${ctx}/images/3-small.jpg" alt="image 3" style="width:120px;height: 120px;">
							<div class="detail">
								<h3>Retired Leaves</h3>
								<p>Sed in molestie lectus. Curabitur non est neque. Maecenas id luctus ligula. Mauris dignissim ante eu arcu ultricies, at sodales orci aliquet.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/2-large.jpg">
							<img src="${ctx}/images/2-small.jpg" alt="image 2" style="width:120px;height: 120px;">
							<div class="detail">
								<h3>Clean Design</h3>
								<p>Vestibulum tincidunt libero urna, ut dignissim purus accumsan nec. Sed in molestie lectus. Curabitur non est neque. Maecenas id luctus ligula.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/1-large.jpg">
							<img src="${ctx}/images/1-small.jpg" alt="image 1" style="width:120px;height: 120px;">
							<div class="detail">
								<h3>Rock Solid</h3>
								<p>Duis ac laoreet mi. Maecenas non lorem sed elit molestie tincidunt. Maecenas id luctus ligula. Mauris dignissim ante eu arcu ultricies, at sodales orci aliquet.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
				</div>
				<div class="portfolio-page" id="page-3" style="display:none;">
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/3-large.jpg">
							<img src="${ctx}/images/3-small.jpg" alt="image 3" style="width:120px;height: 120px;">
							<div class="detail">
								<h3>Wavy Road</h3>
								<p>Duis ac laoreet mi. Maecenas non lorem sed elit molestie tincidunt. Vestibulum tincidunt libero urna, ut dignissim purus accumsan nec. Curabitur non est neque.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/2-large.jpg">
							<img src="${ctx}/images/2-small.jpg" alt="image 2" style="width:120px;height: 120px;">
							<div class="detail">
								<h3>Rocky Mountain</h3>
								<p>Sed in molestie lectus. Curabitur non est neque. Maecenas id luctus ligula. Mauris dignissim ante eu arcu ultricies, at sodales orci aliquet. Duis ac laoreet mi.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/4-large.jpg">
							<img src="${ctx}/images/4-small.jpg" alt="image 4" style="width:120px;height: 120px;">
							<div class="detail">
								<h3>Clear River</h3>
								<p>Duis ac laoreet mi. Maecenas non lorem sed elit molestie tincidunt. Vestibulum tincidunt libero urna, ut dignissim purus accumsan nec. Sed in molestie lectus.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/1-large.jpg">
							<img src="${ctx}/images/1-small.jpg" alt="image 1" style="width:120px;height: 120px;">
							<div class="detail">
								<h3>Rounded Flower</h3>
								<p>Sed in molestie lectus. Curabitur non est neque. Maecenas id luctus ligula. Mauris dignissim ante eu arcu ultricies, at sodales orci aliquet. </p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/5-large.jpg">
							<img src="${ctx}/images/5-small.jpg" alt="image 5" style="width:120px;height: 120px;">
							<div class="detail">
								<h3>Bustling City</h3>
								<p>Duis ac laoreet mi. Maecenas non lorem sed elit molestie tincidunt. Vestibulum tincidunt libero urna, ut dignissim purus accumsan nec. Curabitur non est neque.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/6-large.jpg">
							<img src="${ctx}/images/6-small.jpg" alt="image 22" style="width:120px;height: 120px;">
							<div class="detail">
								<h3>Retired Leaves</h3>
								<p>Sed in molestie lectus. Curabitur non est neque. Maecenas id luctus ligula. Mauris dignissim ante eu arcu ultricies, at sodales orci aliquet.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/8-large.jpg">
							<img src="${ctx}/images/8-small.jpg" alt="image 8" style="width:120px;height: 120px;">
							<div class="detail">
								<h3>Clean Design</h3>
								<p>Vestibulum tincidunt libero urna, ut dignissim purus accumsan nec. Sed in molestie lectus. Curabitur non est neque. Maecenas id luctus ligula.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
					<div class="portfolio-group">
						<a class="portfolio-item" href="${ctx}/images/7-large.jpg">
							<img src="${ctx}/images/7-small.jpg" alt="image 7" style="width:120px;height: 120px;">
							<div class="detail">
								<h3>Rock Solid</h3>
								<p>Duis ac laoreet mi. Maecenas non lorem sed elit molestie tincidunt. Maecenas id luctus ligula. Mauris dignissim ante eu arcu ultricies, at sodales orci aliquet.</p>
								<span class="btn">View</span>
							</div>
						</a>
					</div>
				</div> <!-- page 3 -->
				<div class="pagination" style="text-align: center;">
					<ul class="nav">
						<li class="active">1</li>
						<li>2</li>
						<li>3</li>
					</ul>
				</div>
			</div>
		</div>	<!-- /.content-container -->
    
		<footer>
			<p>Copyright &copy; 2084 Your Company Name Collect from <a href="http://www.cssmoban.com/" title="网页模板" target="_blank">网页模板</a> - More Templates <a href="http://www.cssmoban.com/" target="_blank" title="模板之家">模板之家</a></p>
			<div class="social right">
				<a href="#"><i class="fa fa-facebook"></i></a>
				<a href="#"><i class="fa fa-twitter"></i></a>
				<a href="#"><i class="fa fa-google-plus"></i></a>
				<a href="#"><i class="fa fa-dribbble"></i></a>
				<a href="#"><i class="fa fa-instagram"></i></a>
				<a href="#"><i class="fa fa-linkedin"></i></a>
			</div>
		</footer>
	</div>

</body>
</html>