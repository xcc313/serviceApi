<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="user-scalable=no, width=device-width, initial-scale=1.0" />
    <c:set var="ctx" value="${pageContext.request.contextPath}" />
    <script type="text/javascript" src="${ctx}/js/jquery-1.9.1.min.js" charset="utf-8"></script>
    <link href="${ctx}/css/weui.min.css" rel="stylesheet">
    <link href="${ctx}/css/jquery-weui.min.css" rel="stylesheet">
    <link href="${ctx}/css/demos.css" rel="stylesheet">
    <script src="${ctx}/js/jquery-weui.min.js" charset="utf-8"></script>
    <script src="${ctx}/js/city-picker.min.js" charset="utf-8"></script>
    <title>新闻订阅</title>
    <style type="text/css">
        .weui_panel:before{
            border-top: 0px !important;
        }
        .surePayText{
            color: #fd8d2c !important;
        }
    </style>
    <script type="text/javascript">
        $(function(){
            var subJoke = "${subJoke}";
            if(subJoke=='0'){
                $("#isReceive").prop("checked",false);
                hideAll();
            }else if(subJoke=='1'){
                $("#isReceive").prop("checked",true);
                var subMapList = '${subMapList}';
                var itemJson = eval(subMapList);
                for(var i=0; i<itemJson.length;  i++) {
                    var item = itemJson[i];
                    console.log("item="+item);
                    var sub_index = item.sub_index;
                    $("#time"+sub_index).prop("checked",true);
                }
                showAll();
            }
        })

        function toSub(){
            var checkedIndexs = "";
            $("[name='checkbox1']:checkbox:checked").each(function(){
                checkedIndexs+=$(this).val()+",";
            });
            if(checkedIndexs==""){
                $.toast("请选择接收时区", "forbidden");
                return;
            }
            var userNo = "${userNo}";
            var needPayCoin = $("#needPayCoin").val();
            $.modal({
                title: "确认提示",
                text: "此订阅将消耗"+needPayCoin+"微币,时长为一个月",
                buttons: [
                    { text: "确认订阅", className:"surePayText", onClick: function(){ sureSub(checkedIndexs,needPayCoin); } },
                    { text: "微币充值", onClick: function(){ window.location.href='${ctx}/user/rechargeCoin?userNo='+userNo } },
                    { text: "取消", className: "default", onClick: function(){ } },
                ]
            });

        }

        function sureSub(checkedIndexs,needPayCoin){
            var userNo = "${userNo}";
            $.showLoading("正在订阅...");
            $.ajax({
                url:"${ctx}/api/jokeSubResult",
                type:"GET",
                data:{"checkedIndexs":checkedIndexs,"needPayCoin":needPayCoin,"userNo":userNo},
                success:function(data){
                    $.hideLoading();
                    if(!jQuery.isEmptyObject(data)){
                        $.alert(data.head.result_msg,function(){
                            WeixinJSBridge.call('closeWindow');
                        });
                    }else{
                        $.alert("查询异常，请稍后重试");
                    }
                },
                error:function(){
                    $.hideLoading();
                    $.alert("网络异常，请稍后重试");
                }
            })
        }

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

        function changeReceiveSwitch(){
            var isReceive = $("#isReceive").is(':checked');
            if(isReceive){
                //笑话订阅开关  0未订阅  1已订阅
                //switchChange(1);
                showAll();
            }else{
                $.confirm("确认不再接收笑话推送?", function() {
                    //点击确认后的回调函数
                    //笑话订阅开关  0未订阅  1已订阅
                    switchChange(0);
                    hideAll();
                }, function() {
                    //点击取消后的回调函数
                    $("#isReceive").prop("checked",true);
                    showAll();
                });

            }
        }

        function switchChange(newSwitchStatus){
            //笑话订阅开关  0未订阅  1已订阅
            var userNo = "${userNo}";
            if(newSwitchStatus=='0'){
                $.ajax({
                    url:"${ctx}/api/changeJokeSubSwitch",
                    type:"GET",
                    data:{"newSwitchStatus":newSwitchStatus,"userNo":userNo},
                    success:function(data){
                        if(!jQuery.isEmptyObject(data)){
                            if(data.head.result_code == 'FAIL'){
                                $.alert(data.head.result_msg);
                            }else{
                                var content = data.content;
                                $.toast(content);
                                /*if(!jQuery.isEmptyObject(content)){
                                    var sub_index,item;
                                    for(var i=0, l=content.length; i<l; i++) {
                                        item = content[i];
                                        sub_index = item.sub_index;
                                        $("#time"+sub_index).prop("checked",true);
                                    }
                                }else{
                                    //没有已订阅的记录
                                }*/
                            }

                        }else{
                        }
                    },
                    error:function(){
                        $.alert("网络异常，请稍后重试");
                    }
                })
            }
        }

        function hideAll(){
            $("#receiveTimeDiv").hide();
            $("#receiveNoteDiv").hide();
            $("#sureButtonDiv").hide();
        }

        function showAll(){
            $("#receiveTimeDiv").show();
            $("#receiveNoteDiv").show();
            $("#sureButtonDiv").show();
        }

        function changeNeedPay(){
            var checkedNum = 0;
            $("input[name='checkbox1']").each(function(){
                var isChoose = $(this).is(':checked');
                if(isChoose){
                    checkedNum++;
                }
            });
            if(checkedNum<3){
                $("#needPayCoin").val("30");
                $("#sureButton").text("确认(¥30微币)");
            }else if(checkedNum<5){
                $("#needPayCoin").val("35");
                $("#sureButton").text("确认(¥35微币)");
            }else if(checkedNum<7){
                $("#needPayCoin").val("40");
                $("#sureButton").text("确认(¥40微币)");
            }else if(checkedNum<9){
                $("#needPayCoin").val("45");
                $("#sureButton").text("确认(¥45微币)");
            }else if(checkedNum=9){
                $("#needPayCoin").val("50");
                $("#sureButton").text("确认(¥50微币)");
            }
            console.log($("#needPayCoin").val());
        }

    </script>
