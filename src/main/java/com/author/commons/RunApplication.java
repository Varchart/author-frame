package com.author.commons;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Description: TODO
 * @Author: YNa
 * @Date: 2020/7/21 15:32
 * @Version: #1.0 Copyright ? 2020
 */
@SpringBootApplication
@EnableScheduling
public class RunApplication {
  public static void main(String[] args) {
    SpringApplication.run(RunApplication.class, args);
  }
}
