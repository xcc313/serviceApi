package com.lzj.action;

import com.alibaba.druid.util.StringUtils;
import com.lzj.utils.DESPlus;
import com.lzj.utils.HttpUtils;
import com.lzj.utils.LZJUtil;
import com.lzj.utils.MD5;
import com.lzj.utils.encryptor.Base64Utils;
import com.lzj.utils.encryptor.RSAUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Administrator on 2016/11/17.
 */
public class SearchNewApiUtil {
    private static final String DESPLUSKEY = "LZJAIHUIHUI";

    /**
     * 单期开奖查询
     * @param lotteryCode
     * @param lotteryExpect
     * @return
     * @throws Exception
     * @author lzj
     */
    public static String getNewLotteryResult(String lotteryCode,String lotteryExpect) throws Exception {
        String host = "http://ali-lottery.showapi.com";
        String path = "/one";
        String method = "GET";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE 5ef4a89682574ccabe8d5850d25b6305");
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("code", lotteryCode);
        if(!StringUtils.isEmpty(lotteryExpect)){
            querys.put("expect", lotteryExpect);
        }
        HttpResponse httpResponse = HttpUtils.doGet(host, path, method, headers, querys);
        //获取response的body
        return EntityUtils.toString(httpResponse.getEntity());
    }

    /**
     * 银行卡元素实名认证
     * @param bankcard 银行卡号
     * @param idcard 身份证号
     * @param realname 姓名
     * @param mobileNo 手机号
     * @return
     * @throws Exception
     * @author lzj
     */
    public static String getNewVerifiedResult(String bankcard,String idcard,String realname,String mobileNo) throws Exception {
        String host = "http://aliyun-yhk.id98.cn";
        String path = "/bankcard";
        String method = "GET";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE 5ef4a89682574ccabe8d5850d25b6305");
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("bankcardno", bankcard);
        querys.put("idcardno", idcard);
        querys.put("name", realname);
        querys.put("tel", mobileNo);
        HttpResponse httpResponse = HttpUtils.doGet(host, path, method, headers, querys);
        //获取response的body
        return EntityUtils.toString(httpResponse.getEntity());
    }

    /**
     * 移付宝通道-银行卡元素实名认证
     * @param bankcard 银行卡号
     * @param idcard 身份证号
     * @param realname 姓名
     * @param mobileNo 手机号
     * @return
     * @throws Exception
     * @author lzj
     */
    public static String getNewVerifiedResultByYFB(String bankcard,String idcard,String realname,String mobileNo) throws Exception {
        String merId = "743983212681958";
        String merName = "移付宝测试商户";
        String orderNumber = createOrderNo();
        String orderTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String url = "http://msg.yfbpay.cn/msgplatform/rz/smrz";
        String secretKey = "ciVUz2k96M8z11g5Jp9J06z2YYr077bD";
        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDX/JCJpwFzUPDsxZEwZ89wxQXk2RzEN7zk9SH4pyl3KsCVS+zjmdMj6QOMRvqdTzgWzmt0Fy8XriUbPlzHAzVSrtcfjr7Ok1Zba/O41moUbcUUzHzJnPG0vEBOXyssBC70bTjNO0m47wmas5fk7GiD0zvxLnjXwDlTW3frpj3JxwIDAQAB";
        String searchUrl1 = "cardNum=" + bankcard + "&idCard=" + idcard + "&userName=" + realname + "&phoneNum=" + mobileNo;
        System.out.println("searchUrl1:"+searchUrl1);
        String paydata = Base64Utils.encode(RSAUtils.encryptByPublicKey(searchUrl1.getBytes(), publicKey));
        String searchUrl2 = "merId=" + merId + "&merName=" + merName + "&orderNumber=" + orderNumber + "&orderTime=" + orderTime + "&paydata=" + paydata;
        String md5SecretKey = MD5.MD5Str(secretKey).toLowerCase();
        String md5Signature = searchUrl2 + "&" + md5SecretKey;
        String signature = MD5.MD5Str(md5Signature).toLowerCase();
        Map<String, Object> query = new HashMap<String, Object>();
        query.put("merId", merId);
        query.put("merName", merName);
        query.put("orderNumber", orderNumber);
        query.put("orderTime", orderTime);
        query.put("paydata", paydata);
        query.put("signature", signature);
        System.out.println("query:"+query);
        String returnStr = LZJUtil.sendPost(url, query, "UTF-8");
        //{"errCode":"0001","errMsg":"实名认证失败，请换资料重试"}
        //{"errCode":"0000","errMsg":"验证成功"}
        return returnStr;
    }
    private static String createOrderNo(){
        StringBuffer sb = new StringBuffer();
        Random r = new Random();
        for (int i = 0; i < 5; i++) {
            sb.append(r.nextInt(8999) + 1000);
        }
        String orderNo = System.currentTimeMillis() + sb.toString();
        return orderNo.substring(15, orderNo.length());
    }

