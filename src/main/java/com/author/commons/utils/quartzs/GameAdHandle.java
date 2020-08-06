package com.author.commons.utils.quartzs;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.author.commons.beans.games.QQCampaignDataDTO;
import com.author.commons.service.games.OaQqCampaignService;
import com.author.commons.utils.Constants.redis;
import com.author.commons.utils.aspects.annotations.CacheConvert;
import com.author.commons.utils.aspects.annotations.Noder;
import com.author.commons.utils.caches.RedisImpl;
import com.author.commons.utils.enums.Ndb;
import com.author.commons.utils.enums.Rc;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("all")
public class GameAdHandle {
	@Autowired
	private RedisImpl cacheService;

	@Autowired
	private OaQqCampaignService campaignService;

	private final String REFRESH_TOKEN_URL = "https://api.e.qq.com/oauth/token?client_id={0}&client_secret={1}&grant_type={2}&refresh_token={3}",
			CAMPAIGNS_GET = "https://api.e.qq.com/v1.2/campaigns/get", DAILY_REPORT = "https://api.e.qq.com/v1.2/daily_reports/get", secret = "06QNu3VOaTlMqlug",
			REFRESH_GRANT_TYPE = "refresh_token";

	/*
	 * cron = "0 0 0 * * ?" 凌晨00点执行 fixedDelay = 5000 每5s执行一次
	 */
	@Scheduled(cron = "0 30 1 * * ?")
	public void run() {
		quartzDoor(null);
	}

	@Noder(node = Ndb.writer)
	public boolean quartzDoor(Date date) {
		log.info("GameAdHandle Data Stat Start: {}", DateUtil.now());
		if (null == date) {
			date = customDate(-1).getTime();
		}
		boolean flag = run2data(date);
		log.info("GameAdHandle Data Stat End: {}", DateUtil.now());
		return flag;
	}

