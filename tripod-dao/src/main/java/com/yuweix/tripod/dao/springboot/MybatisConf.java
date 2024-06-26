package com.yuweix.tripod.dao.springboot;


import com.alibaba.druid.pool.DruidDataSource;
import com.yuweix.tripod.dao.datasource.DynamicDataSource;
import com.yuweix.tripod.dao.datasource.DynamicDataSourceAspect;
import com.yuweix.tripod.dao.mybatis.SQLInterceptor;
import com.yuweix.tripod.sharding.aspect.DataSourceAspect;
import com.yuweix.tripod.sharding.context.ShardingContext;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/**
 * @author yuwei
 */
@EnableTransactionManagement(proxyTargetClass = true)
public class MybatisConf {
	@ConditionalOnProperty(name = "tripod.datasource.default.enabled", matchIfMissing = true)
	@ConditionalOnMissingBean(name = "dataSource")
	@Bean(name = "dataSource", initMethod = "init", destroyMethod = "close")
	public DataSource defaultDataSource(@Value("${tripod.datasource.default.driver-class}") String driverClassName
			, @Value("${tripod.datasource.default.url}") String url
			, @Value("${tripod.datasource.default.user-name}") String userName
			, @Value("${tripod.datasource.default.password}") String password
			, @Value("${tripod.datasource.default.default-read-only:false}") boolean defaultReadOnly
			, @Value("${tripod.datasource.default.filters:stat}") String filters
			, @Value("${tripod.datasource.default.max-active:2}") int maxActive
			, @Value("${tripod.datasource.default.initial-size:1}") int initialSize
			, @Value("${tripod.datasource.default.max-wait-mills:60000}") long maxWaitMillis
			, @Value("${tripod.datasource.default.remove-abandoned:false}") boolean removeAbandoned
			, @Value("${tripod.datasource.default.remove-abandoned-timeout:1800}") int removeAbandonedTimeout
			, @Value("${tripod.datasource.default.min-idle:1}") int minIdle
			, @Value("${tripod.datasource.default.time-between-eviction-runs-millis:60000}") long timeBetweenEvictionRunsMillis
			, @Value("${tripod.datasource.default.min-evictable-idle-time-millis:300000}") long minEvictableIdleTimeMillis
			, @Value("${tripod.datasource.default.validation-query:select 'x'}") String validationQuery
			, @Value("${tripod.datasource.default.test-while-idle:true}") boolean testWhileIdle
			, @Value("${tripod.datasource.default.test-on-borrow:false}") boolean testOnBorrow
			, @Value("${tripod.datasource.default.test-on-return:false}") boolean testOnReturn
			, @Value("${tripod.datasource.default.pool-prepared-statements:true}") boolean poolPreparedStatements
			, @Value("${tripod.datasource.default.max-pool-prepared-statement-per-connection-size:50}") int maxPoolPreparedStatementPerConnectionSize
			, @Value("${tripod.datasource.default.max-open-prepared-statements:100}") int maxOpenPreparedStatements) throws SQLException {
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.setDriverClassName(driverClassName);
		dataSource.setUrl(url);
		dataSource.setUsername(userName);
		dataSource.setPassword(password);
		dataSource.setDefaultReadOnly(defaultReadOnly);
		dataSource.setFilters(filters);
		dataSource.setMaxActive(maxActive);
		dataSource.setInitialSize(initialSize);
		dataSource.setMaxWait(maxWaitMillis);
		dataSource.setRemoveAbandoned(removeAbandoned);
		dataSource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
		dataSource.setMinIdle(minIdle);
		dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		dataSource.setValidationQuery(validationQuery);
		dataSource.setTestWhileIdle(testWhileIdle);
		dataSource.setTestOnBorrow(testOnBorrow);
		dataSource.setTestOnReturn(testOnReturn);
		dataSource.setPoolPreparedStatements(poolPreparedStatements);
		dataSource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
		dataSource.setMaxOpenPreparedStatements(maxOpenPreparedStatements);
		return dataSource;
	}

	@ConditionalOnMissingBean(name = "dataSources")
	@Bean(name = "dataSources")
	public Map<String, DataSource> dataSources() {
		return null;
	}

