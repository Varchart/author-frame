package com.author.commons.beans;

import java.time.LocalDateTime;

import cn.hutool.core.date.DateUtil;
import lombok.Data;

@Data
public abstract class BaseEntity {
	private Long id;
	private LocalDateTime createTime = DateUtil.parseLocalDateTime(DateUtil.now());
	private LocalDateTime updateTime = DateUtil.parseLocalDateTime(DateUtil.now());
}
