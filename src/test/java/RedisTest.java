import com.lzj.redis.RedisCallback;
import com.lzj.redis.RedisClientTemplate;
import com.lzj.redis.RedisDao;
import com.lzj.redis.RedisDataSource;
import javafx.application.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import redis.clients.jedis.ShardedJedis;

/**
 * Created by Administrator on 2016/11/12.
 */
public class RedisTest {

    public static void test1(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        RedisClientTemplate redisClientTemplate = (RedisClientTemplate)ac.getBean("redisClientTemplate");
        String result = redisClientTemplate.set("test","lzj");
        System.out.println("result="+result);
    }

    public static void test2(String key){
        ApplicationContext ac = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        RedisClientTemplate redisClientTemplate = (RedisClientTemplate)ac.getBean("redisClientTemplate");
        String value = redisClientTemplate.get(key);
        System.out.println("value="+value);
    }

    public void test3(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        RedisClientTemplate redisClientTemplate = (RedisClientTemplate)ac.getBean("redisClientTemplate");
        ShardedJedis shardedJedis = redisClientTemplate.getShardedJedis();
        Long resultLong = shardedJedis.append("test","append4");
        System.out.println("resultLong="+resultLong);
    }

    public static void main(String[] args) {
        RedisTest redisTest = new RedisTest();
        //test1();
        //test2("test");
        redisTest.test3();
        try {
            Thread.sleep(10*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        test2("test");
        /*ApplicationContext ac =  new ClassPathXmlApplicationContext("classpath:/applicationContext.xml");
        MemberDao userDAO = (MemberDao)ac.getBean("userDAO");
        Member user1 = new Member();
        user1.setId("1");
        user1.setNickname("obama");
        userDAO.add(user1);
        Member user2 = MemberDao.get("1");
        System.out.println(user2.getNickname());*/

    }
/*
    public static void f(){
        new RedisDao().execute(new RedisCallback<Void>() {
            public Void doInRedis(ShardedJedis shardedJedis) {
                shardedJedis.set("redisTestKey","redisTestValue");
                return null;
            }
        });
        String test = new RedisDao().execute(new RedisCallback<String>() {
            public String doInRedis(ShardedJedis shardedJedis) {
                return shardedJedis.get("redisTestKey");
            }
        });
        System.out.println(test);
    }*/
}
