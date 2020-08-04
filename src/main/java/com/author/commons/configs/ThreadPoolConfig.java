package com.author.commons.configs;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@SuppressWarnings("all")
public class ThreadPoolConfig {
	@Bean(name = "tsExecutor")
	public Executor threadTsExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
  taskExecutor.setCorePoolSize(10);
  taskExecutor.setMaxPoolSize(50);
  taskExecutor.setQueueCapacity(500);
  taskExecutor.setKeepAliveSeconds(60);
  taskExecutor.setThreadNamePrefix("ts-executor:");
  taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
  taskExecutor.setAwaitTerminationSeconds(60);
  return taskExecutor;
	}
}
