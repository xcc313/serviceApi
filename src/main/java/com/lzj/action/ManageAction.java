package com.lzj.action;

import com.lzj.captcha.CaptchaServlet;
import com.lzj.op.Manager;
import com.lzj.service.ApiService;
import com.lzj.service.ManageService;
import com.lzj.service.UserService;
import com.lzj.service.WeiXinService;
import com.lzj.utils.ALiYunOssUtil;
import com.lzj.utils.LZJUtil;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/10.
 */
@Controller
@RequestMapping(value = "/manage")
public class ManageAction extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(ManageAction.class);

    @Resource
    private ManageService manageService;

    /**
     * 转到登录页面
     * @return
     */
    @RequestMapping(value="login")
    public String toLogin(){
        log.info("----toLogin-----");
        return "manage/login";
    }


    /**
     * 首页
     * @param request
     * @param model
     * @return
     */
    @RequestMapping(value = "main")
    public String managerLogin(@RequestParam Map<String, String> params,HttpServletRequest request,final ModelMap model) {
        log.info("------main------params="+params);
        try {
            Manager manager = (Manager) SecurityUtils.getSubject().getPrincipal();
            if(manager==null){
                log.info("-----------无此用户--------------");
                return "manage/login";
            }
            log.info("-----------有此用户" + manager.getUserName() + "--------------");
            model.put("allUserNum",manageService.selectAllUserNum());
            model.put("addUserNum",manageService.selectTodayAddUserNum());
            return "manage/manageMain";

        } catch (Exception e) {
            e.printStackTrace();
            return "manage/login";
        }

    }

    @RequestMapping(value = "returnExtraction")
    public String returnExtraction(@RequestParam Map<String, String> params,HttpServletRequest request,final ModelMap model) {
        log.info("------returnExtraction------params=" + params);
        try {
            Manager manager = (Manager) SecurityUtils.getSubject().getPrincipal();
            if(manager==null){
                log.info("-----------无此用户--------------");
                return "manage/login";
            }
            log.info("-----------有此用户" + manager.getUserName() + "--------------");
            model.put("returnExtractionList",manageService.selectReturnExtraction());
            return "manage/returnExtraction";

        } catch (Exception e) {
            e.printStackTrace();
            model.put("errorMsg", "系统异常");
            model.put("errorCode", "re100000000");
            return "errorPage";
        }

    }

    @RequestMapping(value = "operReturnExtraction", method = RequestMethod.POST)
    public void operReturnExtraction(HttpServletResponse response, @RequestParam Map<String, String> params) {
        log.info("---------operReturnExtraction---------params====" + params);
        String jsonStr = "";
        try {
            Manager manager = (Manager) SecurityUtils.getSubject().getPrincipal();
            log.info("-----------有此用户" + manager.getUserName() + "--------------");
            String cashId = params.get("cashId");
            String operator = manager.getUserName();
            String operateType = params.get("operateType"); // 0审核通过  1审核不通过
            String checkMsg = params.get("checkMsg");
            if(isEmpty(cashId) || isEmpty(operator) || isEmpty(operateType) || isEmpty(checkMsg)){
                jsonStr = buildJson("FAIL",null,"必要信息为空");
                outJson(jsonStr, response);
                return ;
            }
            Map<String,Object> purseCashMap = manageService.selectPurseCash(cashId);
            if(purseCashMap==null || purseCashMap.isEmpty()){
                jsonStr = buildJson("FAIL",null,"无此提现信息");
                outJson(jsonStr, response);
                return ;
            }
            String orderStatus = String.valueOf(purseCashMap.get("order_status"));
            String cashStatus = String.valueOf(purseCashMap.get("cash_status"));
            String isBack = String.valueOf(purseCashMap.get("is_back"));
            if(("1".equals(orderStatus) && !"2".equals(cashStatus)) || !"1".equals(isBack)){
                jsonStr = buildJson("FAIL",null,"无需冲正");
                outJson(jsonStr, response);
                return ;
            }
            Map<String,Object> returnExtractionMap = manageService.selectReturnExtractionByCashId(cashId);
            if(returnExtractionMap==null || returnExtractionMap.isEmpty()){
                jsonStr = buildJson("FAIL",null,"无此冲正信息");
                outJson(jsonStr, response);
                return ;
            }
            String checkStatus = String.valueOf(returnExtractionMap.get("check_status"));
            if(!"0".equals(checkStatus)){
                jsonStr = buildJson("FAIL",null,"此冲正信息已审核");
                outJson(jsonStr, response);
                return ;
            }
            if("0".equals(operateType)){
                log.info("冲正审核通过");
                Map<String,Object> extractionResultMap = manageService.returnExtractionCheckPass(cashId,operator);
                if(extractionResultMap!=null && !extractionResultMap.isEmpty()){
                    Boolean success = Boolean.parseBoolean(String.valueOf(extractionResultMap.get("success")));
                    String msg = String.valueOf(extractionResultMap.get("msg"));
                    if(success){
                        jsonStr = buildJson("SUCCESS",null,"冲正成功");
                        outJson(jsonStr, response);
                        return ;
                    }else{
                        jsonStr = buildJson("FAIL",null,msg);
                        outJson(jsonStr, response);
                        return ;
                    }
                }
            }else if("1".equals(operateType)){
                log.info("冲正审核不通过");
                int updateRow = manageService.returnExtractionCheckFail(cashId,operator,checkMsg);
                log.info("updateRow="+updateRow);
                if(updateRow==1){
                    jsonStr = buildJson("SUCCESS",null,"冲正审核不通过成功");
                    outJson(jsonStr, response);
                    return ;
                }
            }
            jsonStr = buildJson("FAIL",null,"冲正异常");
            outJson(jsonStr, response);
            return ;
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            jsonStr = buildJson("FAIL",null,"系统异常");
            outJson(jsonStr, response);
            return ;
        }finally {
            log.info("冲正审核结果，jsonStr="+jsonStr);
        }
    }

    @RequestMapping(value = "returnExtractionDesc", method = RequestMethod.GET)
    public void returnExtractionDesc(HttpServletResponse response, @RequestParam Map<String, String> params) {
        log.info("---------returnExtractionDesc---------params====" + params);
        try {
            String returnExtractionId = params.get("returnExtractionId");
            Map<String,Object> retuanExtractionMap = manageService.selectReturnExtraction(returnExtractionId);
            Map<String,Object> purseCashMap = manageService.selectPurseCash(String.valueOf(retuanExtractionMap.get("cash_id")));
            purseCashMap.put("check_status",retuanExtractionMap.get("check_status"));
            String jsonStr = buildJson("SUCCESS",purseCashMap,"");
            log.info("jsonStr===="+jsonStr);
            outJson(jsonStr, response);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            String jsonStr = buildJson("FAIL",null,"系统异常");
            outJson(jsonStr, response);
        }
    }

    /*public Map<String,Object> insertUser(Map<String,Object> userInfoMap){
        log.info("插入用户,userInfoMap:"+userInfoMap);
        if(userInfoMap==null || userInfoMap.isEmpty()){
            return null;
        }
        String openid = String.valueOf(userInfoMap.get("openid"));
        if(StringUtils.isEmpty(openid)){
            return null;
        }
        try {
            Map<String,Object> userMap = userService.selectUserByOpenId(openid);
            if(userMap==null || userMap.isEmpty()){
                String nickname = String.valueOf(userInfoMap.get("nickname"));
                String sex = String.valueOf(userInfoMap.get("sex"));
                String province = String.valueOf(userInfoMap.get("province"));
                String city = String.valueOf(userInfoMap.get("city"));
                String country = String.valueOf(userInfoMap.get("country"));
                String headimgurl = String.valueOf(userInfoMap.get("headimgurl"));
                String privilege = String.valueOf(userInfoMap.get("privilege"));//用户特权信息，json 数组，如微信沃卡用户为（chinaunicom）
                String unionid = String.valueOf(userInfoMap.get("unionid"));//只有在用户将公众号绑定到微信开放平台帐号后，才会出现该字段。
                Map<String,Object> insertUserMap = new HashMap<String, Object>();
                insertUserMap.put("status","NORMAL");
                insertUserMap.put("nickname",nickname);
                insertUserMap.put("openid",openid);
                insertUserMap.put("sex",sex);
                insertUserMap.put("province",province);
                insertUserMap.put("city",city);
                insertUserMap.put("country",country);
                insertUserMap.put("headimgurl",headimgurl);
                insertUserMap.put("unionid",unionid);
                insertUserMap.put("create_time", new Date());
                insertUserMap.put("wei_coin", 5);
                int userId = apiService.insertMethod("user", insertUserMap);
                if(userId==0){
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userService.selectUserByOpenId(openid);
    }

    @RequestMapping(value="/rechargeCoin",method = RequestMethod.GET)
    public String rechargeCoin(final ModelMap model,@RequestParam Map<String, String> params){
        log.info("--------rechargeCoin-----params=" + params);
        String encryptUserNo = params.get("userNo");
        String userNo = "";
        try {
            userNo = decryptUserNo(encryptUserNo);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("--------风险操作,警告一次，下次关闭商户-----");
            try {
                userService.insertOperationLog("", "risk", "user", "userInfo,params=" + params);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            model.put("errorMsg", "风险操作,警告一次，下次关闭商户");
            model.put("errorCode", "1001");
            return "errorPage";
        }
        try {
            Map<String,Object> whereMap = new HashMap<String, Object>();
            whereMap.put("user_no",userNo);
            Map<String,Object> userMap = apiService.getOneMethod("user", whereMap, "id", "desc", 0);
            if(userMap==null || userMap.isEmpty()){
                model.put("errorMsg","无此用户");
                model.put("errorCode",userNo);
                return "errorPage";
            }else{
                model.put("userMap", userMap);
                return "user/rechargeCoin";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.put("errorMsg","系统异常");
            model.put("errorCode", "100000000");
            return "errorPage";
        }
    }*/


}
