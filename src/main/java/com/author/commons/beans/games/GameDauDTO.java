package com.author.commons.beans.games;

import com.author.commons.beans.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description: TODO
 * @Author: YNa
 * @Date: 2020/6/19 18:15
 * @Version: #1.0 Copyright © 2020
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class GameDauDTO extends BaseEntity {
  private String tableName;
  private String startTime;
  private String endTime;
}
