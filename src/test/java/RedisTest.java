import com.lzj.redis.RedisCallback;
import com.lzj.redis.RedisDao;
import redis.clients.jedis.ShardedJedis;

/**
 * Created by Administrator on 2016/11/12.
 */
public class RedisTest {

    public static void main(String[] args) {
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
