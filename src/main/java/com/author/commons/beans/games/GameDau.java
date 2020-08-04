package com.author.commons.beans.games;

import java.util.Date;

import lombok.Data;

/**
 * @Description: TODO
 * @Author: YNa
 * @Date: 2020/6/19 18:15
 * @Version: #1.0 Copyright © 2020
 */
@Data
public class GameDau {
	private Long id;

	private String ak;

	private String openId;

	private String uu;

	private Date createDate;

	private Date createTime;

	private Date loginDate;

	private Date loginTime;
}
