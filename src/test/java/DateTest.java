import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2016/5/30.
 */
public class DateTest {

    public static int compDate(Date fDate, Date oDate) {
        Calendar aCalendar = Calendar.getInstance();
        aCalendar.setTime(fDate);
        int day1 = aCalendar.get(Calendar.DAY_OF_YEAR);
        aCalendar.setTime(oDate);
        int day2 = aCalendar.get(Calendar.DAY_OF_YEAR);
        System.out.println("day1="+day1+",day2="+day2);
        return day2 - day1;
    }


    public static void main(String[] args) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String a = "2016-12-20 16:20:22";
        String b = "2016-12-27 16:20:22";
        System.out.println(""+compDate(sdf.parse(a),sdf.parse(b)));
        /*System.out.println("123213");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String a = "2016-05-31 15:00:00";
        try{
            Date top_arrive_time = sdf.parse(a);
            Calendar robArriveTime = Calendar.getInstance();
            robArriveTime.setTime(top_arrive_time);
            robArriveTime.add(Calendar.HOUR_OF_DAY, -5);
            Calendar nowTime = Calendar.getInstance();
            if(robArriveTime.before(nowTime)){
                System.out.println("----------当前时间在本轮抢位时间之后，本轮抢置顶位已结束---------");

            }
        }catch(Exception e){
            e.printStackTrace();
        }*/

        Calendar nowCalender = Calendar.getInstance();
        int nowHour = nowCalender.get(Calendar.HOUR_OF_DAY);

        /*SimpleDateFormat sdf   =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String a = "2016-06-03 23:00:00";
        Date aDate = sdf.parse(a);
        Calendar cal1   =   Calendar.getInstance();
        cal1.setTime(aDate);
        cal1.add(Calendar.HOUR,2);
        String arriveTime = sdf.format(cal1.getTime());
        System.out.println(arriveTime);*/
    }



}
