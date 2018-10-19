package com.assist4j.dao.mybatis.provider;


import org.apache.ibatis.jdbc.SQL;

import javax.persistence.Id;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;


/**
 * @author yuwei
 */
public class DeleteSqlProvider extends AbstractProvider {

	public <T>String delete(T t) throws IllegalAccessException {
		if (t == null) {
			return null;
		}
		
		Class<?> entityClass = t.getClass();
		String tableName = getTableName(entityClass);

		List<FieldColumn> fcList = getPersistFieldList(entityClass);
		return new SQL() {{
			DELETE_FROM(tableName);
			boolean whereSet = false;

			for (FieldColumn fc: fcList) {
				Field field = fc.getField();
				
				Id idAnn = field.getAnnotation(Id.class);
				if (idAnn != null) {
					WHERE("`" + fc.getColumnName() + "` = #{" + field.getName() + "}");
					whereSet = true;
				}
			}
			if (!whereSet) {
				throw new IllegalAccessException("'where' is missed.");
			}
		}}.toString();
	}
	
	@SuppressWarnings("unchecked")
	public <PK, T>String deleteByKey(Map<String, Object> param) throws IllegalAccessException {
		PK id = (PK) param.get("param1");
		Class<T> entityClass = (Class<T>) param.get("param2");
		String tableName = getTableName(entityClass);

		List<FieldColumn> fcList = getPersistFieldList(entityClass);
		return new SQL() {{
			DELETE_FROM(tableName);
			boolean whereSet = false;

			for (FieldColumn fc: fcList) {
				Field field = fc.getField();
				
				Id idAnn = field.getAnnotation(Id.class);
				if (idAnn != null) {
					WHERE("`" + fc.getColumnName() + "` = " + id);
					whereSet = true;
				}
			}
			if (!whereSet) {
				throw new IllegalAccessException("'where' is missed.");
			}
		}}.toString();
	}
}
