package com.author.commons.utils.aspects;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.author.commons.utils.aspects.annotations.Noder;
import com.author.commons.utils.dbs.DBContextHolder;
import com.author.commons.utils.enums.DBNodes;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class DSAspect {
	@Pointcut("@annotation(com.author.commons.utils.aspects.annotations.Noder)")
	public void noder() {
	}

	@Before("noder()")
	public void reseive(JoinPoint point) {
		try {
			Noder annotationOfClass = point.getTarget().getClass().getAnnotation(Noder.class);
			String methodName = point.getSignature().getName();
			Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getParameterTypes();
			Method method = point.getTarget().getClass().getMethod(methodName, parameterTypes);
			Noder methodAnnotation = method.getAnnotation(Noder.class);
			methodAnnotation = methodAnnotation == null ? annotationOfClass : methodAnnotation;
			DBNodes dataSourceType = methodAnnotation != null && methodAnnotation.node() != null ? methodAnnotation.node() : DBNodes.reader;
			DBContextHolder.setDBSource(dataSourceType.name());
		} catch (NoSuchMethodException ex) {
			log.warn("Aspect dbNoder inspect exception. {}", ex.getMessage());
		}
	}

	@After("noder()")
	public void clean() {
		DBContextHolder.clearDataSource();
	}
}
