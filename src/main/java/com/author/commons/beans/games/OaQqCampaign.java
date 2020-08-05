package com.author.commons.beans.games;

import java.math.BigDecimal;

import com.author.commons.beans.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class OaQqCampaign extends BaseEntity {

	private static final long serialVersionUID = 338620045420116712L;

	/**
	 * 游戏ID
	 */
	private Long gameId;

	/**
	 * 账号发布应用ID
	 */
	private Long appId;

	/**
	 * 广告主账户ID
	 */
	private Long accountId;

	/**
	 * 推广计划ID
	 */
	private Long campaignId;

	/**
	 * 推广计划名称
	 */
	private String campaignName;

	/**
	 * 推广计划类型
	 */
	private String campaignType;

	/**
	 * 推广计划花费
	 */
	private BigDecimal campaignCost;

	/**
	 * 数据时间
	 */
	private String handleDate;

	/**
	 * 是否有效(1-y,0-n)
	 */
	private boolean valid;
}