</head>
<body>
<header class='demos-header'>
    <h1 class="demos-title" style="font-size: 24px;">笑话订阅</h1>
    <p class='demos-sub-title'>笑一笑，十年少</p>
</header>
<div>
    <div class="weui_cells weui_cells_checkbox">
        <div class="weui_cell weui_cell_switch" style="border-bottom: 1px solid #e5e5e5;">
            <div class="weui_cell_hd weui_cell_primary">订阅接收开关</div>
            <div class="weui_cell_ft">
                <input class="weui_switch" type="checkbox" id="isReceive" onclick="changeReceiveSwitch();" checked>
            </div>
        </div>
        <input id="needPayCoin" type="text" value="30" hidden />
        <div id="receiveTimeDiv">
            <div class="weui_cells_title">请选择接收笑话时间：</div>
            <div class="weui-row">
                <div class="weui-col-33">
                    <label class="weui_cell weui_check_label" for="time1">
                        <div class="weui_cell_hd">
                            <input type="checkbox" class="weui_check" name="checkbox1" id="time1" value="1" onclick="changeNeedPay();">
                            <i class="weui_icon_checked"></i>
                        </div>
                        <div class="weui_cell_bd weui_cell_primary">
                            <p>0~6</p>
                        </div>
                    </label>
                </div>
                <div class="weui-col-33">
                    <label class="weui_cell weui_check_label" for="time2">
                        <div class="weui_cell_hd">
                            <input type="checkbox" class="weui_check" name="checkbox1" id="time2" value="2" onclick="changeNeedPay();">
                            <i class="weui_icon_checked"></i>
                        </div>
                        <div class="weui_cell_bd weui_cell_primary">
                            <p>6~8</p>
                        </div>
                    </label>
                </div>
                <div class="weui-col-33">
                    <label class="weui_cell weui_check_label" for="time3">
                        <div class="weui_cell_hd">
                            <input type="checkbox" class="weui_check" name="checkbox1" id="time3" value="3" onclick="changeNeedPay();">
                            <i class="weui_icon_checked"></i>
                        </div>
                        <div class="weui_cell_bd weui_cell_primary">
                            <p>8~9</p>
                        </div>
                    </label>
                </div>
            </div>
            <div class="weui-row">
                <div class="weui-col-33">
                    <label class="weui_cell weui_check_label" for="time4">
                        <div class="weui_cell_hd">
                            <input type="checkbox" class="weui_check" name="checkbox1" id="time4" value="4" onclick="changeNeedPay();">
                            <i class="weui_icon_checked"></i>
                        </div>
                        <div class="weui_cell_bd weui_cell_primary">
                            <p>9~10</p>
                        </div>
                    </label>
                </div>
                <div class="weui-col-33">
                    <label class="weui_cell weui_check_label" for="time5">
                        <div class="weui_cell_hd">
                            <input type="checkbox" class="weui_check" name="checkbox1" id="time5" value="5" onclick="changeNeedPay();">
                            <i class="weui_icon_checked"></i>
                        </div>
                        <div class="weui_cell_bd weui_cell_primary">
                            <p>10~12</p>
                        </div>
                    </label>
                </div>
                <div class="weui-col-33">
                    <label class="weui_cell weui_check_label" for="time6">
                        <div class="weui_cell_hd">
                            <input type="checkbox" class="weui_check" name="checkbox1" id="time6" value="6" onclick="changeNeedPay();">
                            <i class="weui_icon_checked"></i>
                        </div>
                        <div class="weui_cell_bd weui_cell_primary">
                            <p>12~15</p>
                        </div>
                    </label>
                </div>
            </div>
            <div class="weui-row">
                <div class="weui-col-33">
                    <label class="weui_cell weui_check_label" for="time7">
                        <div class="weui_cell_hd">
                            <input type="checkbox" class="weui_check" name="checkbox1" id="time7" value="7" onclick="changeNeedPay();">
                            <i class="weui_icon_checked"></i>
                        </div>
                        <div class="weui_cell_bd weui_cell_primary">
                            <p>15~18</p>
                        </div>
                    </label>
                </div>
                <div class="weui-col-33">
                    <label class="weui_cell weui_check_label" for="time8">
                        <div class="weui_cell_hd">
                            <input type="checkbox" class="weui_check" name="checkbox1" id="time8" value="8" onclick="changeNeedPay();">
                            <i class="weui_icon_checked"></i>
                        </div>
                        <div class="weui_cell_bd weui_cell_primary">
                            <p>18~19</p>
                        </div>
                    </label>
                </div>
                <div class="weui-col-33">
                    <label class="weui_cell weui_check_label" for="time9">
                        <div class="weui_cell_hd">
                            <input type="checkbox" class="weui_check" name="checkbox1" id="time9" value="9" onclick="changeNeedPay();">
                            <i class="weui_icon_checked"></i>
                        </div>
                        <div class="weui_cell_bd weui_cell_primary">
                            <p>19~24</p>
                        </div>
                    </label>
                </div>
            </div>
        </div>
    </div>
    <div id="receiveNoteDiv" class="weui_panel weui_panel_access">
        <div class="weui_panel_bd">
            <div class="weui_media_box weui_media_text">
                <h4 class="weui_media_title">说明：</h4>
                <p class="weui_media_desc">&nbsp;&nbsp;&nbsp;&nbsp;每次订阅时长为一个月。</p>
                <p class="weui_media_desc">&nbsp;&nbsp;&nbsp;&nbsp;系统会在您选择的接收笑话时区内给您推送笑话，每个时区推送一次，每次推送5条。</p>
            </div>
        </div>
    </div>
    <div id="sureButtonDiv" class="weui_btn_area" style="margin-bottom: 40px;">
        <a href="javascript:toSub();"  class="weui_btn weui_btn_primary" id="sureButton">确认(¥30微币)</a>
    </div>

    <div class="weui_msg">
        <div class="weui_extra_area">
            <a href="${ctx}/user/rechargeCoin?userNo=${userNo}">查看微币规则详情</a>
        </div>
    </div>
</div>

</body>
</html>