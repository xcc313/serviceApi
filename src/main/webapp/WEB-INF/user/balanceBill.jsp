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
    <title>余额账单</title>
    <style type="text/css">
    </style>
    <script type="text/javascript">
        var zxPage = 1;
        $(function(){
            var balanceHistoryListNum = '${fn:length(balanceHistoryList)}';
            if(balanceHistoryListNum>=10){
                var loading = false;  //状态标记
                $(document.body).infinite(100).on("infinite", function() {
                    if(loading) return;
                    loading = true;
                    setTimeout(function() {
                        $.ajax({
                            url : '${ctx}/pay/loadMoreBalanceBill',
                            data:"index="+zxPage+"&userNo=${userNo}",
                            async: false,
                            dataType : 'json',
                            //cache: true, //设置缓存
                            success: function(json) {
                                if (json!=null && json.length > 0) {
                                    var items = [];
                                    var item,method,amount ,create_time ,order_no,trans_status;
                                    for(var i=0, l=json.length; i<l; i++){
                                        item = json[i];
                                        method = item.method;
                                        amount = item.amount;
                                        create_time = item.create_time;
                                        order_no = item.order_no;
                                        trans_status = item.trans_status;
                                        $("#historyDiv").append("<div class=\"weui_media_box weui_media_text\"> <div class=\"weui_media_title\"> <span style=\"float: left;\">"+method+"</span> <span style=\"float: left;padding-left: 20%\">"+amount+" 元</span> <span style=\"color: #999;float: right;\">"+create_time+"</span> </div> <div class=\"weui_media_desc\"> <span>订单编号:"+order_no+"</span> <span style=\"float: right;color: #049202\">"+trans_status+"</span> </div> </div>");
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

        function onDevelop(){
            $.alert("正在开发中...");
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
<body style="font-family: 微软雅黑;">
<div>
    <div class="weui_panel weui_panel_access">
        <div class="weui_panel_hd">余额账单</div>
        <div class="weui_panel_bd" id="historyDiv">
            <c:forEach items="${balanceHistoryList}" var="balanceHistoryMap">
            <div class="weui_media_box weui_media_text">
                <div class="weui_media_title">
                    <span style="float: left;">${balanceHistoryMap.method}</span>
                    <span style="float: left;padding-left: 20%">${balanceHistoryMap.amount} 元</span>
                    <span style="color: #999;float: right;">${balanceHistoryMap.create_time}</span>
                </div>
                <div class="weui_media_desc">
                    <span style="">订单编号:${balanceHistoryMap.order_no}</span>
                    <span style="float: right;color: #049202">${balanceHistoryMap.trans_status}</span>
                </div>
            </div>
            </c:forEach>
        </div>
    </div>
    <div class="weui-infinite-scroll" id="loadingDiv">
        <div class="infinite-preloader"></div><!-- 菊花 -->
        正在加载... <!-- 文案，可以自行修改 -->
    </div>
</div>

</body>
</html>