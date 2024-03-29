package com.yuweix.tripod.dao.sharding;



/**
 * 分片策略
 * @author yuwei
 */
public interface Strategy {
    default TableConfig getTableConf(String tableName) {
        return ShardingContext.getInstance().getTableConf(tableName);
    }

    /**
     * @param shardingVal                分片字段的值
     * @return   返回分片。如：0000,0001等等
     */
    <T>String getShardingIndex(String tableName, T shardingVal);
}
