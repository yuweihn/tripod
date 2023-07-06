package com.yuweix.tripod.sequence.springboot;


import com.yuweix.tripod.sequence.base.BeanMap;
import com.yuweix.tripod.sequence.base.DefaultSequence;
import com.yuweix.tripod.sequence.base.SequenceBeanFactory;
import com.yuweix.tripod.sequence.base.SequenceBeanHolder;
import com.yuweix.tripod.sequence.dao.SegmentSequenceDao;
import com.yuweix.tripod.sequence.dao.SequenceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


/**
 * @author yuwei
 */
public class SequenceConf {
	@ConditionalOnMissingBean(SequenceDao.class)
	@Bean(name = "sequenceDao")
	public SequenceDao sequenceDao(@Autowired DataSource dataSource
			, @Value("${tripod.sequence.setting.inner-step:100}") int innerStep
			, @Value("${tripod.sequence.setting.retry-times:5}") int retryTimes
			, @Value("${tripod.sequence.setting.segment-count:1}") int segmentCount
			, @Value("${tripod.sequence.setting.max-skip-count:5}") int maxSkipCount
			, @Value("${tripod.sequence.setting.max-wait-millis:5000}") long maxWaitMillis
			, @Value("${tripod.sequence.setting.rule-class-name:}") String ruleClassName
			, @Value("${tripod.sequence.setting.table-name:sequence}") String tableName) {
		SegmentSequenceDao sequenceDao = new SegmentSequenceDao();
		sequenceDao.setDataSource(dataSource);
		sequenceDao.setInnerStep(innerStep);
		sequenceDao.setRetryTimes(retryTimes);
		sequenceDao.setSegmentCount(segmentCount);
		sequenceDao.setMaxSkipCount(maxSkipCount);
		sequenceDao.setMaxWaitMillis(maxWaitMillis);
		sequenceDao.setRuleClassName(ruleClassName);
		sequenceDao.setTableName(tableName);
		return sequenceDao;
	}

	@ConditionalOnMissingBean(SequenceBeanHolder.class)
	@Bean
	@ConfigurationProperties(prefix = "tripod.sequence", ignoreUnknownFields = true)
	public SequenceBeanHolder sequenceBeanHolder() {
		return new SequenceBeanHolder() {
			private Map<String, String> map = new HashMap<>();
			@Override
			public Map<String, String> getBeans() {
				return map;
			}
		};
	}

	@ConditionalOnMissingBean(BeanMap.class)
	@Bean
	public BeanMap beanMap(SequenceBeanHolder holder) {
		BeanMap map = new BeanMap();
		Map<String, String> beans = holder.getBeans();
		if (beans != null && !beans.isEmpty()) {
			map.putAll(beans);
		}
		return map;
	}

	@ConditionalOnMissingBean(SequenceBeanFactory.class)
	@Bean(name = "sequenceBeanFactory")
	public SequenceBeanFactory sequenceBeanFactory(Environment env) {
		String clzName = env.getProperty("tripod.sequence.class-name");
		if (clzName != null && !"".equals(clzName)) {
			return new SequenceBeanFactory(clzName);
		} else {
			return new SequenceBeanFactory(DefaultSequence.class);
		}
	}
}
