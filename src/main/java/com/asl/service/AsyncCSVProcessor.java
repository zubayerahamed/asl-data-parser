package com.asl.service;

import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Zubayer Ahamed
 * @since Dec 19, 2020
 */
@Slf4j
@Service
@EnableAsync
public class AsyncCSVProcessor {

	@Bean(name = "taskExecutor")
	public Executor taskExecutor() {
		log.debug("Creating Async Task Executor");
		final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(2);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("CarThread-");
		executor.initialize();
		return executor;
	}

	@Async
	public void processDataFromCSV() {
		int i = 0;
		while(i < 10) {
			System.out.println("Hi there at - " + new Date());
			System.out.println(Thread.currentThread().getName() + " - " + Thread.currentThread().getId());
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i++;
		}
		
	}
}
