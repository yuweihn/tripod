package com.assist4j.data.cache;


import com.alibaba.fastjson.TypeReference;


/**
 * @author yuwei
 */
public interface Cache {
	/**
	 * @param key
	 * @return
	 */
	boolean contains(String key);

	/**
	 * @param key
	 * @param value
	 * @param timeout 过期时间(s)。
	 * @return
	 */
	<T>boolean put(String key, T value, long timeout);

	String get(String key);
	/**
	 * @param key
	 * @param clz
	 * @param <T>
	 * @return
	 */
	<T>T get(String key, Class<T> clz);
	<T>T get(String key, TypeReference<T> type);

	/**
	 * @param key
	 */
	void remove(String key);
}
