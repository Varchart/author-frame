package com.author.commons.beans;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ChannelDetailDTO {
	private BigDecimal income;
	private BigDecimal qqChannel;
	private BigDecimal buyChannel;
}
