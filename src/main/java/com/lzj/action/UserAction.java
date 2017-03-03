package com.lzj.action;

import com.lzj.service.ApiService;
import com.lzj.service.UserService;
import com.lzj.service.WeiXinService;
import com.lzj.utils.ALiYunOssUtil;
import com.lzj.utils.LZJUtil;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/10.
 */
@Controller
@RequestMapping(value = "/user")
public class UserAction extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(UserAction.class);

    @Resource
    private UserService userService;
    @Resource
    private WeiXinService weixinService;
    @Resource
    private ApiService apiService;

    public UserAction(){}

    public UserAction(UserService userService,ApiService apiService){
        this.userService = userService;
        this.apiService = apiService;
    }

    public Map<String,Object> insertUser(Map<String,Object> userInfoMap){
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
                insertUserMap.put("user_no",getUserNo());
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
    }

    /**
     * 打赏
     * @param model
     * @param params
     * @return
     */
    @RequestMapping(value="/reward",method = RequestMethod.GET)
    public String reward(final ModelMap model,@RequestParam Map<String, String> params){
        log.info("--------reward-----params=" + params);
        try {
            String userNo = params.get("userNo");
            Map<String,Object> whereMap = new HashMap<String, Object>();
            whereMap.put("user_no",userNo);
            Map<String,Object> userMap = apiService.getOneMethod("user", whereMap, "id", "desc", 0);
            if(userMap==null || userMap.isEmpty()){
                model.put("errorMsg", "无此用户");
                model.put("errorCode", userNo);
                return "errorPage";
            }else{
                model.put("userMap", userMap);
                return "user/reward";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.put("errorMsg", "系统异常");
            model.put("errorCode", "100000000");
            return "errorPage";
        }
    }

    @RequestMapping(value="/userInfo",method = RequestMethod.GET)
    public String userInfo(final ModelMap model,@RequestParam Map<String, String> params){
        log.info("--------userInfo-----params=" + params);
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
                model.put("errorMsg", "无此用户");
                model.put("errorCode", userNo);
                return "errorPage";
            }else{
                userMap.put("encryptUserNo",encryptUserNo);
                model.put("userMap", userMap);
                return "user/userInfo";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "user/userInfo";
    }

    @RequestMapping(value="refreshUserInfo",method = RequestMethod.GET)
    public void refreshUserInfo(@RequestParam Map<String,String> params,HttpServletResponse response){
        log.info("--------更新用户信息------params=" + params);
        String openid = params.get("openid");
        Map<String,Object> resultMap = new HashMap<String, Object>();
        try {
            if(StringUtils.isEmpty(openid) || "null".equals(openid)){
                log.info("----------openid为空----------");
                outJson("{}", response);
                return;
            }
            Map<String,Object> userMap = userService.selectUserByOpenId(openid);
            if(userMap==null){
                log.info("----------无此用户----------");
                outJson("{}", response);
                return;
            }

            Map<String, Object> userInfoMap = new WxAction(weixinService).getUserInfoByOpenid(openid);
            if(userInfoMap==null || userInfoMap.isEmpty()){
                log.info("----------更新用户信息，用户数据为空----------");
                outJson("{}", response);
                return;
            }else{
                resultMap = refreshUserInfo(userInfoMap);
                JSONObject jsonObject = JSONObject.fromObject(resultMap);
                log.info("jsonObject=" + jsonObject.toString());
                outJson(jsonObject.toString(), response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            outJson("{}", response);
        }

    }


    public Map<String,Object> refreshUserInfo(Map<String,Object> userInfoMap){
        log.info("--------更新用户信息------userInfoMap="+userInfoMap);
        try {
            if(userInfoMap==null || userInfoMap.isEmpty()){
                return null;
            }
            String openid = String.valueOf(userInfoMap.get("openid"));
            if(StringUtils.isEmpty(openid)){
                return null;
            }
            Map<String,Object> userMap = userService.selectUserByOpenId(openid);
            if(userMap!=null && !userMap.isEmpty()){
                String nickname = String.valueOf(userInfoMap.get("nickname"));
                String sex = String.valueOf(userInfoMap.get("sex"));
                String province = String.valueOf(userInfoMap.get("province"));
                String city = String.valueOf(userInfoMap.get("city"));
                String country = String.valueOf(userInfoMap.get("country"));
                String headimgurl = String.valueOf(userInfoMap.get("headimgurl"));
                userService.refreshUserInfo(openid, nickname, sex, province, city, country, headimgurl);
                return userService.selectUserByOpenId(openid);
            }else{
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @RequestMapping(value = "/inviteCode")
    public String inviteCode(final ModelMap model, @RequestParam Map<String, String> params){
        log.info("---------到我的邀请码页-------params:"+params);
        String userNo = params.get("userNo");
        try{
            Map<String,Object> whereMap = new HashMap<String, Object>();
            whereMap.put("user_no",userNo);
            Map<String,Object> userMap = apiService.getOneMethod("user", whereMap, "id", "desc", 0);
            if(userMap==null){
                model.put("errorMsg","无此用户");
                model.put("errorCode", "10000000");
                return "errorPage";
            }
            String inviteCode = "";
            whereMap = new HashMap<String, Object>();
            whereMap.put("user_no",userNo);
            Map<String,Object> inviteMap = apiService.getOneMethod("invite_code", whereMap, "id", "desc", 0);
            if(inviteMap==null || inviteMap.isEmpty()){
                inviteCode = LZJUtil.genKey(8);
                Map<String,Object> insertInviteMap = new HashMap<String, Object>();
                insertInviteMap.put("user_no",userNo);
                insertInviteMap.put("invite_code",inviteCode);
                insertInviteMap.put("status",0);
                insertInviteMap.put("already_use_num",0);
                insertInviteMap.put("create_time", new Date());
                apiService.insertMethod("invite_code", insertInviteMap);
            }else{
                inviteCode = String.valueOf(inviteMap.get("invite_code"));
            }
            model.put("userMap", userMap);
            model.put("inviteCode", inviteCode);
            return "user/inviteCode";
        }catch(Exception e){
            log.info(e.getMessage());
            e.printStackTrace();
        }
        return "user/inviteCode";
    }

    @RequestMapping(value="/zfbCode",method = RequestMethod.GET)
    public String zfbCode(final ModelMap model,@RequestParam Map<String, String> params){
        log.info("--------zfbCode-----params=" + params);
        try {
            String userNo = params.get("userNo");
            Map<String,Object> whereMap = new HashMap<String, Object>();
            whereMap.put("user_no", userNo);
            Map<String,Object> userMap = apiService.getOneMethod("user", whereMap, "id", "desc", 0);
            if(userMap==null || userMap.isEmpty()){
                model.put("errorMsg","无此用户");
                model.put("errorCode",userNo);
                return "errorPage";
            }else{
                Map<String,Object> fingCodeWhereMap = new HashMap<String, Object>();
                fingCodeWhereMap.put("user_no", userNo);
                fingCodeWhereMap.put("status", 1);
                Map<String,Object> zfbCodeMap = apiService.getOneMethod("ali_oss_file", fingCodeWhereMap, "id", "desc", 0);
                if(zfbCodeMap==null || zfbCodeMap.isEmpty()){
                    model.put("zfbCodeUrl","");
                }else{
                    String source = String.valueOf(zfbCodeMap.get("source"));
                    String fileName = String.valueOf(zfbCodeMap.get("file_name"));
                    if(ALiYunOssUtil.exists("lzj-fcheck", source+"/"+fileName)) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String expiresDateStr = "2018-01-01 23:59:59";
                        String zfbCodeUrl = ALiYunOssUtil.genUrl("lzj-fcheck", source + "/" + fileName, sdf.parse(expiresDateStr));
                        log.info("zfbCodeUrl=" + zfbCodeUrl);
                        model.put("zfbCodeUrl",zfbCodeUrl);
                    }else{
                        model.put("zfbCodeUrl","");
                    }
                }
                model.put("userMap", userMap);
                return "user/zfbCode";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "user/zfbCode";
    }

    @RequestMapping(value="getZFBCode",method = RequestMethod.GET)
    public void getZFBCode(@RequestParam Map<String,String> params,HttpServletResponse response){
        log.info("--------getZFBCode------params=" + params);
        String userNo = params.get("userNo");
        Map<String,Object> resultMap = new HashMap<String, Object>();
        try {
            if(StringUtils.isEmpty(userNo) || "null".equals(userNo)){
                log.info("----------userNo为空----------");
                String jsonStr = buildJson("FAIL",null,"相关信息为空");
                outJson(jsonStr, response);
                return ;
            }
            Map<String,Object> whereMap = new HashMap<String, Object>();
            whereMap.put("user_no",userNo);
            Map<String,Object> userMap = apiService.getOneMethod("user", whereMap, "id", "desc", 0);
            if(userMap==null){
                log.info("----------无此用户----------");
                String jsonStr = buildJson("FAIL",null,"无此用户");
                outJson(jsonStr, response);
                return ;
            }
            Map<String,Object> getZFBCodeWhereMap = new HashMap<String, Object>();
            getZFBCodeWhereMap.put("source","802576682");
            getZFBCodeWhereMap.put("file_type","ZFB_CODE");
            getZFBCodeWhereMap.put("status",0);
            Map<String,Object> zfbCodeMap = apiService.getOneMethod("ali_oss_file",getZFBCodeWhereMap,"id","asc",1);
            if(zfbCodeMap!=null && !zfbCodeMap.isEmpty()){
                String id = String.valueOf(zfbCodeMap.get("id"));
                String source = String.valueOf(zfbCodeMap.get("source"));
                String fileName = String.valueOf(zfbCodeMap.get("file_name"));
                log.info("fileName=" + fileName);
                if(ALiYunOssUtil.exists("lzj-fcheck", source+"/"+fileName)){
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String expiresDateStr = "2018-01-01 23:59:59";
                    String zfbCodeUrl = ALiYunOssUtil.genUrl("lzj-fcheck", source+"/"+fileName, sdf.parse(expiresDateStr));
                    log.info("zfbCodeUrl="+zfbCodeUrl);
                    Map<String,Object> updateWhereMap = new HashMap<String, Object>();
                    updateWhereMap.put("id", id);
                    Map<String,Object> updateMap = new HashMap<String, Object>();
                    updateMap.put("status",1);
                    updateMap.put("user_no", userNo);
                    updateMap.put("use_time", new Date());
                    apiService.updateMethod("ali_oss_file", updateMap, updateWhereMap);
                    String jsonStr = buildJson("SUCCESS",zfbCodeUrl,"");
                    outJson(jsonStr, response);
                    return ;
                }else{
                    log.info("----------库存无此激活码----------");
                    String jsonStr = buildJson("FAIL",null,"库存无此激活码");
                    outJson(jsonStr, response);
                    return ;
                }
            }else{
                log.info("----------无激活码----------");
                String jsonStr = buildJson("FAIL",null,"暂无激活码");
                outJson(jsonStr, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            String jsonStr = buildJson("FAIL",null,"系统异常");
            outJson(jsonStr, response);
        }

    }

    /**
     * 每天凌晨刷新用户今日是否签到的定时任务、清空订阅消息推送记录、插入笑话、校验用户订阅到期记录
     */
    public void refreshSign(){
        log.info("-------每天凌晨定时任务--------");
        try {
            //新用户今日是否签到
            userService.refreshSign();
            //清空订阅消息推送记录
            apiService.updateSubCategory(null, 0);
            //插入笑话
            new ApiAction(apiService).insertNewJoke("all");
            //校验用户订阅到期记录
            userService.checkSubArrive();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成用户编号，9位
     * @return
     * @throws Exception
     */
    public String getUserNo(){
        String nowTimeStr = String.valueOf(System.currentTimeMillis());
        String userNo = "80"+nowTimeStr.substring(nowTimeStr.length()-4)+String.valueOf(Math.round(Math.random()*900+100));
        Map<String,Object> whereMap = new HashMap<String, Object>();
        whereMap.put("user_no", userNo);
        Map<String,Object> userMap = apiService.getOneMethod("user", whereMap, "id", "desc", 0);
        if(userMap==null || userMap.isEmpty()){
            return userNo;
        }else{
            return getUserNo();
        }
    }


}
