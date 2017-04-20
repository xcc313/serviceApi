<%@ page language="java" pageEncoding="utf-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE HTML>
<html>
<head>
<title>BigBoss</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="keywords" content="Modern Responsive web template, Bootstrap Web Templates, Flat Web Templates, Andriod Compatible web template, 
Smartphone Compatible web template, free webdesigns for Nokia, Samsung, LG, SonyErricsson, Motorola web design" />
<script type="application/x-javascript"> addEventListener("load", function() { setTimeout(hideURLbar, 0); }, false); function hideURLbar(){ window.scrollTo(0,1); } </script>
 <!-- Bootstrap Core CSS -->
<link href="${ctx}/css/bootstrap.min.css" rel='stylesheet' type='text/css' />
<!-- Custom CSS -->
<link href="${ctx}/assets/css/style2.css" rel='stylesheet' type='text/css' />
<!-- Graph CSS -->
<link href="${ctx}/assets/css/lines.css" rel='stylesheet' type='text/css' />
<link href="${ctx}/assets/css/font-awesome.css" rel="stylesheet">
<!-- jQuery -->
<script src="${ctx}/js/jquery-1.9.1.min.js"></script>
<script src="${ctx}/js/bootstrap.min.js"></script>
<!----webfonts--->
<!-- <link href='http://fonts.useso.com/css?family=Roboto:400,100,300,500,700,900' rel='stylesheet' type='text/css'> -->
<!---//webfonts--->  
<!-- Nav CSS -->
<link href="${ctx}/assets/css/custom.css" rel="stylesheet">
<!-- Metis Menu Plugin JavaScript -->
<script src="${ctx}/assets/js/metisMenu.min.js"></script>
<script src="${ctx}/assets/js/custom.js"></script>
<!-- Graph JavaScript -->
<%--<script src="${ctx}/manage/js/d3.v3.js"></script>
<script src="${ctx}/manage/js/rickshaw.js"></script>--%>
	<style>
		dt{
			width: 110px !important;
		}
		dd{
			margin-left: 130px !important;
		}
	</style>
<script type="text/javascript">
$(function(){
	$('#orderDescModal').modal({
        keyboard: true,
        show:false
    })
})

var returnExtractionId;
function showDescModal(returnExtractionId){
	$.ajax({
		url:"${ctx}/manage/returnExtractionDesc",
		data:{"returnExtractionId":returnExtractionId},
		dataType : 'json',
		cache: false,
		success:function(data){
			if(!jQuery.isEmptyObject(data)){
				//var params = eval(data);
				if(data.head.result_code == 'FAIL'){
					$.alert(data.head.result_msg);
				}else{
					$("#cashId").val(data.content.id);
					$("#cash_order_no").html(data.content.order_no);
					$("#userNo").html(data.content.user_no);
					$("#cashAmount").html(data.content.amount);
					$("#settleAccountName").html(data.content.settle_account_name);
					$("#settleAccountNo").html(data.content.settle_account_no);
					$("#createTime").html(data.content.create_time);
					var cashStatus = data.content.cash_status;
					if(cashStatus=='0'){
						$("#cashStatus").html("提现中");
					}else if(cashStatus=='1'){
						$("#cashStatus").html("提现成功");
					}else if(cashStatus=='2'){
						$("#cashStatus").html("提现失败");
					}else if(cashStatus=='3'){
						$("#cashStatus").html("未知");
					}
					var isBack = data.content.is_back;
					if(isBack=='1'){
						$("#isBack").html("需要");
					}else{
						$("#isBack").html("不需要");
					}
					var checkStatus = data.content.check_status;
					if(checkStatus=='0'){
						$("#checkStatus").html("未审核");
						$("#checkPassBtn").show();
						$("#checkFailBtn").show();
					}else if(checkStatus=='1'){
						$("#checkStatus").html("审核通过");
						$("#checkPassBtn").hide();
						$("#checkFailBtn").hide();
					}else if(checkStatus=='2'){
						$("#checkStatus").html("审核不通过");
						$("#checkPassBtn").hide();
						$("#checkFailBtn").hide();
					}
					$('#orderDescModal').modal('toggle');
					$('#orderDescModal').popover('show');
				}

			}else{
				$.alert("更新异常，请稍后重试");
			}
		},
		error:function(){
			alert("网络异常");
		}
		
	})
}