	@CacheConvert
	protected boolean run2data(Date date) {
		try {
			if (null == date) {
				date = customDate(-1).getTime();
			}
			String accountID = null, appID = null, da1te = DateUtil.formatDate(date);
			Object refreshToken = null, accessToken = null, secret = null;
			LinkedHashSet<String> accounts = (LinkedHashSet) cacheService.redisResults(MessageFormat.format(redis.account_data, Rc.client_id.toString()), redis.redis2set, 0, 0);

			if (CollectionUtil.isEmpty(accounts)) {
				return false;
			}
			Iterator<String> accountItems = accounts.iterator();
			String dateRange = "{\"start_date\":\"" + da1te + "\",\"end_date\":\"" + da1te + "\"}";

			Map<String, Object> accountParams = null;
			accountParams = new HashMap<>();
			accountParams.put(Rc.page.toString(), 1);
			accountParams.put(Rc.page_size.toString(), 100);
			accountParams.put(Rc.is_deleted.toString(), "false");

			if (CollectionUtil.isNotEmpty(accountItems)) {
				do {
					accountID = accountItems.next();
					if (null != accountID) {
						appID = accountID.split(StrUtil.COLON)[0];
						accountID = accountID.split(StrUtil.COLON)[1];
					}
					if (StrUtil.isNotBlank(accountID)) {
						accessToken = cacheService.redisResults(MessageFormat.format(redis.account_data, Rc.access_token.toString() + StrUtil.COLON + accountID), redis.redis1str, 0, 0);
					}
					if (null == accessToken) {
						refreshToken = cacheService.redisResults(MessageFormat.format(redis.account_data, Rc.refresh_token.toString() + StrUtil.COLON + accountID), redis.redis1str, 0, 0);
						secret = cacheService.redisResults(MessageFormat.format(redis.account_data, Rc.secret.toString() + StrUtil.COLON + appID), redis.redis1str, 0, 0);
						String response = HttpUtil.get(MessageFormat.format(REFRESH_TOKEN_URL, appID, secret, REFRESH_GRANT_TYPE, refreshToken));
						JSONObject json = null;
						if (StrUtil.isNotBlank(response)) {
							json = JSONUtil.parseObj(response);
						}
						if (null != json) {
							accessToken = json.getJSONObject(Rc.data.toString()).getStr(Rc.access_token.toString());
						}
					}
					String accountData = null;
					JSONArray campaignItems = null;
					if (null != accessToken) {
						Long time = System.currentTimeMillis() / 1000L;
						accountParams.put(Rc.access_token.toString(), accessToken);
						accountParams.put(Rc.timestamp.toString(), time);
						accountParams.put(Rc.nonce.toString(), uuid());
						accountParams.put(Rc.account_id.toString(), accountID);
						accountParams.put(Rc.fields.toString(), "[\"campaign_id\",\"campaign_type\",\"campaign_name\",\"is_deleted\"]");
						accountData = HttpUtil.get(CAMPAIGNS_GET, accountParams);
					}
					JSONObject data = null;
					if (StrUtil.isNotBlank(accountData)) {
						data = JSONUtil.parseObj(accountData).getJSONObject(Rc.data.toString());
					}
					if (null != data) {
						campaignItems = data.getJSONArray(Rc.list.toString());
					}
					if (CollectionUtil.isNotEmpty(campaignItems)) {
						accountParams.put(Rc.nonce.toString(), uuid());
						accountParams.put(Rc.date_range.toString(), dateRange);
						accountParams.put(Rc.level.toString(), "REPORT_LEVEL_CAMPAIGN");
						accountParams.put(Rc.group_by.toString(), "[\"campaign_id\"]");
						accountParams.put(Rc.fields.toString(), "[\"campaign_id\", \"cost\"]");
						String dailiesData = HttpUtil.get(DAILY_REPORT, accountParams);
						JSONArray dailiesResult = null;
						JSONObject dailyData = null;
						if (StrUtil.isNotBlank(dailiesData)) {
							dailyData = JSONUtil.parseObj(dailiesData).getJSONObject(Rc.data.toString());
						}
						if (null != dailyData) {
							dailiesResult = dailyData.getJSONArray(Rc.list.toString());
						}
						if (CollectionUtil.isNotEmpty(dailiesResult)) {
							Iterator daily = dailiesResult.iterator();
							QQCampaignDataDTO campaign = null;
							do {
								if (campaign == null) {
									campaign = new QQCampaignDataDTO();
								}
								JSONObject result = (JSONObject) daily.next();
								JSONObject campaignData = campaignItems.stream().map(JSONObject::new)
										.filter(c -> result.getStr(Rc.campaign_id.toString()).equalsIgnoreCase(c.getStr(Rc.campaign_id.toString()))).findFirst().orElse(null);
								if (!JSONUtil.isNull(campaignData)) {
									campaignData.set(Rc.cost.toString(), (NumberUtil.div(result.getBigDecimal(Rc.cost.toString()), 100, 2)));

									campaign.setAccountId(Long.valueOf(accountID));
									campaign.setAppId(Long.valueOf(appID));
									campaign.setHandleDate(da1te);

									campaign.setCampaignId(campaignData.getLong(Rc.campaign_id.toString()));
									campaign.setCampaignType(StrUtil.sub(campaignData.getStr(Rc.campaign_type.toString()), 0, 30));
									campaign.setCampaignName(StrUtil.sub(campaignData.getStr(Rc.campaign_name.toString()), 0, 50));
									campaign.setCampaignCost(campaignData.getBigDecimal(Rc.cost.toString()));

									campaignService.insertData(campaign.po());
								}
							} while (daily.hasNext());
						}
					}
				} while (accountItems.hasNext());
			}
		} catch (Throwable ex) {
			log.error("run2data 失败: {}", ex.getMessage());
		}
		return false;
	}

	protected Calendar customDate(int time) {
		Calendar date = Calendar.getInstance();
		date.add(Calendar.DATE, time);
		return date;
	}

	protected synchronized String uuid() {
		return StrUtil.uuid().replaceAll(StrUtil.DASHED, StrUtil.EMPTY);
	}
}
