package com.yuweix.tripod.dao.hibernate;


import org.hibernate.resource.jdbc.spi.StatementInspector;


/**
 * 动态修改表名拦截器
 * @author yuwei
 */
public class DynamicTableInspector implements StatementInspector {
	public DynamicTableInspector() {

	}

	@Override
	public String inspect(String sql) {
		final String srcName = DynamicTableTL.getSrcName();
		final String destName = DynamicTableTL.getDestName();
		if (srcName == null || destName == null) {
			return sql;
		}
		return sql.replaceAll(srcName, destName);
	}
}
