package com.yuweix.assist4j.data.springboot.lettuce;


import com.yuweix.assist4j.data.cache.redis.lettuce.LettuceCache;
import com.yuweix.assist4j.data.serializer.JsonSerializer;
import com.yuweix.assist4j.data.serializer.Serializer;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;


/**
 * 单实例redis
 * @author yuwei
 */
public class LettuceConf {
	@Bean(name = "lettuceClientConfiguration")
	public LettuceClientConfiguration clientConfiguration(@Value("${redis.pool.maxTotal:1024}") int maxTotal
			, @Value("${redis.pool.maxIdle:100}") int maxIdle
			, @Value("${redis.pool.minIdle:100}") int minIdle
			, @Value("${redis.pool.maxWaitMillis:10000}") long maxWaitMillis
			, @Value("${redis.pool.testOnBorrow:false}") boolean testOnBorrow
			, @Value("${redis.timeoutMillis:5000}") long timeoutMillis) {
		GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		poolConfig.setMaxTotal(maxTotal);
		poolConfig.setMaxIdle(maxIdle);
		poolConfig.setMinIdle(minIdle);
		poolConfig.setMaxWaitMillis(maxWaitMillis);
		poolConfig.setTestOnBorrow(testOnBorrow);
		
		LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
				.commandTimeout(Duration.ofMillis(timeoutMillis))
				.poolConfig(poolConfig)
				.build();
		
		return clientConfig;
	}

	@Bean(name = "redisStandaloneConfiguration")
	public RedisStandaloneConfiguration redisStandaloneConfiguration(@Value("${redis.host:}") String host
			, @Value("${redis.port:0}") int port
			, @Value("${redis.dbIndex:0}") int dbIndex
			, @Value("${redis.needPassword:false}") boolean needPassword
			, @Value("${redis.password:}") String password) {
		RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration();
		conf.setHostName(host);
		if (port > 0) {
			conf.setPort(port);
		}
		if (dbIndex > 0) {
			conf.setDatabase(dbIndex);
		}
		if (needPassword) {
			conf.setPassword(RedisPassword.of(password));
		}
		return conf;
	}

	@Primary
	@ConditionalOnMissingBean(name = "lettuceConnectionFactory")
	@Bean(name = "lettuceConnectionFactory")
	public LettuceConnectionFactory lettuceConnectionFactory(@Qualifier("lettuceClientConfiguration") LettuceClientConfiguration clientConfig
			, @Qualifier("redisStandaloneConfiguration") RedisStandaloneConfiguration config) {
		LettuceConnectionFactory connFactory = new LettuceConnectionFactory(config, clientConfig);
		connFactory.setValidateConnection(true);
		connFactory.setShareNativeConnection(false);
		return connFactory;
	}

	@Bean(name = "redisTemplate")
	public RedisTemplate<String, Object> redisTemplate(@Qualifier("lettuceConnectionFactory") LettuceConnectionFactory connFactory) {
		RedisSerializer<?> redisSerializer = new StringRedisSerializer();
		RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
		template.setConnectionFactory(connFactory);
		template.setKeySerializer(redisSerializer);
		template.setValueSerializer(redisSerializer);
		template.setEnableDefaultSerializer(true);
//		template.setEnableTransactionSupport(true);
		return template;
	}

	@ConditionalOnMissingBean(Serializer.class)
	@Bean
	public Serializer cacheSerializer() {
		return new JsonSerializer();
	}

	@ConditionalOnMissingBean(name = "redisCache")
	@Bean(name = "redisCache")
	public LettuceCache redisCache(@Qualifier("redisTemplate") RedisTemplate<String, Object> template
			, Serializer serializer) {
		LettuceCache cache = new LettuceCache();
		cache.setRedisTemplate(template);
		cache.setSerializer(serializer);
		return cache;
	}
}
