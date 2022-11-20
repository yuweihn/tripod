package com.yuweix.tripod.dao.sharding;


import java.util.HashMap;
import java.util.Map;


/**
 * @author yuwei
 */
final class Context {
	private static final Map<String, TableConfig> TABLE_CONF_MAP = new HashMap<>();

	static void initTableConf(Map<String, TableConfig> map) {
		TABLE_CONF_MAP.clear();
		if (map != null) {
			TABLE_CONF_MAP.putAll(map);
		}
	}

	static TableConfig getTableConf(String tableName) {
		return TABLE_CONF_MAP.get(tableName);
	}
}