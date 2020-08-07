package com.author.commons.utils.quartzs;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.author.commons.beans.ChannelDetailDTO;
import com.author.commons.beans.models.OaQqRobot;
import com.author.commons.service.IOaQqRobotService;
import com.author.commons.utils.Constants;
import com.author.commons.utils.Constants.ad;
import com.author.commons.utils.Constants.od;
import com.author.commons.utils.Constants.rd;
import com.author.commons.utils.Constants.redis;
import com.author.commons.utils.Constants.ru;
import com.author.commons.utils.Constants.uri;
import com.author.commons.utils.aspects.annotations.CacheConvert;
import com.author.commons.utils.caches.RedisImpl;
import com.author.commons.utils.enums.Rc;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("all")
public class RunHandle {
	@Autowired
	private RedisImpl cacheService;

	@Autowired
	private IOaQqRobotService iRobotService;

	/*
	 * cron = "0 0 0 * * ?" 凌晨00点执行 fixedDelay = 5000 每5s执行一次
	 */
	@CacheConvert
	@Scheduled(cron = "0 30 1 * * ?")
	public void run1data() {
		StringBuffer cookies = null;
		String ks = MessageFormat.format(redis.account_data, Rc.quid + StrUtil.COLON + Constants.ask);
		Set<?> sks = cacheService.keys(ks);
		Iterator<?> rs = sks.iterator();
		if (CollectionUtil.isNotEmpty(rs)) {
			do {
				String quid = StrUtil.toString(rs.next());

				String appID = quid.substring(quid.lastIndexOf(StrUtil.COLON) + 1), qticket = MessageFormat.format(redis.account_data, Rc.qticket + StrUtil.COLON + appID);

				quid = StrUtil.toString(cacheService.redisResults(quid, redis.redis1str, 0, 0));
				qticket = StrUtil.toString(cacheService.redisResults(qticket, redis.redis1str, 0, 0));
				if (StrUtil.isNotBlank(qticket)) {

					cookies = new StringBuffer();
					cookies.append(Rc.quid).append(Constants.equals).append(quid).append(Constants.split);
					cookies.append(Rc.qticket).append(Constants.equals).append(qticket).append(Constants.split);

					OaQqRobot record = null;
					if (null == record) {
						record = new OaQqRobot();
					}

					record.setAppId(StrUtil.isNotBlank(appID) ? Long.valueOf(appID) : null);

					String email = getDeveloper(appID, cookies);
					record.setEmail(StrUtil.sub(email, 0, 50));

					String ftime = DateUtil.format(customDate(-1).getTime(), DatePattern.PURE_DATE_PATTERN);
					record.setHandleDate(ftime);
					/* 访问人数 */
					String callPersons = getGeneralSituation(appID, cookies);
					record.setCallPersons(StrUtil.isNotBlank(callPersons) ? Long.valueOf(callPersons) : null);
					/* 活跃留存 */
					BigDecimal uaWait = getRetentionTrend(appID, cookies);
					record.setUaWait(uaWait);
					/* 人均停留时长 */
					String uwTime = getOperationData(appID, cookies);
					record.setUwTime(uwTime);
					/* 广告投放 */
					String adSend = getReferUvs(appID, cookies);
					record.setAdSend(StrUtil.isNotBlank(adSend) ? Long.valueOf(adSend) : null);
					/* 收入流水(元) */
					ChannelDetailDTO channelResult = getAdDataDaily(appID, cookies);
					BeanUtil.copyProperties(channelResult, record, false);

					iRobotService.saveActive(record);
				}
			} while (rs.hasNext());
		} else {
			log.error("授权信息缺失:{quid}, {qticket}");
		}
		log.info("run1data 结束时间:{}", DateUtil.now());
	}

