package com.lzj.auth;

import com.lzj.captcha.CaptchaServlet;
import com.lzj.op.Manager;
import com.lzj.service.ManageService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 登录系统后，对用户进行检验，包括严重和授权
 * 
 * @author dj
 * 
 */
@Component
public class ShiroDbRealm extends AuthorizingRealm {

	private static final Logger log = LoggerFactory.getLogger(ShiroDbRealm.class);

	@Autowired
	private ManageService manageService;

	// 设置密码加密方式为MD5
	public ShiroDbRealm() {
		//HashedCredentialsMatcher matcher = new HashedCredentialsMatcher(Md5Hash.ALGORITHM_NAME);
		//setCredentialsMatcher(matcher);
	}

	// 用户验证
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
		UsernamePasswordCaptchaToken token = (UsernamePasswordCaptchaToken) authcToken;
		System.out.println("doGetAuthenticationInfo====="+token.getUsername() + "=====");
		
		String captcha = token.getCaptcha();

		if (authcToken.getPrincipal() == null)
			return null;
		log.info("User login: {}", authcToken.getPrincipal());
		Manager manager = null;
		try {
			manager = manageService.getByUserName((String) authcToken.getPrincipal());
		} catch (Exception e) {
			log.error("query user exception", e);
		}

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

		if (manager == null) {
			request.setAttribute(FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME, "IncorrectCredentialsException");
			return null;
		}
		// 增加判断验证码逻辑
		//如果用户输错密码2次以上，就需要验证验证码是否正确
//		if (user != null&&user.getFailTimes()>MainController.MAX_PASSWORD_ERROR_COUNT) {
			String exitCode = (String) SecurityUtils.getSubject().getSession().getAttribute(CaptchaServlet.KEY_CAPTCHA);
			System.out.println("doGetAuthenticationInfo=====exitCode====="+exitCode + "=====");
			if (null == captcha || !captcha.equalsIgnoreCase(exitCode)) {
				throw new CaptchaException("验证码错误");
			}
//		}
		// 暂停密码定时更改验证
		/*
		 * if(!isUpdatePw(user.getUpdatePasswTime())){
		 * request.setAttribute("LOGINFLAG", "NoUpdatePasswordException");
		 * return null; }
		 */
		if (manager != null && Integer.parseInt(manager.getStatus()) == 0) {
			request.setAttribute(FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME, "UnknownAccountException");
			return null;
		}
		return new SimpleAuthenticationInfo(manager, manager.getPwd(), getName());
	}

	private boolean isUpdatePw(Date updateTime) {
		if (updateTime == null) {
			return false;
		}
		long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
		long diff = new Date().getTime() - updateTime.getTime();
		if ((diff / nd) > 30) {
			return false;
		}
		return true;

	}

	// 用户授权
	// TODO
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		return info;
	}

	@Override
	public boolean supports(AuthenticationToken token) {
		return super.supports(token);
	}

}
