package com.author.commons.beans;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class HeadBean {
	@NotNull(message = "appID缺失.")
	private String appID;
	@NotNull(message = "quID缺失.")
	private String quID;
	@NotNull(message = "qticket缺失.")
	private String qticket;
}
