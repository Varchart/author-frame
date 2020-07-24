package com.author.commons.beans;

import java.math.BigDecimal;

import com.author.commons.beans.models.OaQqRobot;

import cn.hutool.core.bean.BeanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RobotDTO extends BaseEntity {
	/**
	 * 应用编号
	 */
	private Long appId;

	/**
	 * 访问人数
	 */
	private Long callPersons;

	/**
	 * 广告投放
	 */
	private Long adSend;

	/**
	 * 活跃留存
	 */
	private BigDecimal auSave;

	/**
	 * 广告收入
	 */
	private BigDecimal adIncome;

	/**
	 * Q自有渠道
	 */
	private BigDecimal qqChannel;

	/**
	 * 买量渠道
	 */
	private BigDecimal buyChannel;

	/**
	 * 数据时间(yyyyMMdd)
	 */
	private String handleDate;

	/**
	 * 是否有效(1-y,0-n)
	 */
	private Boolean valid;
	
	public OaQqRobot po() {
		OaQqRobot po = new OaQqRobot();
		BeanUtil.copyProperties(this, po, false);
		return po;
	}
}
