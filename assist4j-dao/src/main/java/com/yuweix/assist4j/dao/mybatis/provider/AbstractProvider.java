package com.yuweix.assist4j.dao.mybatis.provider;


import com.yuweix.assist4j.dao.sharding.Sharding;
import com.yuweix.assist4j.dao.sharding.Strategy;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Table;


/**
 * @author yuwei
 */
public abstract class AbstractProvider {
	private static SoftReference<Map<String, String>> TABLE_NAME_REF;
	private static SoftReference<Map<String, String>> SELECT_SQL_REF;
	private static SoftReference<Map<String, String>> SELECT_SQL_WITH_TABLE_ALIAS_REF;
	private static SoftReference<Map<String, List<FieldColumn>>> PERSIST_FIELD_REF;
	private static SoftReference<Map<Class<? extends Strategy>, Strategy>> SHARD_STRATEGY_REF;


	/**
	 * 驼峰转下划线
	 * @param str
	 * @return
	 */
	private static String toUnderline(String str) {
		Pattern pattern = Pattern.compile("[A-Z]");
		Matcher matcher = pattern.matcher(str);
		StringBuffer buf = new StringBuffer(str);
		if (!matcher.find()) {
			return buf.toString();
		}
		buf = new StringBuffer();
		matcher.appendReplacement(buf, "_" + matcher.group(0).toLowerCase());
		matcher.appendTail(buf);
		String res = toUnderline(buf.toString());
		if (res.startsWith("_")) {
			res = res.substring(1);
		}
		return res;
	}

	/**
	 * 根据持久化类获取对应的关系表表名。
	 * @param clz
	 * @return
	 */
	protected String getTableName(Class<?> clz) {
		String className = clz.getName();
		Map<String, String> tableNameMap = null;
		if (TABLE_NAME_REF == null || (tableNameMap = TABLE_NAME_REF.get()) == null) {
			tableNameMap = new ConcurrentHashMap<String, String>();
			TABLE_NAME_REF = new SoftReference<Map<String, String>>(tableNameMap);
		}
		String tableName = tableNameMap.get(className);
		if (tableName == null) {
			Table table = clz.getAnnotation(Table.class);
			if (table != null && !"".equals(table.name().trim())) {
				tableName = table.name().trim();
			} else {
				tableName = toUnderline(clz.getSimpleName());
				if (tableName == null || "".equals(tableName)) {
					throw new RuntimeException("Table name is not found.");
				}
			}
			tableNameMap.put(className, tableName);
		}
		return tableName;
	}

	/**
	 * 根据持久化类获取对应的所有需要持久化的属性SQL。
	 * eg：id as id, user_name as userName, create_time as createTime
	 * @param clz
	 * @return
	 */
	protected String getAllColumnSql(Class<?> clz) {
		String className = clz.getName();
		Map<String, String> selectSqlMap = null;
		if (SELECT_SQL_REF == null || (selectSqlMap = SELECT_SQL_REF.get()) == null) {
			selectSqlMap = new ConcurrentHashMap<String, String>();
			SELECT_SQL_REF = new SoftReference<Map<String, String>>(selectSqlMap);
		}
		String selectSql = selectSqlMap.get(className);
		if (selectSql == null) {
			StringBuilder builder = new StringBuilder("");
			List<FieldColumn> fcList = getPersistFieldList(clz);
			for (int i = 0, size = fcList.size(); i < size; i++) {
				FieldColumn fc = fcList.get(i);
				if (i > 0) {
					builder.append(", ");
				}
				builder.append(fc.getColumnName()).append(" as ").append(fc.getField().getName());
			}
			selectSql = builder.toString();
			selectSqlMap.put(className, selectSql);
		}
		return selectSql;
	}

	/**
	 * eg：a.id as id, a.user_name as userName, a.create_time as createTime
	 * @param clz
	 * @param tableAlias
	 * @return
	 */
	protected String getAllColumnSql(Class<?> clz, String tableAlias) {
		String className = clz.getName();
		Map<String, String> selectSqlMap = null;
		if (SELECT_SQL_WITH_TABLE_ALIAS_REF == null || (selectSqlMap = SELECT_SQL_WITH_TABLE_ALIAS_REF.get()) == null) {
			selectSqlMap = new ConcurrentHashMap<String, String>();
			SELECT_SQL_WITH_TABLE_ALIAS_REF = new SoftReference<Map<String, String>>(selectSqlMap);
		}
		String selectSql = selectSqlMap.get(className);
		if (selectSql == null) {
			StringBuilder builder = new StringBuilder("");
			List<FieldColumn> fcList = getPersistFieldList(clz);
			for (int i = 0, size = fcList.size(); i < size; i++) {
				FieldColumn fc = fcList.get(i);
				if (i > 0) {
					builder.append(", ");
				}
				builder.append(tableAlias).append(".").append(fc.getColumnName()).append(" as ").append(fc.getField().getName());
			}
			selectSql = builder.toString();
			selectSqlMap.put(className, selectSql);
		}
		return selectSql;
	}

