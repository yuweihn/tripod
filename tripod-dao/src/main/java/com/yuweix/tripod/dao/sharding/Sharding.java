package com.yuweix.tripod.dao.sharding;


import java.lang.annotation.Target;
import java.lang.annotation.Retention;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * @author yuwei
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface Sharding {
    /**
     * 分片策略
     */
    Class<? extends Strategy> strategy() default DefaultStrategy.class;
}
