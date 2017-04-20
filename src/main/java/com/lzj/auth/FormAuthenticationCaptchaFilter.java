package com.lzj.auth;

import com.lzj.op.Manager;
import com.lzj.service.ManageService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

public class FormAuthenticationCaptchaFilter extends FormAuthenticationFilter {

	@Resource
	private ManageService manageService;
	
	public static final String DEFAULT_CAPTCHA_PARAM = "captcha";

	private String captchaParam = DEFAULT_CAPTCHA_PARAM;

	public String getCaptchaParam() {

		return captchaParam;

	}

	protected String getCaptcha(ServletRequest request) {

		return WebUtils.getCleanParam(request, getCaptchaParam());

	}

	protected AuthenticationToken createToken(ServletRequest request,
			ServletResponse response) {

		String username = getUsername(request);

		String password = getPassword(request);

		String captcha = getCaptcha(request);

		boolean rememberMe = isRememberMe(request);

		String host = getHost(request);

		return new UsernamePasswordCaptchaToken(username,

		password.toCharArray(), rememberMe, host, captcha);

	}
	
	// 登录成功操作,这里设置了代理商常用信息
		@Override
		protected boolean onLoginSuccess(AuthenticationToken token,
				Subject subject, ServletRequest request, ServletResponse response)
				throws Exception {
			System.out.println("========onLoginSuccess==========");
			Manager manager = (Manager) SecurityUtils.getSubject().getPrincipal();
			//HttpSession session = WebUtils.toHttp(request).getSession(true);
			//session.setAttribute("manager", manager);
			//session.setAttribute("power", userService.checkPower(bossUser.getId()));
			Map<String, String> param = new HashMap<String, String>();
			WebUtils.issueRedirect(request, response, getSuccessUrl(), param, true);
			// save log
			//复位该用户密码错误次数
			//userService.failTimesReset(bossUser.getUserName());
			//String ip = request.getRemoteAddr();
			//userService.saveLog(ip, bossUser);
			manageService.updateManagerLastLogin(manager.getId());

			return false;
		}

}
