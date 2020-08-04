package com.author.commons.utils.aspects.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description: TODO
 * @Author: YNa
 * @Date: 2020/6/15 14:01
 * @Version: #1.0 Copyright © 2020
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.METHOD })
public @interface PageQry {
	String pageIndexName() default "currentPage";

	String pageSizeName() default "pageSize";

	int pageSize() default 20;

	int pageNum() default 1;
}
