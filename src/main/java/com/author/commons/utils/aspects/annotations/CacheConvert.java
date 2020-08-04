package com.author.commons.utils.aspects.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.author.commons.utils.enums.Rdb;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.METHOD })
public @interface CacheConvert {
	Rdb db() default Rdb.db2;

	Rdb rb() default Rdb.db0;
}
