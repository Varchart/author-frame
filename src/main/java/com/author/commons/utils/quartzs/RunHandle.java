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
import com.author.commons.utils.enums.Rc;
import com.author.commons.utils.redis.RedisImpl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
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
	@Scheduled(cron = "0 0 0 * * ?")
	public void run1data() {
		StringBuffer cookies = null;
		String ks = MessageFormat.format(Constants.redis.account_data, Rc.quid + StrUtil.COLON + Constants.ask);
		Set<?> sks = cacheService.keys(ks);
		Iterator<?> rs = sks.iterator();
		if (CollectionUtil.isNotEmpty(rs)) {
			do {
				String quid = StrUtil.toString(rs.next());

				String appID = quid.substring(quid.lastIndexOf(StrUtil.COLON) + 1), qticket = MessageFormat.format(Constants.redis.account_data, Rc.qticket + StrUtil.COLON + appID);

				quid = StrUtil.toString(cacheService.redisResults(quid, Constants.redis.redis1str, 0, 0));
				qticket = StrUtil.toString(cacheService.redisResults(qticket, Constants.redis.redis1str, 0, 0));
				if (StrUtil.isNotBlank(qticket)) {

					cookies = new StringBuffer();
					cookies.append(Rc.quid).append(Constants.equals).append(quid).append(Constants.split);
					cookies.append(Rc.qticket).append(Constants.equals).append(qticket).append(Constants.split);

					OaQqRobot record = null;
					if (null == record) {
						record = new OaQqRobot();
					}

					record.setAppId(StrUtil.isNotBlank(appID) ? Long.valueOf(appID) : null);

					Object email = getDeveloper(appID, cookies);
					record.setEmail(StrUtil.toString(email));

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
		Object code = JSONUtil.parseObj(resp).get(Rc.code.toString());
		if (ObjectUtil.equal(Constants.log.NOT_LOGIN.c(), code)) {
			/* 登录态校验失败 */
			log.warn("{}({}):{}, {}", Constants.log.NOT_LOGIN.m(), appID, uri, DateUtil.now());
			return false;
		}
		return true;
	}

	/**
	 * 获得账号
	 * 
	 * @param appID
	 * @param cookies
	 * @return
	 */
	protected Object getDeveloper(String appID, StringBuffer cookies) {
		try {
			/* 获得账号 */
			String resp = get(Constants.uri.getDeveloper, StrUtil.toString(cookies), null);
			boolean loginFlag = loginCheck(resp, Constants.uri.getDeveloper, appID);
			if (loginFlag) {
				Object data = JSONUtil.parseObj(resp).get(Rc.data.toString());
				if (null != data) {
					Object developerInfo = JSONUtil.parseObj(data).get(Constants.di.developerInfo.toString());
					if (null != developerInfo) {
						return JSONUtil.parseObj(developerInfo).get(Constants.di.email.toString());
					}
				}
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
			String gs5type = "5", resp = post(Constants.uri.generalSituationUri, StrUtil.toString(cookies), StrUtil.toString(params));
			boolean loginFlag = loginCheck(resp, Constants.uri.generalSituationUri, appID);
			if (loginFlag) {
				Object data = JSONUtil.parseObj(resp).get(Rc.data.toString());
				JSONArray generalSituations = JSONUtil.parseArray(JSONUtil.parseObj(data).get(Constants.gs.generalSituation.toString()));
				Iterator<Object> results = generalSituations.iterator();

				/* 访问人数 */
				if (CollectionUtil.isNotEmpty(results)) {
					do {
						JSONObject result = (JSONObject) results.next();
						if (StrUtil.equalsIgnoreCase(StrUtil.toString(result.get(Constants.gs.dataType.toString())), gs5type)) {
							return StrUtil.toString(result.get(Constants.gs.number.toString()));
						}
					} while (results.hasNext());
				}
			}
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
				params.set(Constants.rd.calculate_type.toString(), type);
				params.set(Constants.rd.real_time_begin.toString(), realTime);
				params.set(Constants.rd.real_time_end.toString(), realTime);
			}

			String resp = post(Constants.uri.retentionDataUri, StrUtil.toString(cookies), StrUtil.toString(params));
			boolean loginFlag = loginCheck(resp, Constants.uri.retentionDataUri, appID);
			if (loginFlag) {
				Object data = JSONUtil.parseObj(resp).get(Rc.data.toString());
				JSONArray retentionDatas = JSONUtil.parseArray(JSONUtil.parseObj(data).get(Constants.rd.retentionDatas.toString()));
				Iterator<Object> results = retentionDatas.iterator();
				/* 活跃留存 */
				if (CollectionUtil.isNotEmpty(results)) {
					do {
						JSONObject result = (JSONObject) results.next();
						if (StrUtil.equalsIgnoreCase(StrUtil.toString(result.get(Constants.rd.date_type.toString())), dt)) {
							/* 次日留存 */
							BigDecimal val = NumberUtil.toBigDecimal(StrUtil.toString(result.get(Constants.rd.data_value.toString())));
							return NumberUtil.round(NumberUtil.mul(val, 100), 2, RoundingMode.CEILING);
						}
					} while (results.hasNext());
				}
			}
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
				params.set(Constants.od.ftimeBegin.toString(), ftime);
				params.set(Constants.od.ftimeEnd.toString(), ftime);
			}

			String resp = post(Constants.uri.operationDataUri, StrUtil.toString(cookies), StrUtil.toString(params));
			boolean loginFlag = loginCheck(resp, Constants.uri.operationDataUri, appID);
			if (loginFlag) {
				Object data = JSONUtil.parseObj(resp).get(Rc.data.toString());
				JSONArray accessDatas = JSONUtil.parseArray(JSONUtil.parseObj(data).get(Constants.od.accessDatas.toString()));
				Iterator<Object> results = accessDatas.iterator();
				/* 人均停留时长 */
				if (CollectionUtil.isNotEmpty(results)) {
					do {
						JSONObject result = (JSONObject) results.next();
						if (StrUtil.equalsIgnoreCase(StrUtil.toString(result.get(Rc.ftime.toString())), ftime)) {
							return StrUtil.sub(StrUtil.toString(result.get(Constants.od.uvTime.toString())), 0, 3);
						}
					} while (results.hasNext());
				}
			}
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
			String ftime = DateUtil.format(customDate(-1).getTime(), DatePattern.PURE_DATE_PATTERN), ot = "2", tt = "1", r = "2054";
			if (null == params) {
				params = new JSONObject();
				params.set(Rc.appid.toString(), appID);
				params.set(Constants.ru.ftimeBegin.toString(), ftime);
				params.set(Constants.ru.ftimeEnd.toString(), ftime);
				params.set(Constants.ru.order_type.toString(), ot);
				params.set(Constants.ru.time_tag.toString(), tt);
			}

			String resp = post(Constants.uri.referUvsUri, StrUtil.toString(cookies), StrUtil.toString(params));
			boolean loginFlag = loginCheck(resp, Constants.uri.referUvsUri, appID);
			if (loginFlag) {
				Object data = JSONUtil.parseObj(resp).get(Rc.data.toString());
				JSONArray referUvs = JSONUtil.parseArray(JSONUtil.parseObj(data).get(Constants.ru.referUvs.toString()));
				Iterator<Object> results = referUvs.iterator();
				/* 广告投放 */
				if (CollectionUtil.isNotEmpty(results)) {
					do {
						JSONObject result = (JSONObject) results.next();
						if (StrUtil.equalsIgnoreCase(StrUtil.toString(result.get(Rc.ftime.toString())), ftime)) {
							return StrUtil.toString(result.get(Constants.ru.uv.toString()));
						}
					} while (results.hasNext());
				}
			}
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
				params.set(Constants.ad.ftimeBegin.toString(), ftime);
				params.set(Constants.ad.ftimeEnd.toString(), ftime);
				params.set(Constants.ad.needSubPosData.toString(), needSubPosData);
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
						if (StrUtil.equalsIgnoreCase(StrUtil.toString(result.get(Rc.ftime.toString())), ftime)
								&& StrUtil.equalsIgnoreCase(StrUtil.toString(result.get(Constants.ad.adType.toString())), at)) {
							rts.setAdIncome(millionNumber(result));
						}
					} while (results.hasNext());
				}
			}

			/* 手Q渠道数据 */
			params.set(Constants.ad.channelType.toString(), qct);
			adDataDailyList = channelData(cookies, params);
			if (CollectionUtil.isNotEmpty(adDataDailyList)) {
				Iterator<Object> results = adDataDailyList.iterator();
				if (CollectionUtil.isNotEmpty(results)) {
					do {
						JSONObject result = (JSONObject) results.next();
						if (StrUtil.equalsIgnoreCase(StrUtil.toString(result.get(Rc.ftime.toString())), ftime)
								&& StrUtil.equalsIgnoreCase(StrUtil.toString(result.get(Constants.ad.adType.toString())), at)) {
							rts.setQqChannel(millionNumber(result));
						}
					} while (results.hasNext());
				}
			}

			/* 买量渠道数据 */
			params.set(Constants.ad.channelType.toString(), sct);
			adDataDailyList = channelData(cookies, params);
			if (CollectionUtil.isNotEmpty(adDataDailyList)) {
				Iterator<Object> results = adDataDailyList.iterator();
				if (CollectionUtil.isNotEmpty(results)) {
					do {
						JSONObject result = (JSONObject) results.next();
						if (StrUtil.equalsIgnoreCase(StrUtil.toString(result.get(Rc.ftime.toString())), ftime)
								&& StrUtil.equalsIgnoreCase(StrUtil.toString(result.get(Constants.ad.adType.toString())), at)) {
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
		String resp = post(Constants.uri.adDataDailyUri, StrUtil.toString(cookies), StrUtil.toString(params));
		boolean loginFlag = loginCheck(resp, Constants.uri.adDataDailyUri, params.get(Rc.appid.toString()));
		if (loginFlag) {
			Object data = JSONUtil.parseObj(resp).get(Rc.data.toString());
			return JSONUtil.parseArray(JSONUtil.parseObj(data).get(Constants.ad.AdDataDailyList.toString()));
		}
		return null;
	}

	protected BigDecimal millionNumber(JSONObject result) {
		synchronized (result) {
			String number = StrUtil.toString(result.get(Constants.ad.revenue.toString()));
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
