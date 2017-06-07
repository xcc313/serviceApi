package com.lzj.action;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.lzj.op.WeiXinPayOrder;
import com.lzj.redis.RedisCallback;
import com.lzj.redis.RedisClientTemplate;
import com.lzj.redis.RedisDao;
import com.lzj.service.ApiService;
import com.lzj.service.UserService;
import com.lzj.service.WeiXinService;
import com.lzj.utils.*;
import com.lzj.utils.Base64;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import redis.clients.jedis.ShardedJedis;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.*;

/**
 * Created by Administrator on 2016/7/8.
 */
@Controller
@RequestMapping(value = "/test")
public class TestAction  extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(TestAction.class);
    private String appId = "wx0e5917abd2d73550";
    private String appsecret = "ee92f217101f4a00b333551b5f26ebdf";
    private String apikey = "luzijunweixinqun2016030488888888";
    private String merchantID = "1312831201";


    /*@Resource
    private RedisDao redisDao;*/
    @Resource
    private UserService userService;
    @Resource
    private ApiService apiService;

    /**
     * 微信支付-统一下单
     * @param model
     * @param params
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "testWeixinPay.do")
    public void testWeixinPay(final ModelMap model,@RequestParam Map<String, String> params,HttpServletRequest request,HttpServletResponse response){
        log.info("---------微信支付统一下单方法---------"+params);
        String amountTemp = params.get("amount");
        try {
            String nonceStr = String.valueOf(WeiXinPay.buildRandom(10));
            String out_trade_no = String.valueOf(System.currentTimeMillis()) + String.valueOf(Math.round(Math.random() * 90000 + 10000));
            //String body = "微信群二维码置顶";
            String body = "weixinqun_home pay-test";
            WeiXinPayOrder weixinPayOrder = new WeiXinPayOrder();
            weixinPayOrder.setAppid(appId);
            weixinPayOrder.setMch_id(merchantID);
            weixinPayOrder.setOut_trade_no(out_trade_no);
            weixinPayOrder.setBody(body);
            weixinPayOrder.setTotal_fee((new BigDecimal(amountTemp).multiply(new BigDecimal(100)).intValue()));//分
            weixinPayOrder.setNotify_url("http://183.239.131.218/testAction/weixinPayCallback.do");
            weixinPayOrder.setTrade_type("NATIVE");
            weixinPayOrder.setSpbill_create_ip(request.getRemoteAddr());
            weixinPayOrder.setNonce_str(nonceStr);

            Map<String, String> addOrderResultMap = WeiXinPay.addOrder(weixinPayOrder,apikey);
            log.info("--------addOrderResultMap---------------"+addOrderResultMap);
            if(addOrderResultMap==null){
                log.info("--------微信统一下单失败-----------");
                outJson("{}", response);
                return;
            }
            String return_code = addOrderResultMap.get("return_code");
            if("SUCCESS".equals(return_code)){
                String result_code = addOrderResultMap.get("result_code");
                if("SUCCESS".equals(result_code)){
                    /*String prepay_id = addOrderResultMap.get("prepay_id");
                    String timeStamp = String.valueOf(System.currentTimeMillis()/1000);
                    String packageStr = "prepay_id="+prepay_id;
                    String signType = "MD5";
                    Map<String, Object> payMap = new HashMap<String, Object>();
                    payMap.put("appId", appId);
                    payMap.put("timeStamp", timeStamp);
                    payMap.put("nonceStr", nonceStr);
                    payMap.put("package", packageStr);
                    payMap.put("signType", signType);
                    String paramsList = WeiXinPay.paramsAdd(payMap);
                    paramsList += "&key="+apikey;
                    log.info("payParamsList="+paramsList);
                    String paySign= MD5.MD5Str(paramsList).toUpperCase();
                    payMap.put("paySign", paySign);
                    JSONObject jsonObject = JSONObject.fromObject(payMap);
                    log.info("jsonObject="+jsonObject.toString());
                    outJson(jsonObject.toString(), response);*/

                    String prepay_id = addOrderResultMap.get("prepay_id");
                    String timeStamp = String.valueOf(System.currentTimeMillis()/1000);
                    Map<String, Object> payMap = new HashMap<String, Object>();
                    payMap.put("appId", appId);
                    payMap.put("timestamp", timeStamp);
                    payMap.put("noncestr", addOrderResultMap.get("nonce_str"));
                    payMap.put("package", "WAP");
                    payMap.put("prepayid", prepay_id);
                    String paramsList = WeiXinPay.paramsAdd(payMap);
                    paramsList += "&key="+apikey;
                    log.info("payParamsList=" + paramsList);
                    String a = paramsList.substring(paramsList.indexOf("appId"), paramsList.indexOf("&key"));
                    String paySign= MD5.MD5Str(a).toUpperCase();
                    System.out.println("paySign="+paySign);
                    String string1 = a + "&sign="+addOrderResultMap.get("sign");
                    System.out.println("string1="+string1);
                    String string2 = URLEncoder.encode(string1);
                    System.out.println("string2="+string2);
                    String resultURL = "weixin://wap/pay?"+string2;
                    System.out.println("resultURL="+resultURL);

                    payMap.put("resultURL",resultURL);

                    //String code_url = addOrderResultMap.get("code_url");
                    //Map<String, Object> payMap = new HashMap<String, Object>();
                    //payMap.put("code_url", code_url);
                    JSONObject jsonObject = JSONObject.fromObject(payMap);
                    log.info("jsonObject="+jsonObject.toString());
                    outJson(jsonObject.toString(), response);
                }else{
                    String err_code = addOrderResultMap.get("err_code");
                    String err_code_des = addOrderResultMap.get("err_code_des");
                    log.info("-----业务结果失败-----err_code="+err_code+",err_code_des="+err_code_des);
                    outJson("{}", response);
                }
            }else{
                String return_msg = addOrderResultMap.get("return_msg");
                log.info("-----通信失败----msg="+return_msg);
                outJson("{}", response);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            outJson("{}", response);
            return;
        }
    }

    /**
     * 微信支付回调方法
     * @param model
     * @param params
     * @param request
     * @param response
     */
    @RequestMapping(value = "weixinPayCallback.do", method = RequestMethod.POST)
    public void weixinPayCallback(final ModelMap model,@RequestParam Map<String, String> params,HttpServletRequest request,HttpServletResponse response){
        log.info("---------微信支付回调方法---------");
        BufferedReader bis = null;
        try {
            bis = new BufferedReader(new java.io.InputStreamReader(request.getInputStream()));
            String line = null;
            String result = "";
            while ((line = bis.readLine()) != null) {
                result += line+"\r\n";
            }
            log.info("result="+result);
            Map<String,String> resultMap = LZJUtil.parseXml(result);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally{
            if(bis!=null){
                try {
                    bis.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 网页授权获取用户信息
     */
    @RequestMapping(value = "auth.do", method = RequestMethod.GET)
    public String auth(final ModelMap model,@RequestParam Map<String, String> params,HttpServletRequest request,HttpServletResponse response){
        log.info("---------进入网页授权获取用户信息方法---------");
        response.setCharacterEncoding("UTF-8");
        String state = params.get("state");
        String code = params.get("code");
        log.info("网页授权中，code={},state={}",code,state);
        try {
            if("testPay".equals(state)){
                log.info("--------testPay---------");
                String url1Str = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+appId+"&secret="+appsecret+"&code="+code+"&grant_type=authorization_code";
                String httpReturnStr1 = LZJUtil.sendGet(url1Str, "UTF-8");
                log.info("-------httpReturnStr1-----"+httpReturnStr1);
                Map<String, Object> map1 = LZJUtil.jsonToMap(httpReturnStr1);
                String codeToAccessToken = (String)map1.get("access_token");
                String codeToOpenid = (String)map1.get("openid");
                log.info("------codeToAccessToken--------"+codeToAccessToken);
                String url2Str = "https://api.weixin.qq.com/sns/userinfo?access_token="+codeToAccessToken+"&openid="+codeToOpenid;
                String httpReturnStr2 = LZJUtil.sendGet(url2Str, "UTF-8");
                log.info("-------httpReturnStr2-----"+httpReturnStr2);
                Map<String, Object> map2 = LZJUtil.jsonToMap(httpReturnStr2);
                String openid = String.valueOf(map2.get("openid"));
                model.put("openid",openid);
                return "website/testPay";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "website/testPay";
    }

    @RequestMapping(value = "getQRCode.do")
    public void  getQRCode(@RequestParam Map<String, String> params,HttpServletResponse response){
        log.info("--------------getQRCode-----------");
        String content = params.get("code_url");
        if(StringUtils.isBlank(content))
            return;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        Map hints = new HashMap();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8"); //设置字符集编码类型
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, 300, 300,hints);
            BufferedImage image = toBufferedImage(bitMatrix);
            //输出二维码图片流
            try {
                ImageIO.write(image, "png", response.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (WriterException e1) {
            e1.printStackTrace();
        }
    }

    private static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return image;
    }

    @RequestMapping(value = "test")
    public String  test(@RequestParam Map<String, String> params){
        log.info("--------------test-----------");
        String redirect_uri = java.net.URLEncoder.encode("http://www.weixinqun.name/weixinqun/testAction/auth.do");
        String authStr = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx0e5917abd2d73550&redirect_uri="+redirect_uri+"&response_type=code&scope=snsapi_userinfo&state=testPay#wechat_redirect";
        log.info("authStr="+authStr);
        return "website/testPay";
    }

    @RequestMapping(value="/toUserInfoTest",method = RequestMethod.GET)
    public String toUserInfoTest(final ModelMap model,@RequestParam Map<String, String> params){
        String publisherId = "1";
        log.info("--------toUserInfoTest-----publisherId=" + publisherId);
        try {
            String userNo = "100000001";
            Map<String,Object> whereMap = new HashMap<String, Object>();
            whereMap.put("user_no",userNo);
            Map<String,Object> userMap = apiService.getOneMethod("user", whereMap, "id", "desc", 0);
            if(userMap==null || userMap.isEmpty()){
                model.put("errorMsg","无此用户");
                model.put("errorCode",userNo);
                return "errorPage";
            }else{
                String encryptUserNo = encryptUserNo(userNo);
                userMap.put("encryptUserNo",encryptUserNo);
                model.put("userMap", userMap);
                return "user/userInfo";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "user/userInfo";
    }

    @RequestMapping(value="/toFreeSubJokeTest",method = RequestMethod.GET)
    public void toFreeSubJokeTest(final ModelMap model,@RequestParam Map<String, String> params){
        log.info("--------toFreeSubJokeTest-----");
        try {
            Map<String,Object> whereMap = new HashMap<String, Object>();
            List<Map<String,Object>> userList = apiService.getListMethod("user", whereMap, "id", "desc", 0);
            for(Map<String,Object> userMap:userList){
                new ApiAction(apiService).freeSubJoke("3,8",String.valueOf(userMap.get("user_no")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*@RequestMapping(value="/redisTest",method = RequestMethod.GET)
    public void f(){
        redisDao.execute(new RedisCallback<Void>() {
            public Void doInRedis(ShardedJedis shardedJedis) {
                shardedJedis.set("redisTestKey", "redisTestValue");
                return null;
            }
        });
        String test = redisDao.execute(new RedisCallback<String>() {
            public String doInRedis(ShardedJedis shardedJedis) {
                return shardedJedis.get("redisTestKey");
            }
        });
        System.out.println(test);
    }*/

    public static String countPercent(int num1,int num2){
        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);
        String result = numberFormat.format((float) num1 / (float) num2 * 100);
        System.out.println("num1和num2的百分比为:" + result + "%");
        return result;
    }

    @RequestMapping(value="/redirectTest",method = RequestMethod.GET)
    public void redirectTest(HttpServletResponse response){
        try {
            response.sendRedirect("/api/lotteryPage");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value="/weidianTest")
    public void weidianTest(HttpServletResponse response){
        try {
            Map<String,String> responseMap = new HashMap<>();
            responseMap.put("status", "success");
            outJson(JSONObject.fromObject(responseMap).toString(),response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/*

    @RequestMapping(value="/uploadOSSFile",method = RequestMethod.POST)
    public void uploadOSSFile(HttpServletRequest request,HttpServletResponse response, @RequestParam Map<String, String> params){
        String userNo = params.get("userNo");
        log.info("上传文件到阿里云啊,userNo="+userNo);
        try{
            String path = request.getSession().getServletContext().getRealPath("/")+"uploadOSSTmp";
            log.info("path=" + path);
            File dirPath = new File(path);
            if (!dirPath.exists()) {
                dirPath.mkdirs();
            }
            //创建一个通用的多部分解析器
            CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
            //判断 request 是否有文件上传,即多部分请求
            if(multipartResolver.isMultipart(request)) {
                log.info("-------------有文件上传upLoad---------------");
                //转换成多部分request
                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
                List<MultipartFile> imageFiles = multiRequest.getFiles("imageFile[]");
                for(MultipartFile file:imageFiles){
                    //取得当前上传文件的文件名称
                    String myFileName = file.getOriginalFilename();
                    //如果名称不为“”,说明该文件存在，否则说明该文件不存在
                    if (myFileName.trim() != "") {
                        String imageName = System.currentTimeMillis()+".png";
                        log.info(myFileName+"--->>>---"+imageName);
                        File dirPathFile = new File(path+File.separator+imageName);
                        if (!dirPathFile.exists()) {
                            dirPathFile.createNewFile();
                        }
                        file.transferTo(dirPathFile);
                        //存到阿里云
                        ALiYunOssUtil.saveFile("lzj-fcheck", userNo + "/" + imageName, dirPathFile);
                        Map<String,Object> paramsMap = new HashMap<String,Object>();
                        paramsMap.put("file_name",imageName);
                        paramsMap.put("file_type","ZFB_CODE");
                        paramsMap.put("source",userNo);
                        paramsMap.put("status",0);
                        paramsMap.put("create_time",new Date());
                        apiService.insertMethod("ali_oss_file", paramsMap);
                        dirPathFile.delete();
                    }

                }
            }
            log.info("for over");
            Map<String,Object> resultMap = new HashMap<String, Object>();
            resultMap.put("success", true);
            resultMap.put("msg", "成功");
            JSONObject jsonObject = JSONObject.fromObject(resultMap);
            log.info("---jsonObject---" + jsonObject.toString());
            outJson(jsonObject.toString(), response);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
        }
    }

    @RequestMapping(value="/uploadOSSFile2",method = RequestMethod.POST)
    public void uploadOSSFile2(HttpServletRequest request,HttpServletResponse response, @RequestParam Map<String, String> params){
        log.info("上传文件到阿里云2");
        String userNo = params.get("userNo");
        String uploadFiles = params.get("uploadFiles");
        String uploadFileArr[] = uploadFiles.split(";@;");
        String path = request.getSession().getServletContext().getRealPath("/")+"uploadOSSTmp";
        log.info("path=" + path);
        File dirPath = new File(path);
        if (!dirPath.exists()) {
            dirPath.mkdirs();
        }
        log.info("uploadFileArr.length="+uploadFileArr.length);
        for(int i=0;i<uploadFileArr.length;i++){
            log.info("i="+i);
            FileOutputStream fos = null;
            try {
                String uploadFile = uploadFileArr[i];
                String uploadFileCode = uploadFile.substring(uploadFile.indexOf(",")+1);
                byte[] byteArray = Base64.decode(uploadFileCode);
                // 调整异常数据
                for (byte b : byteArray) {
                    if(b<0){
                        b+=256;
                    }
                }
                String imageName = System.currentTimeMillis()+".png";
                File dirPathFile = new File(path+File.separator+imageName);
                if (!dirPathFile.exists()) {
                    dirPathFile.createNewFile();
                }
                fos = new FileOutputStream(dirPathFile);
                fos.write(byteArray);
                fos.flush();
                //存到阿里云
                ALiYunOssUtil.saveFile("lzj-fcheck", userNo + "/" + imageName, dirPathFile);
                Map<String,Object> paramsMap = new HashMap<String,Object>();
                paramsMap.put("file_name",imageName);
                paramsMap.put("file_type","ZFB_CODE");
                paramsMap.put("source",userNo);
                paramsMap.put("status",0);
                paramsMap.put("create_time",new Date());
                apiService.insertMethod("ali_oss_file", paramsMap);
                dirPathFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
                log.info(e.getMessage());
            }finally {
                if(fos!=null){
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        log.info("for over");
        Map<String,Object> resultMap = new HashMap<String, Object>();
        resultMap.put("success", true);
        resultMap.put("msg", "成功");
        JSONObject jsonObject = JSONObject.fromObject(resultMap);
        log.info("---scanQRCode---" + jsonObject.toString());
        outJson(jsonObject.toString(), response);

    }
*/


    public static void main(String[] args) {
        countPercent(1,10);
        //test1();
        //test2("test");
    }

    public static void test1(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("classpath:/applicationContent.xml");
        RedisClientTemplate redisClientTemplate = (RedisClientTemplate)ac.getBean("redisClientTemplate");
        String result = redisClientTemplate.set("test","lzj");
        System.out.println(result);
    }

    public static void test2(String key){
        ApplicationContext ac = new ClassPathXmlApplicationContext("classpath:/applicationContent.xml");
        RedisClientTemplate redisClientTemplate = (RedisClientTemplate)ac.getBean("redisClientTemplate");
        String value = redisClientTemplate.get(key);
        System.out.println(value);
    }

}
