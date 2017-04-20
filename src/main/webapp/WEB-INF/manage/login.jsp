<%@ page import="org.apache.shiro.web.filter.authc.FormAuthenticationFilter" %>
<%@ page language="java" pageEncoding="utf-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en" class="no-js">

    <head>

        <meta charset="utf-8">
        <title>Login</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="description" content="">
        <meta name="author" content="">

        <!-- CSS -->
        <!-- <link rel='stylesheet' href='http://fonts.googleapis.com/css?family=PT+Sans:400,700'> -->
        <link rel="stylesheet" href="${ctx }/assets/css/reset.css">
        <link rel="stylesheet" href="${ctx }/assets/css/supersized.css">
        <link rel="stylesheet" href="${ctx }/assets/css/style.css">
        <script type="text/javascript" src="${ ctx}/js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="${ ctx}/js/jquery.md5.js"></script>

        <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
        <!--[if lt IE 9]>
            <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
        <![endif]-->
        <script type="text/javascript">
        	$(function(){
                var msg = "";
                var error = "${shiroLoginFailure}";
                var flag = "${LOGINFLAG}";
                console.log("error="+error+",flag="+flag);
                if(!isNullOrEmpty(flag)){
                    if(flag.indexOf("NoUpdatePasswordException")>-1){
                        msg = "口令过期";
                    }else if(flag.indexOf("USERISLOGINING")>-1){
                        msg = "该用户已经登录";
                    }
                }else if(!isNullOrEmpty(error)){
                    if(error.indexOf("UnknownAccountException")>-1){
                        msg = "用户状态异常或用户名不存在";
                    }
                    if(error.indexOf("FailTimesException")>-1){
                        msg = "密码错误次数超限";
                    }
                    if(error.indexOf("IncorrectCredentialsException")>-1){
                        msg = "用户名或密码错误";
                    }
                    if(error.indexOf("CaptchaException")>-1){
                        msg = "验证码错误，请重试.";
                    }
                }
                console.log("msg="+msg);
                if(!isNullOrEmpty(msg)){
                    alert(msg);
                }
        	})
        
        
	        function refreshCaptcha() {
				var _captcha_id = document.getElementById("img_captcha");
				_captcha_id.src="${ctx }/servlet/captchaCode?t=" + Math.random();
			}

            function checkForm(){
                var username = $("#username").val();
                var password = $("#password").val();
                var captcha = $("#captcha").val();
                if(isNullOrEmpty(username)){
                    alert("用户名不能为空");
                    return false;
                }
                if(isNullOrEmpty(password)){
                    alert("密码不能为空");
                    return false;
                }
                if(isNullOrEmpty(captcha)){
                    alert("验证码不能为空");
                    return false;
                }
                $("#password").val($.md5(password));
                return true;
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

        <div class="page-container">
            <h1>Login</h1>
            <form action="${ctx }/manage/login" method="post" onsubmit = "return checkForm();">
                <input type="text" name="username" id="username" class="username" placeholder="Username" maxlength="20" required="required">
                <input type="password" name="password" id="password" class="password" placeholder="Password" maxlength="20" required="required">
				<div style="">
					<input type="text" style="width: 51%;"  placeholder="验证码" name="captcha" id="captcha" maxlength="4" required="required">
					<img title="点击更换" style="width: 35%;vertical-align: bottom;padding-bottom: 5px;" id="img_captcha"  onclick="javascript:refreshCaptcha();" src="${ctx }/servlet/captchaCode" />
				</div>
                <button type="submit">Sign me in</button>
                <div class="error"><span>+</span></div>
            </form>
            <div class="connect">
                <!-- <p>Or connect with:</p>
                <p>
                    <a class="facebook" href=""></a>
                    <a class="twitter" href=""></a>
                </p> -->
            </div>
        </div>
        <div align="center">Collect from <a href="http://www.lanzhuwo.net" target="_blank" title="深圳市路遥里科技有限公司">深圳市路遥里科技有限公司</a></div>

        <!-- Javascript -->
        <%-- <script src="${ctx }/assets/js/jquery-1.8.2.min.js"></script> --%>
        <script src="${ctx }/assets/js/supersized.3.2.7.min.js"></script>
        <script src="${ctx }/assets/js/supersized-init.js"></script>
        <script src="${ctx }/assets/js/scripts.js"></script>

    </body>

</html>

