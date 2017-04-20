<%@ page language="java" pageEncoding="utf-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE HTML>
<html>
<head>
</head>
<body>
     <!-- Navigation -->
        <nav class="top1 navbar navbar-default navbar-static-top" role="navigation" style="margin-bottom: 0;">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="${ctx }/user/managerLogin.do">Just do it</a>
            </div>
            
            
            <!-- /.navbar-header -->
            <ul class="nav navbar-nav navbar-right">
			    <li class="dropdown">
	        		<a href="#" class="dropdown-toggle avatar" data-toggle="dropdown"><img src="${ctx }/assets/img/managerIcon.png"><span class="badge">666</span></a>
	        		<ul class="dropdown-menu">
						<li class="dropdown-menu-header text-center">
							<strong>个人信息</strong>
						</li>
						<li class="m_2"><a href="#"><i class="fa fa-bell-o"></i> 暂未设置 <span class="label label-info">666</span></a></li>
						<li class="m_2"><a href="#"><i class="fa fa-envelope-o"></i> 暂未设置 <span class="label label-success">666</span></a></li>
						<li class="m_2"><a href="#"><i class="fa fa-tasks"></i> 暂未设置 <span class="label label-danger">666</span></a></li>
						<li><a href="#"><i class="fa fa-comments"></i> 暂未设置 <span class="label label-warning">666</span></a></li>
						<!-- <li class="dropdown-menu-header text-center">
							<strong>Settings</strong>
						</li>
						<li class="m_2"><a href="#"><i class="fa fa-user"></i> Profile</a></li>
						<li class="m_2"><a href="#"><i class="fa fa-wrench"></i> Settings</a></li>
						<li class="m_2"><a href="#"><i class="fa fa-usd"></i> Payments <span class="label label-default">42</span></a></li>
						<li class="m_2"><a href="#"><i class="fa fa-file"></i> Projects <span class="label label-primary">42</span></a></li>
						<li class="divider"></li>
						<li class="m_2"><a href="#"><i class="fa fa-shield"></i> Lock Profile</a></li>
						<li class="m_2"><a href="#"><i class="fa fa-lock"></i> Logout</a></li>	 -->
	        		</ul>
	      		</li>
			</ul>
            
            
            
            <div class="navbar-default sidebar" role="navigation">
                <div class="sidebar-nav navbar-collapse">
                    <ul class="nav" id="side-menu">
                        <li>
                            <a href="${ctx }/manage/main"><i class="fa fa-dashboard fa-fw nav_icon"></i>管理后台</a>
                        </li>
                        <li>
                            <a href="#"><i class="fa fa-laptop nav_icon"></i>商户管理<span class="fa arrow"></span></a>
                            <ul class="nav nav-second-level">
                                <li>
                                    <a href="${ctx }/goods/category.do">商户查询</a>
                                </li>
                            </ul>
                            <!-- /.nav-second-level -->
                        </li>
                         <li>
                            <a href="#"><i class="fa fa-check-square-o nav_icon"></i>交易管理<span class="fa arrow"></span></a>
                            <ul class="nav nav-second-level">
                                <li>
                                    <a href="${ctx }/goods/intoGoods.do">订单查询</a>
                                </li>
                                <li>
                                    <a href="${ctx }/goods/onlineOrOffline.do">提现查询</a>
                                </li>
                                <li>
                                    <a href="${ctx }/manage/returnExtraction">冲正查询</a>
                                </li>
                                <li>
                                    <a href="${ctx }/goods/onlineOrOffline.do">流水查询</a>
                                </li>
                            </ul>
                            <!-- /.nav-second-level -->
                        </li>
                        <li>
                            <a href="#"><i class="fa fa-table nav_icon"></i>风险管理<span class="fa arrow"></span></a>
                            <ul class="nav nav-second-level">
                                <li>
                                    <a href="${ctx }/goods/goodsOrder.do">黑名单管理</a>
                                </li>
                            </ul>
                            <!-- /.nav-second-level -->
                        </li>
                    </ul>
                </div>
                <!-- /.sidebar-collapse -->
            </div>
            <!-- /.navbar-static-side -->
        </nav>
</body>
</html>
