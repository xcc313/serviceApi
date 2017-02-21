package com.lzj.fc_pay.wofu;

import com.lzj.utils.DESPlus;
import com.lzj.utils.LZJUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by lzj on 2017/2/14.
 */
public class WoFuAction {
    private static final Logger log = LoggerFactory.getLogger(WoFuAction.class);

    /**
     * 二维码支付下单
     * @param payType wxNative微信下单 alipay支付宝下单
     * @param orderNo     20位以内订单编号
     * @param body        商品名称，在支付的时候展示给付款人
     * @param transAmount 交易金额(元)
     * @param callbackUrl 回调地址，交易成功回调此地址
     * @return
     */
    public Map<String, Object> createOrder(String payType,String orderNo, String body, String transAmount, String callbackUrl) throws Exception{
        log.info("------------二维码支付下单--------------payType="+payType);
        String jsonStr = "{\"bizName\":\""+payType+"\",\"data\":{\"orderNo\":\""+orderNo+"\",\"body\":\""+body+"\",\"transAmount\":\""+transAmount+"\",\"callbackUrl\":\""+callbackUrl+"\"}}";
        log.info("------------二维码支付下单jsonStr--------------"+jsonStr);
        DESPlus des = new DESPlus(WoFuConfig.SECRET_KEY);
        String json = des.encrypt(jsonStr);
        String url = WoFuConfig.TRANS_URL+"?appKey="+WoFuConfig.APP_KEY+"&data="+json;
        String req = LZJUtil.sendGet(url,"UTF-8");
        log.info("req=" + req);
        String responseStr = des.decrypt(req);
        log.info("responseStr=" + responseStr);
        return LZJUtil.jsonToMap(responseStr);
    }

    /**
     *  单笔代付下单（目前仅支持对私代付）
     * @param orderNo 必填 20位以内订单编号
     * @param accountName 必填 收款人户名
     * @param accountNo 必填 账号
     * @param remark 选填 备注信息，将要显示银行记录中
     * @param transAmount 必填 代付金额
     * @param bankFullName 选填 银行开户行*对公代付填写
     * @param callbackUrl 选填 回调地址，中间状态的交易将异步通知结果
     * @param accountType 选填 private对私 public对公 不填默认对私
     * @param cnaps 选填 联行号*对公代付填写
     * @return
     */
    public Map<String, Object> purseCashCreateOrder(String orderNo, String accountName, String accountNo, String remark, String transAmount, String bankFullName, String callbackUrl, String accountType, String cnaps) {
        log.info("------------代付下单--------------");
        return null;
    }

    public static void main(String[] args) {
        try {
            Map<String,Object> resultMap = new WoFuAction().createOrder("alipay","100004","路遥里科技收款","0.02","");
            System.out.println(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
