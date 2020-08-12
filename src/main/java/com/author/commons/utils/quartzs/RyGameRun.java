package com.author.commons.utils.quartzs;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.author.commons.beans.games.GameDauDTO;
import com.author.commons.beans.games.OaAdvStat;
import com.author.commons.beans.games.OaAdvStatDTO;
import com.author.commons.beans.games.OaAdvValueSheet;
import com.author.commons.beans.games.OaAdvValueSheetDTO;
import com.author.commons.beans.games.StatParamsDTO;
import com.author.commons.dao.games.GameDauMapper;
import com.author.commons.service.games.OaAdvStatService;
import com.author.commons.service.games.OaAdvValueSheetService;
import com.author.commons.utils.Constants;
import com.author.commons.utils.Constants.redis;
import com.author.commons.utils.caches.RedisImpl;
import com.author.commons.utils.threads.RunThread;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yn
 *
 */
@Slf4j
@Component
@Transactional
@SuppressWarnings("all")
public class RyGameRun {
	@Resource
	private GameDauMapper gameDauMapper;
	@Resource
	private RedisImpl cacheService;
	@Resource
	private OaAdvValueSheetService valueSheetService;
	@Resource
	private OaAdvStatService statService;
	@Resource
	private Executor tsExecutor;

	private Calendar date;

	private static final String day_key = "adv:*:day:{0}:*:*";
	private static final String dau = "game_{0}_dau";

	@Scheduled(cron = "0 30 2 * * ?")
	public boolean run() {
		this.date = null;
		return quartzDoor(null);
	}

	public boolean quartzDoor(Date date) {
		log.info("RyGameRun Data Stat Start: {}", DateUtil.now());
		if (null == date) {
			date = customDate(-1).getTime();
		}
		boolean flag = daySaveOperator(queryStatRedis(DateUtil.formatDate(date), 0));
		log.info("RyGameRun Data Stat End: {}", DateUtil.now());
		return flag;
	}

	@Transactional
	protected boolean daySaveOperator(Map<String, Long> items) {
		CopyOnWriteArrayList<OaAdvStat> saveItems = null;
		try {
			saveItems = tr(items, 0);
			dbBatch(saveItems, 0);
			return true;
		} catch (Throwable ex) {
			log.error("daySaveOperator() Data Handler Exception:" + ex.getMessage());
		} finally {
			System.gc();
		}
		return false;
	}

