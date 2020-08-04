package com.author.commons.utils.caches;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.author.commons.utils.Constants.redis;

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
			case redis.redis1str:
				return redisTemplete.opsForValue().get(k);
			case redis.redis2set:
				return redisTemplete.opsForSet().members(k);
			case redis.redis3hash:
				return redisTemplete.opsForHash().entries(k);
			case redis.redis4list:
				return redisTemplete.opsForList().range(k, s, e);
			case redis.redis5zset:
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
		case redis.redis1str:
			redisTemplete.opsForValue().set(k, v);
			break;
		case redis.redis2set:
			redisTemplete.opsForSet().add(k, v);
			break;
		}
		if (t > 0) {
			redisTemplete.expire(k, t, TimeUnit.SECONDS);
		}
		return keyExists(k);
	}

	public boolean redisHandle(String k, String v, int c) {
		switch (c) {
		case redis.redis1str:
			if (keyExists(k)) {
				redisTemplete.delete(k);
			}
			break;
		case redis.redis2set:
			redisTemplete.opsForSet().remove(k, v);
			break;
		}
		return keyExists(k);
	}
}
