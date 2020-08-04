package com.author.commons.beans;

import lombok.Data;

/**
 * @Description: TODO
 * @Author: YNa
 * @Date: 2020/7/21 15:38
 * @Version: #1.0 Copyright ? 2020
 */
@Data
public class DataBean {
	private String appID;
	private String secret = "06QNu3VOaTlMqlug";
	private String state = "ry" + ((int) (Math.random() * 1000));
	private String scope = "ads_management";
	private String accountType = "ACCOUNT_TYPE_QQ";
	private String refreshGrantType = "refresh_token";
	private String codeGrantType = "authorization_code";
	private String cmd;
	private String authorCodeRedirectUri = "http://javasttts.renyouwangluo.cn/api/data/{0}-{1}/giveAuthorCode";
}
