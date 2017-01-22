<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>
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
    <script type='text/javascript' src='${ctx}/js/swiper.min.js' charset='utf-8'></script>
    <title>微微一笑</title>
    <style type="text/css">
        .surePayText{
            color: #fd8d2c !important;
        }
        .weui_dialog{
            top:20%;
            max-height: 70%;
            overflow-y: auto;
        }
        .panelTitle{
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
        }
    </style>
    <script type="text/javascript">
        $(function(){
            $(".weui_media_desc").css("display","block");
            showBigPic.open();
            //showBigPic.onClick();


            var firstIndex = '${firstIndex}';
            var loading = false;  //状态标记
            $(document.body).infinite().on("infinite", function() {
                if(loading) return;
                loading = true;
                setTimeout(function() {
                    $.ajax({
                        url : '${ctx}/api/loadJokeHistory',
                        data:"firstIndex="+firstIndex,
                        async: false,
                        dataType : 'json',
                        //cache: true, //设置缓存
                        success: function(json) {
                            if (json!=null && json.length > 0) {
                                var items = [];
                                var item,id,type,content,subContent,pic,addtime,create_time;
                                for(var i=0, l=json.length; i<l; i++){
                                    item = json[i];
                                    id = item.id;
                                    type = item.type;
                                    content = item.content;
                                    pic = item.pic;
                                    addtime = item.addtime;
                                    create_time = item.create_time;
                                    subContent = item.subContent;
                                    var mediaTitle = content;
                                    var mediaDesc = "<img src=\""+pic+"\" alt=\"速查服务\" style=\"max-height: 300px;\" onclick=\"openImg('"+pic+"','"+content+"')\">";
                                    if(type=="text"){
                                        mediaTitle = subContent;
                                        mediaDesc = content;
                                    }
                                    $("#jokeListDiv").append("<div class=\"weui_media_box weui_media_text\"><h4 class=\"weui_media_title\">"+mediaTitle+"</h4><p class=\"weui_media_desc\">"+mediaDesc+"</p><ul class=\"weui_media_info\"><li class=\"weui_media_info_meta\">速查服务</li><li class=\"weui_media_info_meta\">"+create_time+"</li></ul> </div>");
                                    $(".weui_media_desc").css("display","block");
                                    if(i==l-1){
                                        firstIndex = id;
                                    }
                                }
                                loading = false;
                            } else {
                                $(document.body).destroyInfinite();  //销毁插件
                                return;
                            }
                        },
                        error: function() {
                            $.alert('请求数据失败，请稍后再试');
                            $(document.body).destroyInfinite();  //销毁插件
                        }
                    });//end ajax
                }, 1500);   //模拟延迟
            });
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

        var showBigPic = $.photoBrowser({
            items: [
                {
                    image: "${ctx}/images/fcheck.jpg",
                    caption: "关注\"速查服务\"公众号，笑话订阅，随时逗你"
                }
            ]
        });
        function openImg(imgSrc,content){
            //var photoContainerImg = $(".photo-container img:eq(0)");
            //alert(photoContainerImg.is(":empty"));

            $(".photo-container img:eq(0)").attr("src",imgSrc);
            $(".caption-item.caption-item-0.active").html(content);
            console.log("imgSrc="+imgSrc+",content="+content);
            showBigPic.open();
        }

    </script>
</head>
<body>
<header class='demos-header'>
    <h1 class="demos-title" style="font-size: 24px;">开心一刻</h1>
    <p class='demos-sub-title'>笑一笑，十年少</p>
</header>
<div>
    <div class="weui_panel">
        <div class="weui_panel_hd">关注“速查服务”,订阅笑话更方便哦</div>
        <div class="weui_panel_bd" id="jokeListDiv">
            <c:forEach items="${jokeList}" var="jokeMap">
                <div class="weui_media_box weui_media_text">
                    <h4 class="weui_media_title">
                        <c:if test="${jokeMap.type eq 'text'}">
                            ${jokeMap.subContent}
                        </c:if>
                        <c:if test="${jokeMap.type eq 'pic'}">
                            ${jokeMap.content}
                        </c:if>
                    </h4>
                    <p class="weui_media_desc">
                        <c:if test="${jokeMap.type eq 'text'}">
                            ${jokeMap.content}
                        </c:if>
                        <c:if test="${jokeMap.type eq 'pic'}">
                            <img src="${jokeMap.pic}" alt="速查服务" style="max-height: 300px;" onclick="openImg('${jokeMap.pic}','${jokeMap.content}')">
                            <%--<p style="height: 200px;background: url('${jokeMap.pic}') 0 0 no-repeat scroll transparent;background-size:100% 100%;"></p>--%>
                        </c:if>
                    </p>
                    <ul class="weui_media_info">
                        <li class="weui_media_info_meta">速查服务</li>
                        <li class="weui_media_info_meta">${jokeMap.create_time}</li>
                        <%--<li class="weui_media_info_meta weui_media_info_meta_extra">点击查看详情</li>--%>
                    </ul>
                </div>
            </c:forEach>
        </div>
    </div>
</div>

<div class="weui-infinite-scroll">
    <div class="infinite-preloader"></div><!-- 菊花 -->
    正在加载... <!-- 文案，可以自行修改 -->
</div>

</body>
</html>