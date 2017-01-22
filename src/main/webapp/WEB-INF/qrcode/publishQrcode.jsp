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
	<%--<script src="https://cdn.bootcss.com/zepto/1.1.6/zepto.min.js"></script>--%>
	<script src="${ctx}/js/weuiUpload.js"></script>
	<title>二维码优汇</title>
	<style type="text/css">
		.fontCss{
			font-size: 14px;
			font-family: '微软雅黑';
			color:#666;
		}
	</style>
	<script type="text/javascript">
		$.toast.prototype.defaults.duration = 3000;

		$(function(){
		})

		function submitForm(){
			var uploadFilesLi = $(".weui_uploader_file");
			if(uploadFilesLi.length!=2){
				$("#uploadFileDiv").addClass("weui_cell_warn");
				return;
			}
			var uploadFiles = "";
			for(var i=0;i<uploadFilesLi.length;i++) {
				var tmpFile = uploadFilesLi.eq(i).css('background-image');
				tmpFile = tmpFile.replace('url(','').replace(')','').replace(/\"/gi, "");
				uploadFiles=uploadFiles+tmpFile+";@;";
			}
			var weixinhao = $.trim($('#weixinhao').val());
			var inviteCode = $.trim($('#inviteCode').val());
			var qrcodeType = $.trim($('#qrcodeType').val());
			var codeCategory = $.trim($('#codeCategory').data('values'));
			var codeTitle = $.trim($('#codeTitle').val());
			var codeDesc = $.trim($('#codeDesc').val());
			var publishNum = '${userMap.publish_num}';
			var publisherId = '${publisherId}';
			if(typeof(publishNum)=="undefined" || publishNum=="" || publishNum==0){
				if(isNullOrEmpty(weixinhao)){
					$("#weixinhaoDiv").addClass("weui_cell_warn");
					return;
				}
				if(isNullOrEmpty(inviteCode)){
					$("#inviteCodeDiv").addClass("weui_cell_warn");
					return;
				}
			}
			if(isNullOrEmpty(qrcodeType)){
				$("#qrcodeTypeDiv").addClass("weui_cell_warn");
				return;
			}
			if(isNullOrEmpty(codeCategory)){
				$("#codeCategoryDiv").addClass("weui_cell_warn");
				return;
			}
			var codeCategorys = codeCategory.split(",");
			if(codeCategorys.length>3){
				$.toast("最多三个标签", "forbidden");
				return;
			}
			if(isNullOrEmpty(codeTitle)){
				$("#codeTitleDiv").addClass("weui_cell_warn");
				return;
			}
			if(isNullOrEmpty(codeDesc)){
				$("#codeDescDiv").addClass("weui_cell_warn");
				return;
			}
			$.showLoading();
			$.showLoading("正在提交...");
			$.ajax({
				async:false,
				type: "POST",
				url:'${ctx}/qrcode/publicQRCode',
				//data:$('#yourformid').serialize(),
				data: {
					weixinhao:weixinhao,
					inviteCode:inviteCode,
					qrcodeType:qrcodeType,
					codeCategory:codeCategory,
					codeTitle:codeTitle,
					codeDesc:codeDesc,
					uploadFiles: uploadFiles,
					publisherId: publisherId
				},
				success: function(data) {
					$.hideLoading();
					if(!jQuery.isEmptyObject(data)){
						var params = eval(data);
						resultSuccess = params.success;
						resultMsg = params.msg;
						if(resultSuccess){
							$.toast(resultMsg,function(){
								WeixinJSBridge.call('closeWindow');
							});
						}else{
							$.toast(resultMsg, "forbidden");
						}
					}else{
						$.toast("系统异常", "forbidden");
					}
				},
				error: function(request) {
					$.hideLoading();
					$.toast("网络异常", "forbidden");
				}
			});


			//$("#publishQrcodeForm").submit();
		}

		function isNullOrEmpty(strVal) {
			if (strVal == '' || strVal == null || strVal == undefined) {
				return true;
			} else {
				return false;
			}
		}

	</script>
</head>
<body>
<header class='demos-header'>
	<h1 class="demos-title">发布信息</h1>
	<%--<p class='demos-sub-title'>轻量强大的UI库，不仅仅是 WeUI</p>--%>
</header>
<form id="publishQrcodeForm" method="post" action="${ctx}/qrcode/publicQRCode"  enctype="multipart/form-data">
	<c:if test="${empty userMap || userMap.publish_num=='0'}">
		<div class="weui_cells weui_cells_form">
			<div class="weui_cells_title" style="font-weight: bold;">首次发布请输入以下信息:</div>
			<div id="weixinhaoDiv" class="weui_cell">
				<div class="weui_cell_hd"><label class="weui_label">微信号</label></div>
				<div class="weui_cell_bd weui_cell_primary">
					<input id="weixinhao" name="weixinhao" class="weui_input" type="text" placeholder="请输入真实微信号">
				</div>
			</div>
			<div id="inviteCodeDiv" class="weui_cell">
				<div class="weui_cell_hd"><label class="weui_label">邀请码</label></div>
				<div class="weui_cell_bd weui_cell_primary">
					<input id="inviteCode" name="inviteCode" class="weui_input" type="text" placeholder="请输入邀请码">
				</div>
			</div>
		</div>
	</c:if>

<div class="weui_cells weui_cells_form">
	<div class="weui_cells_title" style="font-weight: bold;">请填写发布信息:</div>
	<div id="qrcodeTypeDiv" class="weui_cell">
		<div class="weui_cell_hd"><label class="weui_label">二维码类别</label></div>
		<div class="weui_cell_bd weui_cell_primary">
			<input id="qrcodeType" name="qrcodeType" class="weui_input" type="text" placeholder="请选择类别"  readonly>
			<script>
				$("#qrcodeType").picker({
					title: "请选择二维码类别",
					cols: [
						{
							textAlign: 'center',
							values: ['公众号', '个人', '群', '其它']
						}
					]
				});
			</script>
		</div>
	</div>
	<div id="codeCategoryDiv" class="weui_cell">
		<div class="weui_cell_hd"><label class="weui_label">信息标签</label></div>
		<div class="weui_cell_bd weui_cell_primary">
			<input id="codeCategory" name="codeCategory" class="weui_input" type="text" placeholder="请选择标签,以便搜索" value readonly>
			<script>
				$("#codeCategory").select({
					title: "请选择信息标签",
					multi: true,
					max: 3,
					min: 1,
					//beforeClose:beforeCloseSelect(),
					items: [
						{title: "新闻", value: "xinwen"}, {title: "财经", value: "caijing"},{title: "母婴", value: "muying"}, {title: "娱乐", value: "yule"},
						{title: "搞笑", value: "gaoxiao"},{title: "两性", value: "liangxing"},{title: "运动", value: "yundong"},{title: "旅游", value: "lvyou"},
						{title: "汽车", value: "qiche"},{title: "互粉", value: "hufen"},{title: "影音", value: "yingyin"},{title: "游戏", value: "youxi"},
					]
				});
			</script>
		</div>
	</div>
	<div id="codeTitleDiv" class="weui_cell">
		<div class="weui_cell_hd"><label class="weui_label">标题</label></div>
		<div class="weui_cell_bd weui_cell_primary">
			<input id="codeTitle" name="codeTitle" class="weui_input" type="text" placeholder="请输入标题(15字以内)">
		</div>
	</div>
</div>
<div class="weui_cells_title" style="color: black">详细描述</div>
<div class="weui_cells weui_cells_form">
	<div id="codeDescDiv" class="weui_cell">
		<div class="weui_cell_bd weui_cell_primary">
			<textarea id="codeDesc" name="codeDesc" class="weui_textarea" placeholder="请输入详细描述" rows="3"></textarea>
			<div class="weui_textarea_counter">200以内</div>
		</div>
	</div>
</div>
<div class="weui_cells_title">上传(第一张为首页展示图片,第二张为二维码图片)</div>
<div class="weui_cells weui_cells_form">
	<div id="uploadFileDiv" class="weui_cell">
		<div class="weui_cell_bd weui_cell_primary">
			<div class="weui_uploader">
				<div class="weui_uploader_hd weui_cell">
					<div class="weui_cell_bd weui_cell_primary">图片上传</div>
					<div class="weui_cell_ft js_counter">0/2</div>
				</div>
				<div class="weui_uploader_bd">
					<ul class="weui_uploader_files"></ul>
					<div class="weui_uploader_input_wrp">
						<input class="weui_uploader_input js_file" type="file" accept="image/jpg,image/jpeg,image/png,image/gif" multiple=""></div>
				</div>
			</div>
		</div>
	</div>
</div>
<div class="weui_btn_area" style="margin-bottom: 40px;">
	<a href="javascript:submitForm();"  class="weui_btn weui_btn_primary">确定</a>
</div>
</form>
</body>
</html>