	protected CopyOnWriteArrayList tr(Map<String, Long> cacheItems, int tn) {
		int t = 0;
		CopyOnWriteArrayList<OaAdvStat> saveItems = new CopyOnWriteArrayList<>();
		tn = (tn <= 0 ? Constants.threadNumber : tn);
		CountDownLatch countDownLatch = new CountDownLatch(tn);
		do {
			List<String> keys = new ArrayList(cacheItems.entrySet());
			int size = keys.size();
			/* 为每一位线程分配任务 */
			int startIndex = size / tn * t, endIndex = size / tn * (t + 1);
			if (t == tn - 1) {
				endIndex = size;
			}

			List threadResults = keys.subList(startIndex, endIndex);
			if (date == null) {
				date = customDate(-1);
			}

			tsExecutor.execute(new Thread(new RunThread() {
				@Override
				public void run2doing() {
					try {
						if (CollectionUtil.isNotEmpty(threadResults)) {
							synchronized (threadResults) {
								Iterator<ConcurrentHashMap.Entry<String, Long>> iter = threadResults.iterator();
								do {
									ConcurrentHashMap.Entry<String, Long> item = iter.next();
									try {
										/* 通过获取到的缓存标识,检索对应的相关信息封装 */
										String[] keyChar = item.getKey().split(StrUtil.COLON);
										/* 组装匹配条件(统计日期+应用ID+广告ID) */
										String outDateItem = keyChar[keyChar.length - 4], outProductItem = keyChar[keyChar.length - 2], outAdvertItem = keyChar[keyChar.length - 1];
										/* 验证是否是上一天的数据,任务启动时间默认为次日 */
										if (StrUtil.equalsIgnoreCase(DateUtil.formatDate(date.getTime()), outDateItem)) {
											String mateItem = new StringBuffer(outDateItem).append(outProductItem).append(outAdvertItem).toString().trim();
											/* 匹配ING */
											Iterator<ConcurrentHashMap.Entry<String, Long>> searchItems = threadResults.iterator();
											StatParamsDTO param = null;
											do {
												ConcurrentHashMap.Entry<String, Long> searchItem = searchItems.next();
												try {
													String[] searchChar = searchItem.getKey().split(StrUtil.COLON);
													/* 组装匹配条件(统计日期+应用ID+广告ID) */
													String innerDateItem = searchChar[searchChar.length - 4], innerProductItem = searchChar[searchChar.length - 2], innerAdvertItem = searchChar[searchChar.length - 1];
													String search = new StringBuffer(innerDateItem).append(innerProductItem).append(innerAdvertItem).toString().trim();
													if (StrUtil.equalsIgnoreCase(mateItem, search)) {
														if (param == null) {
															param = new StatParamsDTO();
														}
														param.setAdvId(Long.valueOf(innerAdvertItem));
														param.setProductId(Long.valueOf(innerProductItem));
														if (StrUtil.contains(searchItem.getKey(), columns.click.toString())) {
															if (StrUtil.contains(searchItem.getKey(), columns.count.toString())) {
																param.setCount(searchItem.getValue());
															}
															if (StrUtil.contains(searchItem.getKey(), columns.dist.toString())) {
																param.setDist(searchItem.getValue());
															}
														}
														if (StrUtil.contains(searchItem.getKey(), columns.allow.toString())) {
															if (searchItem.getKey().contains(columns.count.toString())) {
																param.setAllowCount(searchItem.getValue());
															}
															if (StrUtil.contains(searchItem.getKey(), columns.dist.toString())) {
																param.setAllowDist(searchItem.getValue());
																/* 日活 */
																if (BigDecimal.ZERO.compareTo(param.getRate100()) < 1) {
																	GameDauDTO record = new GameDauDTO();
																	record.setTableName(MessageFormat.format(dau, param.getProductId()));
																	record.setStartTime(d2hm(innerDateItem, null));
																	record.setEndTime(dateDefaultHM());
																	int dauCount = gameDauMapper.queryDau(record);
																	/* 计算占比(有效点击/日活) */
																	if (dauCount > 0) {
																		param.setRate100(BigDecimal.valueOf(param.getAllowDist()).divide(BigDecimal.valueOf(dauCount), 2, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)));
																	}
																}
															}
														}
														threadResults.remove(searchItem.getKey());
													}
												} catch (Throwable ex) {
													continue;
												}
											} while (searchItems.hasNext());
											/* 验证记录是否已存在 */
											if (null != param) {
												/* 统计日期 */
												param.setSummaryDate(outDateItem);
												int existsFlag = statService.exists(param);
												if (existsFlag <= 0) {
													OaAdvStatDTO advStatResult = statService.persist(param);
													if (null != advStatResult) {
														advStatResult.setClickAd(param.getCount());
														advStatResult.setUniqueClick(param.getDist());
														advStatResult.setSuccessOpen(param.getAllowCount());
														advStatResult.setValidClick(param.getAllowDist());
														advStatResult.setDutyRate(param.getRate100());
														advStatResult.setSummarydate(DateUtil.parseDate(param.getSummaryDate()));
														Object priceNode = getJsonRecord(advStatResult.getPriceNodes(), param.getSummaryDate());
														if (null != priceNode) {
															JSONObject priceJson = JSONUtil.parseObj(priceNode);
															BigDecimal price = priceJson.getBigDecimal(columns.value.toString());

															OaAdvValueSheetDTO valueSheetDTO = new OaAdvValueSheetDTO();
															BeanUtil.copyProperties(advStatResult, valueSheetDTO);
															valueSheetDTO.setPageNumber(1);
															valueSheetDTO.setPageSize(1);
															valueSheetDTO = valueSheetService.queryData(valueSheetDTO);
															if (valueSheetDTO != null && null != valueSheetDTO.getPrice()) {
																if (ObjectUtil.compare(price, valueSheetDTO.getPrice()) != 0) {
																	price = valueSheetDTO.getPrice();
																}
															}
															advStatResult.setPrice(price);
														}
														saveItems.add(advStatResult.getPo());
													}
												}
											}
										}
									} catch (Throwable ex) {
										continue;
									}
								} while (iter.hasNext());
							}
						}
					} catch (Throwable ex) {
						log.error("线程安排失败: {}", ex.getMessage());
					} finally {
						countDownLatch.countDown();
					}
				}
			}));
			t++;
		} while (t < tn);

