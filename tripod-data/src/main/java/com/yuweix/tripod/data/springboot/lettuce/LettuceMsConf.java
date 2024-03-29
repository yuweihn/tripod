package com.yuweix.tripod.data.springboot.lettuce;


import com.yuweix.tripod.core.json.Json;
import com.yuweix.tripod.data.cache.redis.lettuce.LettuceCache;
import com.yuweix.tripod.data.serializer.JsonSerializer;
import com.yuweix.tripod.data.serializer.Serializer;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;


/**
 * 一主多从redis
 * @author yuwei
 */
public class LettuceMsConf {
	@Bean(name = "lettuceClientConfiguration")
	public LettuceClientConfiguration clientConfiguration(@Value("${tripod.redis.pool.max-total:20}") int maxTotal
			, @Value("${tripod.redis.pool.max-idle:10}") int maxIdle
			, @Value("${tripod.redis.pool.min-idle:10}") int minIdle
			, @Value("${tripod.redis.pool.max-wait-millis:10000}") long maxWaitMillis
			, @Value("${tripod.redis.pool.time-between-eviction-runs-millis:-1}") long timeBetweenEvictionRunsMillis
			, @Value("${tripod.redis.pool.test-on-borrow:false}") boolean testOnBorrow
			, @Value("${tripod.redis.timeout-millis:5000}") long timeoutMillis) {
		GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		poolConfig.setMaxTotal(maxTotal);
		poolConfig.setMaxIdle(maxIdle);
		poolConfig.setMinIdle(minIdle);
		poolConfig.setMaxWaitMillis(maxWaitMillis);
		poolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		poolConfig.setTestOnBorrow(testOnBorrow);
		LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
				.commandTimeout(Duration.ofMillis(timeoutMillis))
				.poolConfig(poolConfig)
				.build();
		return clientConfig;
	}

	@Bean(name = "redisSentinelConfiguration")
	public RedisSentinelConfiguration redisSentinelConfiguration(@Value("${tripod.redis.master.name}") String masterName
			, @Value("${tripod.redis.sentinel.ip}") String host
			, @Value("${tripod.redis.sentinel.port}") int port
			, @Value("${tripod.redis.db-index:0}") int dbIndex
			, @Value("${tripod.redis.need-password:false}") boolean needPassword
			, @Value("${tripod.redis.password:}") String password) {
		RedisSentinelConfiguration conf = new RedisSentinelConfiguration();
		RedisNode redisNode = new RedisNode.RedisNodeBuilder().withName(masterName).build();
		conf.setMaster(redisNode);
		conf.setDatabase(dbIndex);
		if (needPassword) {
			conf.setPassword(RedisPassword.of(password));
		}

		Set<RedisNode> sentinels = new HashSet<>();
		sentinels.add(new RedisNode(host, port));
		conf.setSentinels(sentinels);
		return conf;
	}

	@Bean(name = "lettuceConnectionFactory")
	public LettuceConnectionFactory lettuceConnectionFactory(@Qualifier("lettuceClientConfiguration") LettuceClientConfiguration clientConfig
			, @Qualifier("redisSentinelConfiguration") RedisSentinelConfiguration config) {
		LettuceConnectionFactory connFactory = new LettuceConnectionFactory(config, clientConfig);
		connFactory.setValidateConnection(false);
		connFactory.setShareNativeConnection(true);
		return connFactory;
	}

	@Bean(name = "redisTemplate")
	public RedisTemplate<String, Object> redisTemplate(@Qualifier("lettuceConnectionFactory") LettuceConnectionFactory connFactory) {
		RedisSerializer<?> redisSerializer = new StringRedisSerializer();
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connFactory);
		template.setKeySerializer(redisSerializer);
		template.setValueSerializer(redisSerializer);
		template.setEnableDefaultSerializer(true);
//		template.setEnableTransactionSupport(true);
		return template;
	}

	@ConditionalOnMissingBean(RedisMessageListenerContainer.class)
	@Bean
	public RedisMessageListenerContainer messageContainer(@Qualifier("lettuceConnectionFactory") LettuceConnectionFactory connFactory) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connFactory);
		return container;
	}

	@ConditionalOnMissingBean(Serializer.class)
	@Bean
	public Serializer cacheSerializer(Json json) {
		return new JsonSerializer(json);
	}

	@ConditionalOnMissingBean(name = "redisCache")
	@Bean(name = "redisCache")
	public LettuceCache redisCache(@Qualifier("redisTemplate") RedisTemplate<String, Object> template
			, Serializer serializer
			, RedisMessageListenerContainer messageContainer) {
		LettuceCache cache = new LettuceCache(template, serializer);
		cache.setMessageContainer(messageContainer);
		return cache;
	}
}
