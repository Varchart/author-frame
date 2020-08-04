package com.author.commons.beans.games;

import java.math.BigDecimal;

import lombok.Data;

/**
 *  Created by YNa on 2020年5月18日16点16分
 *  @description: 广告数据统计查询条件DTO
 */
@Data
public class StatParamsDTO {
    private Long advId;
    private Long productId;
    private Long count = 0l;
    private Long dist = 0l;
    private Long allowCount = 0l;
    private Long allowDist = 0l;
    private String summaryDate;
    private BigDecimal rate100 = BigDecimal.ZERO;
}
