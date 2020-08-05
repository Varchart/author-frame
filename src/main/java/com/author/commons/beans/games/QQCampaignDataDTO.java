package com.author.commons.beans.games;

import java.math.BigDecimal;

import com.author.commons.beans.BaseEntity;

import cn.hutool.core.bean.BeanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description: TODO
 * @Author: YNa
 * @Date: 2020/7/17 18:03
 * @Version: #1.0 Copyright © 2020
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class QQCampaignDataDTO extends BaseEntity {
  private Long id;
  private Long appId;
  private Long gameId;
  private Long accountId;
  private Long campaignId;
  private String campaignName;
  private String campaignType;
  private String handleDate;
  private BigDecimal campaignCost;
  private boolean valid;

  public OaQqCampaign po(){
    OaQqCampaign po = new OaQqCampaign();
    BeanUtil.copyProperties(this, po);
    return po;
  }
}