    /**
     * 失信被执行人查询
     * @param dtype 返回数据格式：json或xml，默认json
     * @param isExactlySame 名称是否需要与关键字一样, True或者False(Default value is False)
     * @param pageIndex 页码，默认第1页
     * @param pageSize 每页条数，默认10条，最大不超过50条
     * @param province 省份代码，默认为空
     * @param searchKey 查询关键字
     * @return
     * @throws Exception
     * @author lzj
     */
    public static String getNewDishonestyResult(String dtype,Boolean isExactlySame,String pageIndex,String pageSize,String province,String searchKey) throws Exception {
        String host = "http://ali.court.yjapi.com";
        String path = "/Court/SearchCourt";
        String method = "GET";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE 5ef4a89682574ccabe8d5850d25b6305");
        Map<String, String> querys = new HashMap<String, String>();
        if(!StringUtils.isEmpty(dtype)){
            querys.put("dtype", dtype);
        }
        if(isExactlySame!=null){
            querys.put("isExactlySame", ""+isExactlySame);
        }
        if(!StringUtils.isEmpty(pageIndex)){
            querys.put("pageIndex", pageIndex);
        }
        if(!StringUtils.isEmpty(pageSize)){
            querys.put("pageSize", pageSize);
        }
        if(!StringUtils.isEmpty(province)){
            querys.put("province", province);
        }
        querys.put("searchKey", searchKey);
        HttpResponse httpResponse = HttpUtils.doGet(host, path, method, headers, querys);
        //获取response的body
        return EntityUtils.toString(httpResponse.getEntity());
    }

    /**
     * 邮编查询
     * @param areaid 区域ID（区域查询接口中获取）
     * @param address 地址关键字
     * @return
     * @throws Exception
     */
    public static String getNewZipcodeResult(String areaid,String address) throws Exception {
        String host = "http://jisuybcx.market.alicloudapi.com";
        String path = "/zipcode/addr2code";
        String method = "GET";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE 5ef4a89682574ccabe8d5850d25b6305");
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("address", address);
        querys.put("areaid", areaid);
        HttpResponse httpResponse = HttpUtils.doGet(host, path, method, headers, querys);
        //获取response的body
        return EntityUtils.toString(httpResponse.getEntity());
    }

    /**
     * 笑话查询
     * @param jokeType 笑话类型  text文字  pic图片  all所有
     * @param pagenum 页码(每天大概更新十页左右)
     * @param pagesize  每页条数 最大20
     * @param sort  排序 addtime按时间倒叙 rand随机获取 sort=rand时，pagenum无效
     * @return
     * @throws Exception
     */
    public static String getNewJokeResult(String jokeType,String pagenum,String pagesize,String sort) throws Exception {
        String host = "http://jisuxhdq.market.alicloudapi.com";
        String path = "/xiaohua/"+jokeType;
        String method = "GET";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE 5ef4a89682574ccabe8d5850d25b6305");
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("pagenum", pagenum);
        querys.put("pagesize", pagesize);
        querys.put("sort", sort);
        HttpResponse httpResponse = HttpUtils.doGet(host, path, method, headers, querys);
        //获取response的body
        return EntityUtils.toString(httpResponse.getEntity());
    }



    public static void main(String[] args) {
        try{
            System.out.println(getNewVerifiedResultByYFB("6217995650008296546","431121198809096938","漆玉亮","13357287006"));
        }catch (Exception e){e.printStackTrace();}
    }
}
