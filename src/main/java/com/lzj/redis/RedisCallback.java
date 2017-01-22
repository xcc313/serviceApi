package com.lzj.redis;

import redis.clients.jedis.ShardedJedis;

/**
 * 
 * @author wg
 * @date   2015年1月9日
 * @param <T>
 */
public interface RedisCallback<T> {
	public T doInRedis(ShardedJedis shardedJedis);
}
