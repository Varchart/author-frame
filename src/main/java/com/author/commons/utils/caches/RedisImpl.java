package com.author.commons.utils.caches;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.author.commons.utils.Constants;

@Service
public class RedisImpl {
	@Autowired
	private RedisTemplate<String, String> redisTemplete;

	protected boolean keyExists(String k) {
		return redisTemplete.hasKey(k);
	}

	public Object redisResults(String k, int c, long s, long e) {
		if (keyExists(k)) {
			switch (c) {
			case Constants.redis.redis1str:
				return redisTemplete.opsForValue().get(k);
			case Constants.redis.redis2set:
				return redisTemplete.opsForSet().members(k);
			case Constants.redis.redis3hash:
				return redisTemplete.opsForHash().entries(k);
			case Constants.redis.redis4list:
				return redisTemplete.opsForList().range(k, s, e);
			case Constants.redis.redis5zset:
				return redisTemplete.opsForZSet().range(k, s, e);
			}
		}
		return null;
	}

	public Set<?> keys(String k) {
		return redisTemplete.keys(k);
	}

	public boolean redisHandle(String k, String v, int c, long t) {
		switch (c) {
		case Constants.redis.redis1str:
			redisTemplete.opsForValue().set(k, v);
			break;
		case Constants.redis.redis2set:
			redisTemplete.opsForSet().add(k, v);
			break;
		}
		if (t > 0) {
			redisTemplete.expire(k, t, TimeUnit.SECONDS);
		}
		return keyExists(k);
	}
}
