package com.yuweix.assist4j.boot.redis;


import com.yuweix.assist4j.data.springboot.lettuce.LettuceClusterConf;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "assist4j.boot.lettuce.cluster.enabled")
@Import({LettuceClusterConf.class})
public class LettuceClusterAutoConfiguration {

}
