import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2016/12/2.
 */
public class CreateMeau {
    public static void main(String[] args) {
        System.out.println("toCreateMenu()方法");
        OutputStream os=null;
        InputStream is=null;
        try {
            String access_token = "9pMDMUKsHNQY_qjk7P0T7Ls-vWZjouQVbe1BndkIdtkAnaNyS4D3XUYLGx0jOenA-2_kAo3lEn_OCweNFXLZbsskpBRrH5wGk_HcCWqG7ZCP4IcJkMIS7Mm416ZoXbgpFOWhACALUC";
            String appId = "wx4ee57072d531b1a9";
            //String weixinUrl = "http://www.fcheck.cn";
            String weixinUrl = "http://www.qrcodevip.com";

            URL url2=new URL("https://api.weixin.qq.com/cgi-bin/menu/create?access_token="+access_token);
            HttpURLConnection huc=(HttpURLConnection) url2.openConnection();
            huc.setDoOutput(true);
            huc.setDoInput(true);
            huc.setRequestMethod("POST");

            //微信回调地址
            String redirect_uri = java.net.URLEncoder.encode(weixinUrl+"/wx/auth.do");
            String jokeUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+appId+"&redirect_uri="+redirect_uri+"&response_type=code&scope=snsapi_base&state=joke&connect_redirect=1#wechat_redirect";
            String lotteryUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+appId+"&redirect_uri="+redirect_uri+"&response_type=code&scope=snsapi_base&state=lottery&connect_redirect=1#wechat_redirect";
            String zipcodeUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+appId+"&redirect_uri="+redirect_uri+"&response_type=code&scope=snsapi_base&state=zipcode&connect_redirect=1#wechat_redirect";
            String verifiedUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+appId+"&redirect_uri="+redirect_uri+"&response_type=code&scope=snsapi_base&state=verified&connect_redirect=1#wechat_redirect";
            String dishonestyUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+appId+"&redirect_uri="+redirect_uri+"&response_type=code&scope=snsapi_base&state=dishonesty&connect_redirect=1#wechat_redirect";
            String userInfoUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+appId+"&redirect_uri="+redirect_uri+"&response_type=code&scope=snsapi_base&state=userInfo&connect_redirect=1#wechat_redirect";
            String weiCoinRechargeUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+appId+"&redirect_uri="+redirect_uri+"&response_type=code&scope=snsapi_base&state=weiCoinRecharge&connect_redirect=1#wechat_redirect";
			String menuString="{ \"button\":[ { \"name\":\"生活\", \"sub_button\":[{ \"type\":\"view\", \"name\":\"笑话订阅\", \"url\":\""+jokeUrl+"\" },"
                    + "{ \"type\":\"view\", \"name\":\"邮编查询\", \"url\":\""+zipcodeUrl+"\" }, "
					+ "{ \"type\":\"view\", \"name\":\"开彩查询\", \"url\":\""+lotteryUrl+"\" }]},"
					+ "{ \"name\":\"金融\", \"sub_button\":[ { \"type\":\"view\", \"name\":\"失信查询\", \"url\":\""+dishonestyUrl+"\" }, "
					+ "{ \"type\":\"view\", \"name\":\"银行卡认证\", \"url\":\""+verifiedUrl+"\" }]},"
					+ "{ \"name\":\"我的\", \"sub_button\":[ { \"type\":\"click\", \"name\":\"签到\", \"key\":\"signIn\" },"
                    + "{ \"type\":\"view\", \"name\":\"微币充值\", \"url\":\""+weiCoinRechargeUrl+"\" },"
                    + "{ \"type\":\"view\", \"name\":\"个人中心\", \"url\":\""+userInfoUrl+"\" }]}"
					+ "]}";
            /*String menuString="{ \"button\":[ { \"name\":\"二维码优汇\",  \"type\":\"view\", \"url\":\""+qrCodeUrl+"\" }, "
                    + "{ \"name\":\"我的控制台\",  \"type\":\"view\", \"url\":\""+userInfo+"\" }";
            menuString += "]}";*/

            System.out.println("menuString-->"+menuString);
            os=huc.getOutputStream();
            os.write(menuString.getBytes("UTF-8"));
            os.flush();
            is=huc.getInputStream();
            int len=0;
            byte[] buff=new byte[1024];
            while((len=is.read(buff))>0){
                System.out.println("toCreateMenu中的--->" + new String(buff, 0, len));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            try {
                if(is!=null){
                    is.close();
                }
                if(os!=null){
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
