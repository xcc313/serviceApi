package com.lzj.redis;

import org.springframework.stereotype.Repository;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import javax.annotation.Resource;


@Repository
public class RedisDao {
/*
	@Resource
	private ShardedJedisPool shardedJedisPool;

	private ShardedJedis getConnection() {
		return shardedJedisPool.getResource();
	}

	private void releaseConnection(ShardedJedis shareJedis) {
		shardedJedisPool.returnResource(shareJedis);
	}

	public <T> T execute(RedisCallback<T> redisCallback) {
		ShardedJedis shardedJedis = null;
		try {
			shardedJedis=getConnection();
			return redisCallback.doInRedis(shardedJedis);
		} finally {
			if(shardedJedis!=null){
				releaseConnection(shardedJedis);
			}
		}
	}*/
}