	/**
	 * 根据持久化类获取对应的所有需要持久化的属性集合
	 * @param clz
	 * @return
	 */
	protected List<FieldColumn> getPersistFieldList(Class<?> clz) {
		String className = clz.getName();
		Map<String, List<FieldColumn>> persistFieldMap = null;
		if (PERSIST_FIELD_REF == null || (persistFieldMap = PERSIST_FIELD_REF.get()) == null) {
			persistFieldMap = new ConcurrentHashMap<String, List<FieldColumn>>();
			PERSIST_FIELD_REF = new SoftReference<Map<String, List<FieldColumn>>>(persistFieldMap);
		}
		List<FieldColumn> fcList = persistFieldMap.get(className);
		if (fcList == null) {
			fcList = getPersistFieldList0(clz);
			persistFieldMap.put(className, fcList);
		}
		return fcList;
	}

	/**
	 * Gets all fields of the given class and its parents (if any).
	 * @return
	 */
	private static List<FieldColumn> getPersistFieldList0(Class<?> clz) {
		final List<Field> allFields = new ArrayList<Field>();
		Class<?> currentClass = clz;
		while (currentClass != null) {
			allFields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
			currentClass = currentClass.getSuperclass();
		}

		List<FieldColumn> list = new ArrayList<FieldColumn>();
		for (Field field: allFields) {
			Column column = field.getAnnotation(Column.class);
			if (column == null) {
				continue;
			}

			String colName = column.name();
			if ("".equals(colName.trim())) {
				colName = toUnderline(field.getName());
			}
			if (colName == null || "".equals(colName.trim())) {
				continue;
			}
			list.add(new FieldColumn(colName.trim(), field));
		}
		return list;
	}

	public static class FieldColumn {
		private String columnName;
		private Field field;

		public FieldColumn(String columnName, Field field) {
			this.columnName = columnName;
			this.field = field;
		}

		public String getColumnName() {
			return columnName;
		}
		public Field getField() {
			return field;
		}
	}

	protected Object getFieldValue(Field field, Object t) {
		if (!field.isAccessible()) {
			field.setAccessible(true);
		}
		try {
			return field.get(t);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	protected String getShardingIndex(Sharding sharding, String tableName, Object shardingVal) {
		if (sharding == null) {
			return null;
		}
		Class<? extends Strategy> strategyClz = sharding.strategy();

		Map<Class<? extends Strategy>, Strategy> strategyMap = null;
		if (SHARD_STRATEGY_REF == null || (strategyMap = SHARD_STRATEGY_REF.get()) == null) {
			strategyMap = new ConcurrentHashMap<>();
			SHARD_STRATEGY_REF = new SoftReference<>(strategyMap);
		}
		Strategy strategy = strategyMap.get(strategyClz);
		if (strategy == null) {
			try {
				strategy = strategyClz.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			strategyMap.put(strategyClz, strategy);
		}
		return strategy.getShardingIndex(tableName, shardingVal);
	}

	protected <T>String getPhysicalTableName(Class<T> entityClass, Object shardingVal) {
		String tbName = getTableName(entityClass);
		List<FieldColumn> fcList = getPersistFieldList(entityClass);
		for (FieldColumn fc: fcList) {
			Field field = fc.getField();
			Sharding sharding = field.getAnnotation(Sharding.class);
			if (sharding != null) {
				String shardingIndex = getShardingIndex(sharding, tbName, shardingVal);
				return tbName + "_" + shardingIndex;
			}
		}
		return tbName;
	}

	protected <T>String getPhysicalTableName(T t) {
		Class<?> entityClass = t.getClass();
		String tbName = getTableName(entityClass);
		List<FieldColumn> fcList = getPersistFieldList(entityClass);
		for (FieldColumn fc: fcList) {
			Field field = fc.getField();
			Sharding sharding = field.getAnnotation(Sharding.class);
			if (sharding != null) {
				String shardingIndex = getShardingIndex(sharding, tbName, getFieldValue(field, t));
				return tbName + "_" + shardingIndex;
			}
		}
		return tbName;
	}
}
