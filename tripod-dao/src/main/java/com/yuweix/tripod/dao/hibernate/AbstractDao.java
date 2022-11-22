package com.yuweix.tripod.dao.hibernate;


import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.yuweix.tripod.dao.PersistContext;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaQuery;


/**
 * @author yuwei
 */
public abstract class AbstractDao<T extends Serializable, PK extends Serializable> extends PersistContext implements Dao<T, PK> {
	private Class<T> clz;
	private SessionFactory sessionFactory;

	@Resource
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	protected Session getSession() {
		return sessionFactory.getCurrentSession();
	}


	@SuppressWarnings("unchecked")
	public AbstractDao() {
		this.clz = null;
		Type t = getClass().getGenericSuperclass();
		if (t instanceof ParameterizedType) {
			this.clz = (Class<T>) ((ParameterizedType) t).getActualTypeArguments()[0];
		}
	}

	@Override
	public List<T> getAll() {
		Session session = getSession();
		CriteriaQuery<T> criteriaQuery = session.getCriteriaBuilder().createQuery(clz);
		Query<T> query = session.createQuery(criteriaQuery);
		return query.list();
	}

	@Override
	public T get(final PK id) {
		return getSession().get(clz, id);
	}

	@Override
	public void save(final T entity) {
		getSession().save(entity);
	}

	@Override
	public void saveOrUpdateAll(final Collection<T> entities) {
		getSession().saveOrUpdate(entities);
	}

	@Override
	public void update(final T entity) {
		getSession().update(entity);
	}

	@Override
	public void saveOrUpdate(final T entity) {
		getSession().saveOrUpdate(entity);
	}

	@Override
	public T merge(final T entity) {
		getSession().merge(entity);
		return entity;
	}

	@Override
	public void deleteByKey(PK id) {
		final T entity = get(id);
		if (entity != null) {
			getSession().delete(entity);
		}
	}





	/**
	 * 查询某个表的所有字段
	 * @param sql
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected List<T> query(String sql, Map<String, Object> params) {
		return (List<T>) new MapCallback(sql, clz, params).doInHibernate(getSession());
	}
	/**
	 * 查询某个表的所有字段
	 * @param sql
	 * @return
	 */
	protected List<T> query(String sql) {
		return query(sql, (Object[]) null);
	}
	/**
	 * 查询某个表的所有字段
	 * @param sql
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected List<T> query(String sql, Object[] params) {
		return (List<T>) new IndexCallback(sql, clz, params).doInHibernate(getSession());
	}


	/**
	 * 查询某个表的所有字段。分页查询
	 * @param sql
	 * @param pageNo
	 * @param pageSize
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected List<T> query(String sql, int pageNo, int pageSize, Map<String, Object> params) {
		return (List<T>) new MapCallback(sql, clz, pageNo, pageSize, params).doInHibernate(getSession());
	}
	/**
	 * 查询某个表的所有字段。分页查询
	 * @param sql
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	protected List<T> query(String sql, int pageNo, int pageSize) {
		return query(sql, pageNo, pageSize, (Object[]) null);
	}
	/**
	 * 查询某个表的所有字段。分页查询
	 * @param sql
	 * @param pageNo
	 * @param pageSize
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected List<T> query(String sql, int pageNo, int pageSize, Object[] params) {
		return (List<T>) new IndexCallback(sql, clz, pageNo, pageSize, params).doInHibernate(getSession());
	}


	/**
	 * 查询记录的条数
	 * @param sql
	 * @param params
	 * @return
	 */
	protected int queryCount(String sql, Map<String, Object> params) {
		return (Integer) new MapCntCallback(sql, params).doInHibernate(getSession());
	}
	/**
	 * 查询记录的条数
	 * @param sql
	 * @return
	 */
	protected int queryCount(String sql) {
		return queryCount(sql, (Object[]) null);
	}
	/**
	 * 查询记录的条数
	 * @param sql
	 * @param params
	 * @return
	 */
	protected int queryCount(String sql, Object[] params) {
		return (Integer) new IndexCntCallback(sql, params).doInHibernate(getSession());
	}


	/**
	 * 查询某个表的所有字段。只取单条记录
	 * @param sql
	 * @param params
	 * @return
	 */
	protected T queryForObject(String sql, Map<String, Object> params) {
		List<T> list = query(sql, params);
		return list == null || list.size() <= 0 ? null : list.get(0);
	}

	@SuppressWarnings("unchecked")
	protected List<Map<String, Object>> queryForMapList(String sql, int pageNo, int pageSize, Map<String, Object> params) {
		return (List<Map<String, Object>>) new MapCallback(sql, Map.class, pageNo, pageSize, params).doInHibernate(getSession());
	}
	@SuppressWarnings("unchecked")
	protected List<Map<String, Object>> queryForMapList(String sql, Map<String, Object> params) {
		return (List<Map<String, Object>>) new MapCallback(sql, Map.class, params).doInHibernate(getSession());
	}

	/**
	 * 查询某个表的所有字段。只取单条记录
	 * @param sql
	 * @param params
	 * @return
	 */
	protected T queryForObject(String sql, Object[] params) {
		List<T> list = query(sql, params);
		return list == null || list.size() <= 0 ? null : list.get(0);
	}
	/**
	 * 查询某个表的所有字段。只取单条记录
	 * @param sql
	 * @return
	 */
	protected T queryForObject(String sql) {
		return queryForObject(sql, (Object[]) null);
	}

	protected List<Map<String, Object>> queryForMapList(String sql, int pageNo, int pageSize) {
		return queryForMapList(sql, pageNo, pageSize, (Object[]) null);
	}
	@SuppressWarnings("unchecked")
	protected List<Map<String, Object>> queryForMapList(String sql, int pageNo, int pageSize, Object[] params) {
		return (List<Map<String, Object>>) new IndexCallback(sql, Map.class, pageNo, pageSize, params).doInHibernate(getSession());
	}
	@SuppressWarnings("unchecked")
	protected List<Map<String, Object>> queryForMapList(String sql, Object[] params) {
		return (List<Map<String, Object>>) new IndexCallback(sql, Map.class, params).doInHibernate(getSession());
	}

	/**
	 * 执行增删改操作
	 * @param sql
	 * @param params
	 * @return
	 */
	protected int execute(String sql, Map<String, Object> params) {
		return (Integer) new MapModifyCallback(sql, params).doInHibernate(getSession());
	}
	/**
	 * 执行增删改操作
	 * @param sql
	 * @param params
	 * @return
	 */
	protected int execute(String sql, Object[] params) {
		return (Integer) new IndexModifyCallback(sql, params).doInHibernate(getSession());
	}
	/**
	 * 执行增删改操作
	 * @param sql
	 * @return
	 */
	protected int execute(String sql) {
		return execute(sql, (Object[]) null);
	}
	
	public void delete(final T entity) {
		getSession().delete(entity);
	}
}
