package com.yuweix.tripod.dao.mybatis;


import com.yuweix.tripod.dao.mybatis.order.OrderBy;
import com.yuweix.tripod.dao.mybatis.where.Criteria;
import com.yuweix.tripod.sharding.Shardable;

import java.io.Serializable;
import java.util.List;


/**
 * @author yuwei
 */
public interface Dao<T extends Serializable, PK extends Serializable> extends Shardable {
	T get(PK id);
	T get(Object shardingVal, PK id);
	
	int findCount(Criteria criteria);
	List<T> findList(Criteria criteria, OrderBy orderBy);
	List<T> findPageList(Criteria criteria, int pageNo, int pageSize, OrderBy orderBy);
	
	int insert(T t);
	int insertSelective(T t);
	int updateByPrimaryKey(T t);
	int updateByPrimaryKeyExcludeVersion(T t);
	int updateByPrimaryKeySelective(T t);
	int updateByPrimaryKeySelectiveExcludeVersion(T t);
	int delete(T t);
	int deleteByKey(PK id);
	int deleteByKey(Object shardingVal, PK id);
}
