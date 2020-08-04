package com.author.commons.configs;

import java.util.LinkedHashMap;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.author.commons.beans.dbs.RoutingDataSource;
import com.author.commons.utils.enums.Ndb;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;

@Configuration
public class DruidConfig {

	@Bean
	@Primary
	@ConfigurationProperties("spring.datasource.druid.reader")
	public DataSource reader() {
		return DruidDataSourceBuilder.create().build();
	}

	@Bean
	@ConfigurationProperties("spring.datasource.druid.writer")
	public DataSource writer() {
		return DruidDataSourceBuilder.create().build();
	}

	@Bean
	public DataSource myRoutingDataSource(@Qualifier("reader") DataSource reader, @Qualifier("writer") DataSource writer) {
		LinkedHashMap<Object, Object> targetDataSources = new LinkedHashMap<>();
		targetDataSources.put(Ndb.reader.toString(), reader);
		targetDataSources.put(Ndb.writer.toString(), writer);
		RoutingDataSource routingDataSource = new RoutingDataSource();
		routingDataSource.setDefaultTargetDataSource(reader);
		routingDataSource.setTargetDataSources(targetDataSources);
		return routingDataSource;
	}

	@Bean
	public PaginationInterceptor paginationInterceptor() {
		/* paginationInterceptor.setLimit(你的最大单页限制数量，默认 500 条，小于 0 如 -1 不受限制) */
		return new PaginationInterceptor();
	}

	@EnableTransactionManagement
	class MybatisConfig {

		@Resource(name = "myRoutingDataSource")
		private DataSource routingDataSource;

		@Bean
		public SqlSessionFactory sqlSessionFactory() throws Exception {
			SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
			sqlSessionFactoryBean.setDataSource(routingDataSource);
			sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));
			return sqlSessionFactoryBean.getObject();
		}

		@Bean
		public PlatformTransactionManager platformTransactionManager() {
			return new DataSourceTransactionManager(routingDataSource);
		}
	}
}