function colseGoodsDescTable(){
	$("#goodsOrderDescTable tbody").empty();
}

var checkResult;
	function checkBtn(checkResult){
		var cashId = $("#cashId").val();
		var checkMsg = $("#checkMsg").val();
		$.ajax({
			url:'${ctx}/manage/operReturnExtraction',
			type:'POST',
			data:{'cashId':cashId,'operateType':checkResult,'checkMsg':checkMsg},
			dataType:'json',
			success:function(data){
				if(!jQuery.isEmptyObject(data)){
					alert(data.head.result_msg);
					$('#orderDescModal').modal('toggle');
					$('#orderDescModal').popover('hide');
					window.location.reload();
				}else{
					alert("系统异常");
					$('#orderDescModal').modal('toggle');
					$('#orderDescModal').popover('hide');
					window.location.reload();
				}
			},
			error:function(){
				alert("网络异常，请稍后重试");
				$('#orderDescModal').modal('toggle');
				$('#orderDescModal').popover('hide');
				window.location.reload();
			}
		})
	}
</script>
</head>
<body>
<div id="wrapper">
<%@include file="manageLeft.jsp" %>
        <div id="page-wrapper">
        <div class="col-md-12 graphs">
	   <div class="xs">
  	 <h3>
  	 <span>冲正查询</span>
  	 </h3>
  	<div class="bs-example4" data-example-id="contextual-table">
    <table class="table table-striped">
      <thead>
        <tr>
		  <th>序号</th>
			<th>用户编号</th>
		  <th>提现单号</th>
		  <th>金额</th>
		  <th>状态</th>
			<th>创建时间</th>
		  <th>操作</th>
        </tr>
      </thead>
      <tbody>
      	<c:forEach items="${returnExtractionList }" var="returnExtractionMap" varStatus="Index">
      		<tr>
	          <th>${Index.index+1 }</th>
				<td>${returnExtractionMap.user_no }</td>
	          <td>${returnExtractionMap.cash_order_no }</td>
	          <td>${returnExtractionMap.amount }</td>
	          <td>
	          <c:if test="${returnExtractionMap.check_status eq 0}">
				  未审核
	          </c:if>
	          <c:if test="${returnExtractionMap.check_status eq 1}">
				  审核通过
	          </c:if>
	          <c:if test="${returnExtractionMap.check_status eq 2}">
				  审核不通过
	          </c:if>
	          </td>
				<td>${returnExtractionMap.create_time }</td>
	          <td><a href="javascript:showDescModal('${returnExtractionMap.id }')">详情</a></td>
	        </tr>
      	</c:forEach>
      </tbody>
    </table>
    
    <nav style="text-align: center;">
	  <ul class="pagination" >
	    <%-- <li><a href="${ctx }/goods/goodsOrder.do?page=0" aria-label="Previous"><span aria-hidden="true">&laquo;</span></a></li> --%>
	    <li <c:if test="${currentPage eq 1 || currentPage eq 0 }">class='active'</c:if>><a href="${ctx }/goods/goodsOrder.do?page=1">1</a></li>
	    <li <c:if test="${currentPage eq 2 }">class='active'</c:if>><a href="${ctx }/goods/goodsOrder.do?page=2">2</a></li>
	    <li <c:if test="${currentPage eq 3 }">class='active'</c:if>><a href="${ctx }/goods/goodsOrder.do?page=3">3</a></li>
	    <li <c:if test="${currentPage eq 4 }">class='active'</c:if>><a href="${ctx }/goods/goodsOrder.do?page=4">4</a></li>
	    <li <c:if test="${currentPage eq 5 }">class='active'</c:if>><a href="${ctx }/goods/goodsOrder.do?page=5">5</a></li>
	    <li <c:if test="${currentPage eq 6 }">class='active'</c:if>><a href="${ctx }/goods/goodsOrder.do?page=6">6</a></li>
	    <li <c:if test="${currentPage eq 7 }">class='active'</c:if>><a href="${ctx }/goods/goodsOrder.do?page=7">7</a></li>
	    <li <c:if test="${currentPage eq 8 }">class='active'</c:if>><a href="${ctx }/goods/goodsOrder.do?page=8">8</a></li>
	    <li <c:if test="${currentPage eq 9 }">class='active'</c:if>><a href="${ctx }/goods/goodsOrder.do?page=9">9</a></li>
	    <li <c:if test="${currentPage eq 10 }">class='active'</c:if>><a href="${ctx }/goods/goodsOrder.do?page=10">10</a></li>
	    <!-- <li><a href="#" aria-label="Next"><span aria-hidden="true">&raquo;</span></a></li> -->
	  </ul>
	</nav>
   </div>
  </div>
  <div class="copy_layout">
      <p>Copyright &copy; 2016.Shenzhen City Lu Yao Li Technology Co. Ltd.</p>
  </div>
   </div>
      </div>
      <!-- /#page-wrapper -->
   </div>
    <!-- /#wrapper -->
    
    
    <!-- Modal -->
