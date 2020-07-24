package com.author.commons.beans.models;

import java.io.Serializable;
import java.math.BigDecimal;

import com.author.commons.beans.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author yn
 * @since 2020-07-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OaQqRobot extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

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

}
