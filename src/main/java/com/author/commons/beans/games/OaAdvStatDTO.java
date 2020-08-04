package com.author.commons.beans.games;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.BeanUtils;

import com.author.commons.beans.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class OaAdvStatDTO extends BaseEntity {
	private Long id;

	/**
	 * 运营产品id
	 */
	private Long productId;

	/**
	 * 运营产品appid
	 */
	private String productAppid;

	/**
	 * 广告主合作渠道id
	 */
	private Long advChannelId;

	/**
	 * 广告主广告id
	 */
	private Long advAppId;

	/**
	 * 广告主广告appid
	 */
	private String advAppAppid;

	/**
	 * 广告主广告上线id
	 */
	private Long advId;

	/**
	 * 广告主广告上线名称
	 */
	private String advName;

	/**
	 * 位置ID
	 */
	private Integer positionId;

	/**
	 * 统计时间
	 */
	private Date summarydate;

	/**
	 * 展示次数
	 */
	private Long showNumber;

	/**
	 * 点击广告
	 */
	private Long clickAd;

	/**
	 * 成功打开
	 */
	private Long successOpen;

	/**
	 * 去重点击
	 */
	private Long uniqueClick;

	/**
	 * 广告主实际数据（手填）
	 */
	private Long realityData;

	/**
	 * 有效点击（去重用户）
	 */
	private Long validClick;

	/**
	 * 占比
	 */
	private BigDecimal dutyRate;

	/**
	 * 单价
	 */
	private BigDecimal price;

	/**
	 * 收入
	 */
	private BigDecimal income;

	/**
	 * 转化
	 */
	private BigDecimal translation;

	/**
	 * 价值
	 */
	private BigDecimal cost;

	/**
	 * 合作方式id
	 */
	private Integer cooperationId;

	/**
	 * 合作方式名称
	 */
	private String cooperationName;

	/**
	 * 量级需求
	 */
	private String magnitude;

	private Boolean status;

	/**
	 * 总金额
	 */
	private BigDecimal amount;

	/**
	 * 单价时间节点组
	 */
	private String priceNodes;

	public OaAdvStat getPo() {
		OaAdvStat po = new OaAdvStat();
		BeanUtils.copyProperties(this, po);
		return po;
	}
}