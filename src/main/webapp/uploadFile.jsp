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
    <script src="${ctx}/js/weuiUpload.js"></script>
    <title>上传文件</title>
    <style type="text/css">

    </style>

    <script type="text/javascript">

        function submitForm(){
            var uploadFilesLi = $(".weui_uploader_file");
            var uploadFiles = "";
            for(var i=0;i<uploadFilesLi.length;i++) {
                var tmpFile = uploadFilesLi.eq(i).css('background-image');
                tmpFile = tmpFile.replace('url(','').replace(')','').replace(/\"/gi, "");
                uploadFiles=uploadFiles+tmpFile+";@;";
            }
            $.showLoading();
            $.showLoading("正在上传...");
            console.log(uploadFiles);
            $.ajax({
                async:false,
                type: "POST",
                url:'${ctx}/test/uploadOSSFile',
                data: {
                    uploadFiles: uploadFiles,
                    userNo: '802576682'
                },
                success: function(data) {
                    $.hideLoading();
                    if(!jQuery.isEmptyObject(data)){
                        var params = eval(data);
                        var resultSuccess = params.success;
                        var resultMsg = params.msg;
                        $.alert("resultSuccess="+resultSuccess+",resultMsg="+resultMsg);
                    }else{
                        $.toast("系统异常", "forbidden");
                    }
                },
                error: function(request) {
                    $.hideLoading();
                    $.toast("网络异常", "forbidden");
                }
            });
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
<div class="weui_cells_title" style="margin-top: 40px;">选择文件</div>

<form method="post" action="${ctx}/test/uploadOSSFile"  enctype="multipart/form-data">
    <input type="text" value="802576682" id="userNo" name="userNo" style="display: none">
    <input id="input-dim-2" type="file" multiple="multiple" name="imageFile[]">
    <button type="submit" >确定</button>
</form>

</body>
</html>
