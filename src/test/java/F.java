import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Administrator on 2015/10/14.
 */
public class F {
    public static String getUserNo(){
        String nowTimeStr = String.valueOf(System.currentTimeMillis());
        String userNo = "80"+nowTimeStr.substring(nowTimeStr.length()-4)+String.valueOf(Math.round(Math.random()*900+100));
        return userNo;
    }
    public static void stringTest(){
        String a = "笑话订阅推送\n" +
                "郑重提醒：请至少保持两天签到一次，不然您会错过小微的推送哦\n" +
                "1、一漂亮女同事，她老公给她送午饭，没说话放下就走了。新来的男同事问：刚才那是谁啊？她回答：送外卖的。新来又问：怎么沒给钱？她说：不用给，晚上陪他睡一觉就好了。男同事沉默了，第\n" +
                "二天，给她带了四菜一汤的午饭，整个办公室轰然大笑……\n" +
                "2、下午肚子饿，看见同事桌上有瓶酸奶，想都没想就喝了，一会同事来了大叫到：“我的洗面奶怎么不见了！108块啊！”哥没说话，只是默默的走向厕所，一顿抠嗓子，老难受了，把东西拼命的吐，直\n" +
                "到吐出酸水，好不容易吐的差不多了，眼泪叭嚓滴回到座位上时，同事抱着一个瓶子 说：“吓死我了，洗面奶滚到桌子下了，我的酸奶怎么又不见了呢。”哥心里直骂：你奶奶个熊滴，喝你点酸奶，把\n" +
                "人往死里整。\n" +
                "3、今天在办公室闲的没事，在玩一块磁铁，被领导看到了。领导伸手就来拿，结果“嗖”的一下，磁铁吸在了领导的金戒指上面…\n" +
                "4、今天接了个电话，问我买不买房子，不买还会涨。我说已经有几套了，手头实在没钱，对方沉默了几秒钟又说，那你房子卖不卖？现在房价那么高，再不卖就卖不到这价了。我感觉不说实话不行了\n" +
                "，就对他说，其实我穷，既没房也买不起房。他沉默了几秒又说，明天有个楼开盘，晚上带上椅子来排队，给你200元一天，干不？我被他深深地感动了，..TMD这才是真正的销售！不管客户啥情况，总\n" +
                "有一款适合你！不抛弃，不放弃！\n" +
                "5、睡觉前，我对老婆说：“你看现在的萌妹子说话就是好听，后面都带个重叠的字，比如吃饭饭，睡觉觉。听着多舒服啊！”<br />\n" +
                "　　老婆不屑地白了我一眼，说：“就这些我也会啊。”<br />\n" +
                "　　我怀疑地看着老婆，说：“你也会？说来听听？”<br />\n" +
                "　　老婆咬牙切齿地说：“别叨叨！”\n" +
                "\n" +
                "<a href=''>更多乐趣,点我查看>></a>";
        System.out.println(a.length());
    }
    public static void main(String[] args) {
        //System.out.println(getUserNo());
        stringTest();
    }
}