	/**
	 * 登录态校验
	 * 
	 * @param resp 响应
	 * @return
	 */
	protected boolean loginCheck(String resp, String uri, Object appID) {
		if (JSONUtil.isJson(resp)) {
			String rtnCode = JSONUtil.parseObj(resp).getStr(Rc.code.toString());
			if (StrUtil.equalsIgnoreCase(Constants.log.NOT_LOGIN.c(), rtnCode)) {
				/* 登录态校验失败 */
				log.warn("{}({}):{}, {}", Constants.log.NOT_LOGIN.m(), appID, uri, DateUtil.now());
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * 获得账号
	 * 
	 * @param appID
	 * @param cookies
	 * @return
	 */
	protected String getDeveloper(String appID, StringBuffer cookies) {
		try {
			/* 获得账号 */
			String resp = get(uri.getDeveloper, StrUtil.toString(cookies), null);
			boolean loginFlag = loginCheck(resp, uri.getDeveloper, appID);
			if (!loginFlag) {
				return null;
			}
			JSONObject data = JSONUtil.parseObj(resp).getJSONObject(Rc.data.toString());
			if (JSONUtil.isNull(data)) {
				return null;
			}
			JSONObject developerInfo = data.getJSONObject(Constants.di.developerInfo.toString());
			if (!JSONUtil.isNull(developerInfo)) {
				return developerInfo.getStr(Constants.di.email.toString());
			}
		} catch (Throwable ex) {
			log.error("getDeveloper 失败:{}", ex.getMessage());
		}
		return null;
	}

	/**
	 * 访问人数
	 * 
	 * @param appID
	 * @param cookies
	 * @return
	 */
	protected String getGeneralSituation(String appID, StringBuffer cookies) {
		JSONObject params = null;
		try {
			if (null == params) {
				params = new JSONObject();
				params.set(Rc.appid.toString(), appID);
			}
			/* 访问人数类型 */
			String gs5type = "5", resp = post(uri.generalSituationUri, StrUtil.toString(cookies), StrUtil.toString(params));
			boolean loginFlag = loginCheck(resp, uri.generalSituationUri, appID);
			if (!loginFlag) {
				return null;
			}
			JSONObject data = JSONUtil.parseObj(resp).getJSONObject(Rc.data.toString());
			if (JSONUtil.isNull(data)) {
				return null;
			}
			JSONArray generalSituations = data.getJSONArray(Constants.gs.generalSituation.toString());
			if (JSONUtil.isNull(generalSituations)) {
				return null;
			}
			Iterator results = generalSituations.iterator();

			if (CollectionUtil.isEmpty(results)) {
				return null;
			}
			/* 访问人数 */
			do {
				JSONObject result = (JSONObject) results.next();
				if (StrUtil.equalsIgnoreCase(result.getStr(Constants.gs.dataType.toString()), gs5type)) {
					return result.getStr(Constants.gs.number.toString());
				}
			} while (results.hasNext());
		} catch (Throwable ex) {
			log.error("getGeneralSituation 失败:{}", ex.getMessage());
		}
		return null;
	}

	/**
	 * 活跃留存
	 * 
	 * @param appID
	 * @param cookies
	 * @return
	 */
	protected BigDecimal getRetentionTrend(String appID, StringBuffer cookies) {
		JSONObject params = null;
		try {
			/* 活跃留存类型 */
			String type = "2", dt = "3", realTime = DateUtil.format(customDate(-2).getTime(), DatePattern.PURE_DATE_PATTERN);
			if (null == params) {
				params = new JSONObject();
				params.set(Rc.appid.toString(), appID);
				params.set(rd.calculate_type.toString(), type);
				params.set(rd.real_time_begin.toString(), realTime);
				params.set(rd.real_time_end.toString(), realTime);
			}
			String resp = post(uri.retentionDataUri, StrUtil.toString(cookies), StrUtil.toString(params));
			boolean loginFlag = loginCheck(resp, uri.retentionDataUri, appID);
			if (!loginFlag) {
				return null;
			}
			JSONObject data = JSONUtil.parseObj(resp).getJSONObject(Rc.data.toString());
			if (JSONUtil.isNull(data)) {
				return null;
			}
			JSONArray retentionDatas = data.getJSONArray(rd.retentionDatas.toString());
			if (JSONUtil.isNull(retentionDatas)) {
				return null;
			}
			Iterator<Object> results = retentionDatas.iterator();
			if (CollectionUtil.isEmpty(results)) {
				return null;
			}
			/* 活跃留存 */
			do {
				JSONObject result = (JSONObject) results.next();
				if (StrUtil.equalsIgnoreCase(result.getStr(rd.date_type.toString()), dt)) {
					/* 次日留存 */
					BigDecimal val = NumberUtil.toBigDecimal(result.getStr(rd.data_value.toString()));
					return NumberUtil.round(NumberUtil.mul(val, 100), 2, RoundingMode.CEILING);
				}
			} while (results.hasNext());
		} catch (Throwable ex) {
			log.error("getRetentionTrend 失败:{}", ex.getMessage());
		}
		return BigDecimal.ZERO;
	}

	/**
	 * 人均停留时长
	 * 
	 * @param appID
	 * @param cookies
	 * @return
	 */
	protected String getOperationData(String appID, StringBuffer cookies) {
		JSONObject params = null;
		try {
			/* 人均停留时长 */
			String ftime = DateUtil.format(customDate(-1).getTime(), DatePattern.PURE_DATE_PATTERN);
			if (null == params) {
				params = new JSONObject();
				params.set(Rc.appid.toString(), appID);
				params.set(od.ftimeBegin.toString(), ftime);
				params.set(od.ftimeEnd.toString(), ftime);
			}
			String resp = post(uri.operationDataUri, StrUtil.toString(cookies), StrUtil.toString(params));
			boolean loginFlag = loginCheck(resp, uri.operationDataUri, appID);
			if (!loginFlag) {
				return null;
			}
			JSONObject data = JSONUtil.parseObj(resp).getJSONObject(Rc.data.toString());
			if (JSONUtil.isNull(data)) {
				return null;
			}
			JSONArray accessDatas = data.getJSONArray(od.accessDatas.toString());
			if (JSONUtil.isNull(accessDatas)) {
				return null;
			}
			Iterator<Object> results = accessDatas.iterator();
			if (CollectionUtil.isEmpty(results)) {
				return null;
			}
			/* 人均停留时长 */
			do {
				JSONObject result = (JSONObject) results.next();
				if (StrUtil.equalsIgnoreCase(result.getStr(Rc.ftime.toString()), ftime)) {
					return StrUtil.sub(result.getStr(od.uvTime.toString()), 0, 3);
				}
			} while (results.hasNext());
		} catch (Throwable ex) {
			log.error("getOperationData 失败:{}", ex.getMessage());
		}
		return null;
	}

	/**
	 * 广告投放
	 * 
	 * @param appID
	 * @param cookies
	 * @return
	 */
	protected String getReferUvs(String appID, StringBuffer cookies) {
		JSONObject params = null;
		try {
			String ftime = DateUtil.format(customDate(-1).getTime(), DatePattern.PURE_DATE_PATTERN), ot = "2", tt = "1", rf = "2054";
			if (null == params) {
				params = new JSONObject();
				params.set(Rc.appid.toString(), appID);
				params.set(ru.ftimeBegin.toString(), ftime);
				params.set(ru.ftimeEnd.toString(), ftime);
				params.set(ru.order_type.toString(), ot);
				params.set(ru.time_tag.toString(), tt);
			}

			String resp = post(uri.referUvsUri, StrUtil.toString(cookies), StrUtil.toString(params));
			boolean loginFlag = loginCheck(resp, uri.referUvsUri, appID);
			if (!loginFlag) {
				return null;
			}
			JSONObject data = JSONUtil.parseObj(resp).getJSONObject(Rc.data.toString());
			if (JSONUtil.isNull(data)) {
				return null;
			}
			JSONArray referUvs = data.getJSONArray(ru.referUvs.toString());
			if (JSONUtil.isNull(referUvs)) {
				return null;
			}
			Iterator<Object> results = referUvs.iterator();
			if (CollectionUtil.isEmpty(results)) {
				return null;
			}
			/* 广告投放 */
			do {
				JSONObject result = (JSONObject) results.next();
				if (StrUtil.equalsIgnoreCase(result.getStr(Rc.ftime.toString()), ftime) && StrUtil.equalsIgnoreCase(result.getStr(ru.refer.toString()), rf)) {
					return result.getStr(ru.uv.toString());
				}
			} while (results.hasNext());
		} catch (Throwable ex) {
			log.error("getReferUvs 失败:{}", ex.getMessage());
		}
		return null;
	}

	/**
	 * 收入流水(元)
	 * 
	 * @param appID
	 * @param cookies
	 * @return
	 */
	protected ChannelDetailDTO getAdDataDaily(String appID, StringBuffer cookies) {
		ChannelDetailDTO rts = null;
		JSONObject params = null;
		try {
			String ftime = DateUtil.format(customDate(-1).getTime(), DatePattern.PURE_DATE_PATTERN), at = "0", needSubPosData = "0";
			int qct = 1, sct = 2;
			if (null == params) {
				params = new JSONObject();
				params.set(Rc.appid.toString(), appID);
				params.set(ad.ftimeBegin.toString(), ftime);
				params.set(ad.ftimeEnd.toString(), ftime);
				params.set(ad.needSubPosData.toString(), needSubPosData);
			}
			if (null == rts) {
				rts = new ChannelDetailDTO();
			}

			/* 广告投放 */
			JSONArray adDataDailyList = channelData(cookies, params);
			if (CollectionUtil.isNotEmpty(adDataDailyList)) {
				Iterator<Object> results = adDataDailyList.iterator();
				if (CollectionUtil.isNotEmpty(results)) {
					do {
						JSONObject result = (JSONObject) results.next();
						if (StrUtil.equalsIgnoreCase(result.getStr(Rc.ftime.toString()), ftime) && StrUtil.equalsIgnoreCase(result.getStr(ad.adType.toString()), at)) {
							rts.setAdIncome(millionNumber(result));
						}
					} while (results.hasNext());
				}
			}

			/* 手Q渠道数据 */
			params.set(ad.channelType.toString(), qct);
			adDataDailyList = channelData(cookies, params);
			if (CollectionUtil.isNotEmpty(adDataDailyList)) {
				Iterator<Object> results = adDataDailyList.iterator();
				if (CollectionUtil.isNotEmpty(results)) {
					do {
						JSONObject result = (JSONObject) results.next();
						if (StrUtil.equalsIgnoreCase(result.getStr(Rc.ftime.toString()), ftime) && StrUtil.equalsIgnoreCase(result.getStr(ad.adType.toString()), at)) {
							rts.setQqChannel(millionNumber(result));
						}
					} while (results.hasNext());
				}
			}

			/* 买量渠道数据 */
			params.set(ad.channelType.toString(), sct);
			adDataDailyList = channelData(cookies, params);
			if (CollectionUtil.isNotEmpty(adDataDailyList)) {
				Iterator<Object> results = adDataDailyList.iterator();
				if (CollectionUtil.isNotEmpty(results)) {
					do {
						JSONObject result = (JSONObject) results.next();
						if (StrUtil.equalsIgnoreCase(result.getStr(Rc.ftime.toString()), ftime) && StrUtil.equalsIgnoreCase(result.getStr(ad.adType.toString()), at)) {
							rts.setBuyChannel(millionNumber(result));
						}
					} while (results.hasNext());
				}
			}
		} catch (Throwable ex) {
			log.error("getAdDataDaily 失败:{}", ex.getMessage());
		}
		return rts;
	}

	protected JSONArray channelData(StringBuffer cookies, JSONObject params) {
		String resp = post(uri.adDataDailyUri, StrUtil.toString(cookies), StrUtil.toString(params));
		boolean loginFlag = loginCheck(resp, uri.adDataDailyUri, params.get(Rc.appid.toString()));
		if (loginFlag) {
			JSONObject data = JSONUtil.parseObj(resp).getJSONObject(Rc.data.toString());
			if (JSONUtil.isNull(data)) {
				return null;
			}
			return data.getJSONArray(ad.AdDataDailyList.toString());
		}
		return null;
	}

	protected BigDecimal millionNumber(JSONObject result) {
		synchronized (result) {
			String number = result.getStr(ad.revenue.toString());
			return NumberUtil.div(StrUtil.isNotBlank(number) ? number : "0", Constants.million, 2);
		}
	}

	protected String post(String uri, String cookies, String params) {
		return HttpRequest.post(uri).header(Rc.cookie.toString(), cookies).body(params).execute().body();
	}

	protected String get(String uri, String cookies, String params) {
		return HttpRequest.get(uri).header(Rc.cookie.toString(), cookies).body(params).execute().body();
	}

	protected Calendar customDate(int time) {
		Calendar date = Calendar.getInstance();
		date.add(Calendar.DATE, time);
		return date;
	}
}
