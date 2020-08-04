package com.author.commons.utils.caches;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.author.commons.utils.Constants.redis;
import com.author.commons.utils.enums.Rdb;

import cn.hutool.core.util.NumberUtil;

@Service
public class RedisImpl {
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	protected boolean keyExists(String k) {
		return redisTemplate.hasKey(k);
	}

	public Object redisResults(String k, int c, long s, long e) {
		if (keyExists(k)) {
			switch (c) {
			case redis.redis1str:
				return redisTemplate.opsForValue().get(k);
			case redis.redis2set:
				return redisTemplate.opsForSet().members(k);
			case redis.redis3hash:
				return redisTemplate.opsForHash().entries(k);
			case redis.redis4list:
				return redisTemplate.opsForList().range(k, s, e);
			case redis.redis5zset:
				return redisTemplate.opsForZSet().range(k, s, e);
			}
		}
		return null;
	}

	public Set<?> keys(String k) {
		return redisTemplate.keys(k);
	}

	public boolean redisHandle(String k, String v, int c, long t) {
		switch (c) {
		case redis.redis1str:
			redisTemplate.opsForValue().set(k, v);
			break;
		case redis.redis2set:
			redisTemplate.opsForSet().add(k, v);
			break;
		}
		if (t > 0) {
			redisTemplate.expire(k, t, TimeUnit.SECONDS);
		}
		return keyExists(k);
	}

	public boolean redisHandle(String k, String v, int c) {
		switch (c) {
		case redis.redis1str:
			if (keyExists(k)) {
				redisTemplate.delete(k);
			}
			break;
		case redis.redis2set:
			redisTemplate.opsForSet().remove(k, v);
			break;
		}
		return keyExists(k);
	}

	public boolean convertDB(int db) {
		if (db <= 0) {
			db = Rdb.db2.c();
		}
		LettuceConnectionFactory factory = (LettuceConnectionFactory) redisTemplate.getConnectionFactory();
		if (null == factory) {
			return false;
		}
		if (NumberUtil.compare(db, factory.getDatabase()) == 0) {
			return false;
		}
		factory.setDatabase(db);
		redisTemplate.setConnectionFactory(factory);
		factory.resetConnection();
		factory.afterPropertiesSet();
		return true;
	}
}
