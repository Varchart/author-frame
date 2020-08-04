package com.author.commons.utils.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import com.github.pagehelper.PageHelper;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.db.Page;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: TODO
 * @Author: YNa
 * @Date: 2020/6/15 14:09
 * @Version: #1.0 Copyright © 2020
 */
@Slf4j
@Aspect
@Component
public class PageAspect {
	@Pointcut("@annotation(com.author.commons.utils.aspects.annotations.PageQry)")
	public void cut() {
	}

	@Before("cut()")
	public void paging(JoinPoint joinPoint) {
		Object[] args = joinPoint.getArgs();

		if (ArrayUtil.isNotEmpty(args)) {

			Object params = args[0];
			if (params instanceof Page) {
				Page page = (Page) params;
				PageHelper.startPage(page.getPageNumber(), page.getPageSize());
			}
		} else {
			log.warn("分页失败,未定义Page参数.");
		}
	}
}
