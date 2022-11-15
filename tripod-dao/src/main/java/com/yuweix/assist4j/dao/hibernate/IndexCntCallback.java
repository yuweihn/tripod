package com.yuweix.assist4j.dao.hibernate;


import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;


/**
 * @author yuwei
 */
public class IndexCntCallback extends IndexParamCallback {
	protected String sql;
	protected Object[] params;

	public IndexCntCallback(String sql, Object[] params) {
		this.sql = sql;
		this.params = params;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object doInHibernate(Session session) throws HibernateException {
		NativeQuery<Object> query = session.createNativeQuery(sql);
		assembleParams(query, params);
		return new Integer(query.uniqueResult().toString());
	}
}
