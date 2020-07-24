package com.author.commons.configs;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;

@EnableTransactionManagement
@Configuration
public class DruidConfig {

  @Primary
  @Bean(name = "data1Source")
  @ConfigurationProperties("spring.datasource.druid.one")
  public DataSource dataSourceOne() {
    return DruidDataSourceBuilder.create().build();
  }

  @Bean(name = "data2Source")
  @ConfigurationProperties("spring.datasource.druid.two")
  public DataSource dataSourceTwo() {
    return DruidDataSourceBuilder.create().build();
  }

  @Bean
  public PaginationInterceptor paginationInterceptor() {
    /* paginationInterceptor.setLimit(你的最大单页限制数量，默认 500 条，小于 0 如 -1 不受限制) */
    return new PaginationInterceptor();
  }
}
