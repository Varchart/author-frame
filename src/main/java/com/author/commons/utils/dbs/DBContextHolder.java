package com.author.commons.utils.dbs;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

/**
 * 读写分离转换器
 * 
 * @author YNa
 *
 */
@Slf4j
public class DBContextHolder {
	private DBContextHolder() {
	}

	private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();
	private static final AtomicInteger counter = new AtomicInteger(-1);

	public static void set(String noder) {
		contextHolder.set(noder);
	}

	public static String getDBSource() {
		return contextHolder.get();
	}

	public static void setDBSource(String noder) {
		synchronized (contextHolder) {
			/* 轮询 */
			int index = counter.getAndIncrement() % 2;
			if (counter.get() > 9999) {
				counter.set(-1);
			}
			if (index == 0) {
				contextHolder.set(noder);
				log.info("当前 DataSource: {}", noder);
			}
		}
	}

	public static void clearDataSource() {
		synchronized (contextHolder) {
			contextHolder.remove();
		}
	}
}