	@ConditionalOnMissingBean(DataSourceAspect.class)
	@Bean(name = "dynamicDataSourceAspect")
	public DynamicDataSourceAspect dynamicDataSourceAspect() {
		return new DynamicDataSourceAspect();
	}

	@Primary
	@ConditionalOnMissingBean(name = "dynamicDataSource")
	@Bean(name = "dynamicDataSource")
	public DataSource dynamicDataSource(@Autowired(required = false) @Qualifier("dataSource") DataSource defaultDataSource
			, @Value("${tripod.datasource.default.lenient:false}") boolean lenient
			, @Qualifier("dataSources") Map<String, DataSource> dataSources) {
		if (dataSources == null) {
			dataSources = new HashMap<>();
		}

		DynamicDataSource dds = new DynamicDataSource();
		dds.setLenientFallback(lenient);
		dds.setDefaultTargetDataSource(defaultDataSource);
		dds.setTargetDataSources(new HashMap<>(dataSources));
		return dds;
	}

	@ConditionalOnMissingBean(name = "mapperLocations")
	@Bean(name = "mapperLocations")
	public Resource[] mapperLocations(@Value("${tripod.mybatis.mapper.location-pattern:}") String locationPattern) throws IOException {
		if (locationPattern == null || "".equals(locationPattern)) {
			return new Resource[0];
		}
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		return resolver.getResources(locationPattern);
	}

	@ConditionalOnMissingBean(name = "basePackage")
	@Bean(name = "basePackage")
	public String basePackage(Environment env) {
		return env.getProperty("tripod.mybatis.base-package");
	}

	@ConditionalOnMissingBean(name = "sqlInterceptor")
	@Bean(name = "sqlInterceptor")
	public Interceptor sqlInterceptor() {
		return new SQLInterceptor();
	}

	@ConditionalOnMissingBean(name = "pluginInterceptors")
	@Bean(name = "pluginInterceptors")
	public Interceptor[] pluginInterceptors(@Qualifier("sqlInterceptor") Interceptor sqlInterceptor) {
		return new Interceptor[]{sqlInterceptor};
	}

	@ConditionalOnMissingBean(SqlSessionFactory.class)
	@Bean(name = "sqlSessionFactory")
	public SqlSessionFactoryBean sqlSessionFactoryBean(@Autowired DataSource dataSource
			, @Autowired(required = false) ShardingContext shardingContext
			, @Qualifier("mapperLocations") Resource[] mapperLocations
			, @Qualifier("pluginInterceptors") Interceptor[] pluginInterceptors) {
		SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
		sessionFactoryBean.setDataSource(dataSource);
		sessionFactoryBean.setPlugins(pluginInterceptors);
		if (mapperLocations != null && mapperLocations.length > 0) {
			sessionFactoryBean.setMapperLocations(mapperLocations);
		}
		return sessionFactoryBean;
	}

	@ConditionalOnMissingBean(SqlSessionTemplate.class)
	@Bean(name = "sqlSessionTemplate")
	public SqlSessionTemplate SqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}

	@ConditionalOnMissingBean(MapperScannerConfigurer.class)
	@Bean(name = "mapperScannerConf")
	public MapperScannerConfigurer mapperScannerConf(@Qualifier("basePackage") String basePackage) {
		MapperScannerConfigurer conf = new MapperScannerConfigurer();
//		conf.setSqlSessionFactoryBeanName("sqlSessionFactory");
		conf.setSqlSessionTemplateBeanName("sqlSessionTemplate");
		conf.setBasePackage(basePackage);
		return conf;
	}

	@ConditionalOnMissingBean(TransactionManager.class)
	@Bean(name = "transactionManager")
	public DataSourceTransactionManager transactionManager(@Autowired DataSource dataSource) {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
		transactionManager.setDataSource(dataSource);
		return transactionManager;
	}

	@ConditionalOnMissingBean(TransactionTemplate.class)
	@Bean(name = "transactionTemplate")
	public TransactionTemplate transactionTemplate(@Qualifier("transactionManager") PlatformTransactionManager transactionManager) {
		return new TransactionTemplate(transactionManager);
	}
}