		try {
			countDownLatch.await();
		} catch (Throwable ex) {
			log.error("线程阻塞异常: {}", ex.getMessage());
		}

		return saveItems;
	}

	protected void dbBatch(CopyOnWriteArrayList<OaAdvStat> saveItems, int tn) {
		if (CollectionUtil.isNotEmpty(saveItems)) {
			int t = 0, keySize = saveItems.size();
			tn = (tn <= 0 ? Constants.threadNumber : tn);
			do {
				/* 为每一位线程分配任务 */
				int startIndex = keySize / tn * t, endIndex = keySize / tn * (t + 1);
				if (t == tn - 1) {
					endIndex = keySize;
				}
				CopyOnWriteArrayList threadResults = new CopyOnWriteArrayList(saveItems.subList(startIndex, endIndex));

				tsExecutor.execute(new Thread(new RunThread() {
					@Override
					public void run2doing() {
						try {
							if (CollectionUtil.isNotEmpty(threadResults)) {
								synchronized (threadResults) {
									statService.saveBatch(threadResults);
									CopyOnWriteArrayList valueSheetResults = bindData(threadResults);
									if (CollectionUtil.isNotEmpty(valueSheetResults)) {
										valueSheetService.saveBatch(valueSheetResults);
									}
								}
							}
						} catch (Throwable ex) {
							log.error("线程安排失败: {}", ex.getMessage());
						} finally {
							System.gc();
						}
					}
				}));
				t++;
			} while (t < tn);
		}
	}

	protected Calendar customDate(int time) {
		Calendar date = Calendar.getInstance();
		date.add(Calendar.DATE, time);
		return date;
	}

	/**
	 * 日记录
	 * 
	 * @return
	 */
	protected Map<String, Long> queryStatRedis(String date) {
		LinkedHashMap<String, Long> advertItems = null;
		Set<?> keys = cacheService.keys(MessageFormat.format(day_key, date));
		log.info("{} 看看这天({})有多少产品受欢迎: {}", DateUtil.now(), date, keys.size());
		if (CollectionUtil.isNotEmpty(keys)) {
			Iterator iter = keys.iterator();
			advertItems = new LinkedHashMap<String, Long>();
			do {
				String key = String.valueOf(iter.next());
				Integer totalNum = 0;
				if (StrUtil.contains(key, columns.count.toString())) {
					Object strResults = cacheService.redisResults(key, redis.redis1str, 0, 0);
					if (strResults != null) {
						totalNum = Integer.valueOf(obj2str(strResults));
					}
					Long countNum = advertItems.get(key);
					advertItems.put(key, (countNum == null ? 0 : countNum) + totalNum);
				}
				if (StrUtil.contains(key, columns.dist.toString())) {
					Object setResults = cacheService.redisResults(key, redis.redis2set, 0, 0);
					if (setResults instanceof LinkedHashSet) {
						totalNum = ((LinkedHashSet) setResults).size();
					}
					Long countNum = advertItems.get(key);
					advertItems.put(key, (countNum == null ? 0 : countNum) + totalNum);
				}
			} while (iter.hasNext());
		}
		log.info("{} Cache Readed Over", DateUtil.now());
		return advertItems;
	}

	protected Map<String, Long> queryStatRedis(String date, int threadNumer) {
		List<?> oriKeys = null;
		/* 多线程任务全部执行完毕才进行下一步操作 */
		Set<?> keys = cacheService.keys(MessageFormat.format(day_key, date));
		ConcurrentHashMap<String, Long> advertItems = new ConcurrentHashMap<String, Long>();
		threadNumer = (threadNumer <= 0) ? Constants.threadNumber : threadNumer;
		CountDownLatch countDownLatch = new CountDownLatch(threadNumer);
		if (CollectionUtil.isNotEmpty(keys)) {
			oriKeys = new ArrayList<>(keys);
			int thread = 0, keySize = oriKeys.size();
			log.info("{} 看看这天({})有多少产品受欢迎: {}", DateUtil.now(), date, keySize);
			do {
				/* 为每一位线程分配任务 */
				int startIndex = keySize / threadNumer * thread, endIndex = keySize / threadNumer * (thread + 1);
				if (thread == threadNumer - 1) {
					endIndex = keySize;
				}
				CopyOnWriteArrayList threadResults = new CopyOnWriteArrayList(oriKeys.subList(startIndex, endIndex));

				tsExecutor.execute(new Thread(new RunThread() {
					@Override
					public void run2doing() {
						try {
							if (CollectionUtil.isNotEmpty(threadResults)) {
								synchronized (threadResults) {
									Iterator<String> keyItes = threadResults.iterator();
									do {
										String key = keyItes.next();
										try {
											Integer totalNum = 0;
											if (StrUtil.contains(key, columns.count.toString())) {
												Object strResults = cacheService.redisResults(key, redis.redis1str, 0, 0);
												if (strResults != null) {
													totalNum = Integer.valueOf(obj2str(strResults));
												}
												Long countNum = advertItems.get(key);
												advertItems.put(key, (countNum == null ? 0 : countNum) + totalNum);
											}
											if (StrUtil.contains(key, columns.dist.toString())) {
												Object setResults = cacheService.redisResults(key, redis.redis2set, 0, 0);
												if (setResults instanceof LinkedHashSet) {
													totalNum = ((LinkedHashSet) setResults).size();
												}
												Long countNum = advertItems.get(key);
												advertItems.put(key, (countNum == null ? 0 : countNum) + totalNum);
											}
										} catch (Throwable ex) {
											continue;
										}
									} while (keyItes.hasNext());
								}
							}
						} catch (Throwable ex) {
							log.error("线程安排失败: {}", ex.getMessage());
						} finally {
							countDownLatch.countDown();
						}
					}
				}));
				thread++;
			} while (thread < threadNumer);

			try {
				countDownLatch.await();
			} catch (Throwable ex) {
				log.error("线程阻塞异常: {}", ex.getMessage());
			}
		}
		log.info("{} Cache Readed Over", DateUtil.now());
		return advertItems;
	}

	protected CopyOnWriteArrayList bindData(CopyOnWriteArrayList<OaAdvStat> records) {
		Map<String, OaAdvValueSheet> results = null;
		if (CollectionUtil.isNotEmpty(records)) {
			results = records.stream().collect(Collectors.groupingBy(os -> os.getProductId() + DateUtil.formatDate(os.getSummarydate()) + os.getAdvAppId(),
					Collectors.collectingAndThen(Collectors.toCollection(CopyOnWriteArrayList::new), items -> {
						OaAdvValueSheet record = new OaAdvValueSheet();
						OaAdvStat item = items.stream().filter(o -> null != o.getProductId() && null != o.getAdvAppId()).findAny().orElse(null);
						if (null != item) {
							BeanUtil.copyProperties(item, record);
							record.setDutyRate(items.stream().map(o -> bigDecimalEmptyFormat(o.getDutyRate())).reduce(BigDecimal.ZERO, BigDecimal::add));
							record.setCost(items.stream().map(o -> bigDecimalEmptyFormat(o.getCost())).reduce(BigDecimal.ZERO, BigDecimal::add));
							record.setValidClick(items.stream().mapToLong(o -> o.getValidClick() == null ? 0 : o.getValidClick()).sum());
							record.setUniqueClick(items.stream().mapToLong(o -> o.getUniqueClick() == null ? 0 : o.getUniqueClick()).sum());
							record.setSuccessOpen(items.stream().mapToLong(o -> o.getSuccessOpen() == null ? 0 : o.getSuccessOpen()).sum());
						}
						return record;
					})));
		}
		if (CollectionUtil.isNotEmpty(results)) {
			return new CopyOnWriteArrayList(results.values());
		}
		return null;
	}

	protected BigDecimal bigDecimalEmptyFormat(BigDecimal record) {
		return null == record ? BigDecimal.valueOf(0) : record;
	}

	protected Object getJsonRecord(String item, String date) {
		JSONArray jsonItems = JSONUtil.parseArray(item);
		if (StrUtil.isNotBlank(date)) {
			Object record = jsonItems.stream().filter(jsonRecord -> dataExists(jsonRecord, date)).findFirst().orElse(null);
			if (null != record) {
				return record;
			}
		}
		return jsonArraySort(jsonItems);
	}

	protected String jsonArraySort(JSONArray jsonItems) {
		CopyOnWriteArrayList<JSONObject> jsonResults = new CopyOnWriteArrayList<>();
		Iterator jsonIter = jsonItems.iterator();
		while (jsonIter.hasNext()) {
			Object jsonItem = jsonIter.next();
			if (jsonItem instanceof JSONObject) {
				jsonResults.add(JSONUtil.parseObj(jsonItem));
			}
		}

		/* sorting */
		Collections.sort(jsonResults, new Comparator<JSONObject>() {
			String beforeValue;
			String afterValue;

			@Override
			public int compare(JSONObject jsonBefore, JSONObject jsonAfter) {
				try {
					beforeValue = jsonBefore.getStr(columns.value.toString()).replaceAll(StrUtil.DASHED, StrUtil.EMPTY);
					afterValue = jsonAfter.getStr(columns.value.toString()).replaceAll(StrUtil.DASHED, StrUtil.EMPTY);
				} catch (JSONException ex) {
					/* 处理异常 */
					ex.printStackTrace();
				}
				/* 这里是按照时间逆序排列,不加负号为正序排列 */
				return -beforeValue.compareTo(afterValue);
			}
		});
		return jsonResults.get(0).toString();
	}

	protected boolean dataExists(Object record, String data) {
		String existsRecord = null;
		if (record instanceof JSONObject) {
			existsRecord = ((JSONObject) record).getStr(columns.value.toString());
		}
		return StrUtil.isNotBlank(existsRecord) ? StrUtil.equalsIgnoreCase(data.trim(), existsRecord.trim()) : false;
	}

	protected String d2hm(String date, String hm) {
		if (StrUtil.isBlank(hm)) {
			hm = Constants.dt.HH_MM;
		}
		return date + hm;
	}

	protected String dateH(String date, String hour) {
		return DateUtil.formatDateTime(dateHFormat(date + StrUtil.SPACE + hour, null, null));
	}

	protected Date dateHFormat(String date, Integer hour, String format) {
		Calendar time = Calendar.getInstance();
		time.setTime(DateUtil.parse(date, StrUtil.isBlank(format) ? Constants.df.YYYY_MM_DD_HH : format));
		time.add(Calendar.HOUR, hour == null ? 0 : hour);
		return time.getTime();
	}

	protected String dateHM(String date, String hour, Integer h) {
		return DateUtil.format(dateHMFormat(dateH(date, hour), h, null), DatePattern.NORM_DATETIME_MINUTE_PATTERN);
	}

	protected Date dateHMFormat(String date, Integer hour, String format) {
		Calendar time = Calendar.getInstance();
		time.setTime(DateUtil.parse(date, StrUtil.isBlank(format) ? DatePattern.NORM_DATETIME_MINUTE_PATTERN : format));
		time.add(Calendar.HOUR, hour == null ? 0 : hour);
		return time.getTime();
	}

	protected String dateDefaultHM() {
		return d2hm(DateUtil.format(DateUtil.date(), DatePattern.NORM_DATE_PATTERN), null);
	}

	protected String obj2str(Object o) {
		String rtn = StrUtil.toString(0);
		return StrUtil.isNotBlank(rtn) ? rtn : "0";
	}

	public void setDate(Date time) {
		if (time == null) {
			this.date = null;
		} else {
			Calendar date = Calendar.getInstance();
			date.setTime(time);
			this.date = date;
		}
	}

	enum columns {
		count, dist, allow, click, value;
	}
}
