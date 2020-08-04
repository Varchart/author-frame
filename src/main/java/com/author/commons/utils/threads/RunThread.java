package com.author.commons.utils.threads;

/**
 * @Description: TODO
 * @Author: YNa
 * @Date: 2020/7/4 16:22
 * @Version: #1.0 Copyright © 2020
 */
public abstract class RunThread implements Runnable {
	public RunThread() {
	}

	public void run() {
		this.run2doing();
	}

	public void run2doing() {
	}
}
