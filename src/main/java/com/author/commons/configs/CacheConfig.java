package com.author.commons.configs;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@SuppressWarnings("all")
public class CacheConfig extends CachingConfigurerSupport {
	@Bean(name = "redisTemplete")
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
		RedisTemplate<String, Object> template = new RedisTemplate();
		template.setConnectionFactory(factory);
		return template;
	}
}
