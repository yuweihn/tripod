package com.yuweix.assist4j.data.springboot;


import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.IOException;


/**
 * @author yuwei
 */
@EnableTransactionManagement(proxyTargetClass = true)
public class MybatisConf {
	@ConditionalOnMissingBean(name = "mapperLocations")
	@Bean(name = "mapperLocations")
	public Resource[] mapperLocations(@Value("${assist4j.mybatis.mapper.locationPattern:}") String locationPattern) throws IOException {
		if (locationPattern == null || "".equals(locationPattern)) {
			return new Resource[0];
		}
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resolver.getResources(locationPattern);
		return resources;
	}

	@ConditionalOnMissingBean(name = "basePackage")
	@Bean(name = "basePackage")
	public String basePackage(@Value("${assist4j.mybatis.basePackage:}") String basePackage) {
		return basePackage;
	}

	@ConditionalOnMissingBean(name = "sqlSessionFactory")
	@Bean(name = "sqlSessionFactory")
	public SqlSessionFactoryBean sqlSessionFactoryBean(@Qualifier("dataSource") DataSource dataSource
			, @Qualifier("mapperLocations") Resource[] mapperLocations) {
		SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
		sessionFactoryBean.setDataSource(dataSource);
		if (mapperLocations != null && mapperLocations.length > 0) {
			sessionFactoryBean.setMapperLocations(mapperLocations);
		}
		return sessionFactoryBean;
	}

	@ConditionalOnMissingBean(name = "sqlSessionTemplate")
	@Bean(name = "sqlSessionTemplate")
	public SqlSessionTemplate SqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}

	@ConditionalOnMissingBean(name = "mapperScannerConf")
	@Bean(name = "mapperScannerConf")
	public MapperScannerConfigurer mapperScannerConf(@Qualifier("basePackage") String basePackage) {
		MapperScannerConfigurer conf = new MapperScannerConfigurer();
//		conf.setSqlSessionFactoryBeanName("sqlSessionFactory");
		conf.setSqlSessionTemplateBeanName("sqlSessionTemplate");
		conf.setBasePackage(basePackage);
		return conf;
	}

	@ConditionalOnMissingBean(name = "transactionManager")
	@Bean(name = "transactionManager")
	public DataSourceTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
		transactionManager.setDataSource(dataSource);
		return transactionManager;
	}

	@ConditionalOnMissingBean(name = "transactionTemplate")
	@Bean(name = "transactionTemplate")
	public TransactionTemplate transactionTemplate(@Qualifier("transactionManager") PlatformTransactionManager transactionManager) {
		return new TransactionTemplate(transactionManager);
	}
}