<div class="modal fade" id="orderDescModal">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title">提现订单号:<font id="cash_order_no"></font></h4>
		  <input id="cashId" style="display: none;">
      </div>
      <div class="modal-body container-fluid" style="">
        <div class="row">
        	<div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
				<ul class="list-inline">
					<li style="width: 40%">
						<dl class="dl-horizontal">
							<dt>用户编号:</dt>
							<dd id="userNo"></dd>
						</dl>
					</li>
					<li style="width: 40%">
						<dl class="dl-horizontal">
							<dt>提现金额:</dt>
							<dd id="cashAmount"></dd>
						</dl>
					</li>
					<li style="width: 40%">
						<dl class="dl-horizontal">
							<dt>结算户名:</dt>
							<dd id="settleAccountName"></dd>
						</dl>
					</li>
					<li style="width: 40%">
						<dl class="dl-horizontal">
							<dt>结算卡号:</dt>
							<dd id="settleAccountNo"></dd>
						</dl>
					</li>
					<li style="width: 40%">
						<dl class="dl-horizontal">
							<dt>提现时间:</dt>
							<dd id="createTime"></dd>
						</dl>
					</li>
					<li style="width: 40%">
						<dl class="dl-horizontal">
							<dt>提现状态:</dt>
							<dd id="cashStatus"></dd>
						</dl>
					</li>
					<li style="width: 40%">
						<dl class="dl-horizontal">
							<dt>是否需要冲正:</dt>
							<dd id="isBack"></dd>
						</dl>
					</li>
					<li style="width: 40%">
						<dl class="dl-horizontal">
							<dt>冲正审核:</dt>
							<dd id="checkStatus"></dd>
						</dl>
					</li>
				</ul>
        	</div>
			<div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
				<textarea class="form-control" rows="3" placeholder="请输入审核信息" id="checkMsg"></textarea>
			</div>
        </div>
      </div>
      <div class="modal-footer">
		  <button type="button" class="btn btn-primary" id="checkPassBtn" onclick="checkBtn('0')">审核通过</button>
		  <button type="button" class="btn btn-danger" id="checkFailBtn" onclick="checkBtn('1')">审核不通过</button>
        <button type="button" class="btn btn-default" data-dismiss="modal" onclick="colseGoodsDescTable()">关闭</button>
      </div>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<!-- Modal -->
<div class="modal fade" id="resultModal">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="resultTitle"></h4>
      </div>
      <div class="modal-body" style="text-align: center;">
        <h5 id="resultBody"></h5>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal" onclick="javascript:window.location.reload();">关闭</button>
      </div>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div><!-- /.modal -->
</body>
</html>
