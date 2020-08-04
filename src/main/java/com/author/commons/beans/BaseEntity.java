package com.author.commons.beans;

import java.time.LocalDateTime;

import cn.hutool.core.date.DateUtil;
import cn.hutool.db.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class BaseEntity extends Page {
	private static final long serialVersionUID = -3354728353006741810L;
	private Long id;
	private LocalDateTime createTime = DateUtil.parseLocalDateTime(DateUtil.now());
	private LocalDateTime updateTime = DateUtil.parseLocalDateTime(DateUtil.now());
}
