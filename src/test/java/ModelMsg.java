import com.alibaba.fastjson.JSONObject;
import com.lzj.action.KFNotifyNotice;
import com.lzj.utils.LZJUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/12/13.
 */
public class ModelMsg {
    public static void main(String[] args) {
        sendKFMessage();

    }

    public static void sendModelMsg(){
        OutputStream os=null;
        InputStream is=null;
        try {
            URL url2=new URL("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=i_5oGz9mXbQT5Iap3TBtWuCtYNbzT24bCZBujzQHhGd23J4YdtWdms5dbVThcnTBa24vZMqq-Oqp-ko0PutB4wFb7EvH_-AfhbjIGoOEBfCpmUVjiScIODJXirf4rZAxLOWjAGAUXF");
            HttpURLConnection huc=(HttpURLConnection) url2.openConnection();
            huc.setDoOutput(true);
            huc.setDoInput(true);
            huc.setRequestMethod("POST");
            //String template_id = "JnFiGYhw6xgEa8lA-ViYoCrUb5QSwxpFXIXQIpWI7o0";//正式模板ID
            String first = "开心一刻";
            String keyword1 ="笑话订阅";
            String keyword2 = "keyword2";
            String keyword3 = "1、今天降温，站路边定车后跟师傅电话联系：“您在大厦南门停，看见一个快冻死的傻逼就是我，特好认。”说完哆哆嗦嗦挂了电话。过了一会儿，师傅又打过来了：“您好，这路边好几个傻逼，哪个是你？”" +
                    "2、闺蜜因为喜欢网购和老公吵架，跑我这来了，刚来到就气愤的说:“他就是跪着求我，我也不会原谅他的。”一小时后，闺蜜说:“他如果来接我，我就勉为其难跟他走。”两小时后她说:“想想是我太任性了，他如果打电话让我回去今天这事就算了。”三小时后，闺蜜说:“我想想阳台上的衣服还没收，我先回去收衣服了哈。。。。”" +
                    "3、傍晚，室友和她男票出去散步，结果突然开始下雨，于是她男票把外套脱下来，遮在两个人头上挡雨。啊，听起来还挺美好的。但是直男永远不会辜负大家的期望，室友男票：“诶，你看我们像不像舞龙？”" +
                    "4、有一天，一个哥们到我们这来，看见大门上贴着“小心玻璃”几个字，扭头就走了，他指着大门说，“你们这不欢迎我啊~”" +
                    "5、妈妈长得特别漂亮，总嫌我不好看。有一天又说我，我终于忍不住反驳：“谁让你不整明白再生出来的!”她说：“告诉你多少次了，你是我捡的!”我说：“那你不挑个好的捡?”“好的谁扔啊!”行!妈，你赢了!";

            /*String keyword3 = "1、今天降温，站路边定车后跟师傅电话联系：“您在大厦南门停，看见一个快冻死的傻逼就是我，特好认。”说完哆哆嗦嗦挂了电话。过了一会儿，师傅又打过来了：“您好，这路边好几个傻逼，哪个是你？”" +
                    "2、闺蜜因为喜欢网购和老公吵架，跑我这来了，刚来到就气愤的说:“他就是跪着求我，我也不会原谅他的。”一小时后，闺蜜说:“他如果来接我，我就勉为其难跟他走。”两小时后她说:“想想是我太任性了，他如果打电话让我回去今天这事就算了。”三小时后，闺蜜说:“我想想阳台上的衣服还没收，我先回去收衣服了哈。。。。”";
            */
            String remark = "更多乐趣,点击详情查看>>";
            String descUrl = "http://www.baidu.com";
            String toUserOpenId = "ojrBPxGMZOnnQzZM8-8ER2-gPi88";
            String templateId = "JnFiGYhw6xgEa8lA-ViYoCrUb5QSwxpFXIXQIpWI7o0";
            String msgString="{ \"touser\":\""+toUserOpenId+"\", \"template_id\":\""+templateId+"\",\"url\":\""+descUrl+"\",  \"data\":{ \"first\": { \"value\":\""+first+"\", \"color\":\"#173177\" }, \"keyword1\":{ \"value\":\""+keyword1+"\", \"color\":\"#173177\" }, \"keyword2\":{ \"value\":\""+keyword2+"\", \"color\":\"#173177\" }, \"keyword3\":{ \"value\":\""+keyword3+"\", \"color\":\"#173177\" }, \"remark\":{ \"value\":\""+remark+"\", \"color\":\"#173177\" } } }";
            os=huc.getOutputStream();
            os.write(msgString.getBytes("UTF-8"));
            os.flush();
            is=huc.getInputStream();
            int len=0;
            byte[] buff=new byte[1024];
            String sendModelResult = "";
            while((len=is.read(buff))>0){
                sendModelResult = new String(buff,0,len);
                System.out.println("sendWXModelMsg中的--->"+sendModelResult);
            }
            System.out.println("--------sendModelResult-------" + sendModelResult);
            Map<String,Object> sendModelResultMap = LZJUtil.jsonToMap(sendModelResult);
            int errcode = (Integer)sendModelResultMap.get("errcode");
            String errmsg = String.valueOf(sendModelResultMap.get("errmsg"));
            String msgid = "";
            if(errcode==0 && "ok".equals(errmsg)){
                System.out.println("--------------发送模板消息成功---------------");
                msgid = String.valueOf(sendModelResultMap.get("msgid"));
            }else{
                System.out.println("--------------发送模板消息失败---------------");
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

    public static void sendKFMessage(){
        String access_token = "VgKTEaNQMHKGJUKQsNnUeiI6ZA6rdrmdErVJJUE5AauAYC_ZMAeenJU4J8LBqv-9N_ZOEC4lX-8mmA9CoXasiknks4M9YRmOywJtcqicMxk_yNnPbHZ1L1aEDls2GYe9VEUfAFABKA";
        Map<String,Object> kfParamsMap = new HashMap<String, Object>();
        kfParamsMap.put("accessToken",access_token);
        kfParamsMap.put("toUserOpenId","ojrBPxGMZOnnQzZM8-8ER2-gPi88");
        String a = "笑话订阅推送\n\uE032郑重提醒：由于微信限制，请至少保持两天签到一次，不然您会错过小微的推送哦\uE032\n1、小学生优秀作文选：《当领导的好处》<br />\n" +
                "        爸爸当上领导后，从来不敢多说话，生怕给别人带来压力。上个月，下属小李到我家来玩，爸爸随口说了句：“家里没空调，比较热，见笑了。” <br />\n" +
                "　　第二天，小李带师傅给我家安装了5匹的大空调！怎么也不肯收钱。<br />\n" +
                "　　上个星期，爸爸看到老张在办公室吃午饭，其中有煎的鱼，随口说了句：“哎呦，真香！”！<br />\n" +
                "        当天晚上。老张给我家送了一箱鲜鱼来。<br />\n" +
                "<br />\n" +
                "　　有天晚上在路上，碰到单位的小王和他老婆逛街，爸爸随口称赞道：“哎呦，小王，你媳妇真漂亮！”<br />\n" +
                "　　第二天晚上，妈妈不在家，突然听到有人敲门，打开门一看，小王媳妇站在门口，见到爸爸就满脸的微笑说：“领导，我们家小王说嫂子不在家，让我来陪陪你！”<br />\n" +
                "<br />\n" +
                "       当领导真好，怪不得这么多人喜欢当领导！\n" +
                "2、关于抢红包诗一首：锄禾日当午，不如抢钱苦。抢完了上午，还要抢下午。问你抢多少，总共两块五！一查流量费， 超过二百五！\n" +
                "3、练完铁头功的大师兄刚一下山就被装有磁铁的大吊车给吸走了\n" +
                "4、明明上小学二年级，他上学经常迟到。<br />\n" +
                "　　一天，老师问他迟到的原因，他呆了很长时间都没有回答。<br />\n" +
                "　　当老师再问时，他竟然“哇”的一声哭了起来！<br />\n" +
                "　　老师：“你哭什么呀？”<br />\n" +
                "　　明明：“我昨天准备好的很多‘迟到理由’，不知怎么，今天连一个都想不起来了！”\n" +
                "5、一天，老师对同学们说：“如果你是皇帝，我是丞相，你会对我说些什么？”<br />\n" +
                "　　就在这时，小明站起来怒吼道：“拖出去砍了！”\n" +
                "\n" +
                "<a href='http://www.baidu.com'>更多乐趣,点我查看>></a>" ;
        String contentStr = a.replace("<br />","");
        contentStr = contentStr.replace("\r","");
        System.out.println("contentStr==============="+contentStr);
        kfParamsMap.put("content",contentStr);
        Thread thread = new Thread(new KFNotifyNotice("kefuMessage_text",kfParamsMap));
        thread.start();
    }
}
