package com.yuweix.tripod.dao.sharding;


/**
 * @author yuwei
 **/
public interface TableSetting {
	/**
	 * 逻辑表后占位符长度
	 * eg.
	 * user  ====>>>>  user_0000
	 * @return   逻辑表后占位符长度
	 */
	int getSuffixLength();

	/**
	 * 分库数量
	 */
	int getDatabaseSize();

	/**
	 * 分表数量
	 */
	int getTableSize();
}