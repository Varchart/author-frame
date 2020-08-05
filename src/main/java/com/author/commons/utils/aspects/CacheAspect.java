package com.author.commons.utils.aspects;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.author.commons.utils.aspects.annotations.CacheConvert;
import com.author.commons.utils.caches.RedisImpl;
import com.author.commons.utils.enums.Rdb;

import cn.hutool.core.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class CacheAspect {
	@Autowired
	private RedisImpl cacheService;

	@Pointcut("@annotation(com.author.commons.utils.aspects.annotations.CacheConvert)")
	public void cc() {
	}

	@Before("cc()")
	public void reseive(JoinPoint point) {
		try {
			Rdb dt = read(point, 1);
			cacheService.convertDB(dt.c());
		} catch (Throwable ex) {
			log.warn("Aspect cache convert reseive exception. {}", ex.getMessage());
		}
	}

	@After("cc()")
	public void rollBack(JoinPoint point) {
		try {
			Rdb dt = read(point, 0);
			cacheService.convertDB(dt.c());
		} catch (Throwable ex) {
			log.warn("Aspect cache convert rollback exception. {}", ex.getMessage());
		}
	}

	protected synchronized Rdb read(JoinPoint point, int c) throws Throwable {
		Rdb dt = null;
		if (null != point) {
			CacheConvert annotationOfClass = point.getTarget().getClass().getAnnotation(CacheConvert.class);
			String methodName = point.getSignature().getName();
			Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getParameterTypes();
			Method method = point.getTarget().getClass().getMethod(methodName, parameterTypes);
			CacheConvert methodAnnotation = method.getAnnotation(CacheConvert.class);
			methodAnnotation = methodAnnotation == null ? annotationOfClass : methodAnnotation;
			if (NumberUtil.compare(c, 0) == 0) {
				dt = methodAnnotation != null && methodAnnotation.rb() != null ? methodAnnotation.rb() : Rdb.db0;
			} else {
				dt = methodAnnotation != null && methodAnnotation.db() != null ? methodAnnotation.db() : Rdb.db2;
			}
		}
		return dt;
	}
}
