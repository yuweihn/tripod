package com.yuweix.assist4j.sequence.dao;


import com.yuweix.assist4j.sequence.bean.SequenceHolder;


/**
 * @author yuwei
 */
public interface SequenceDao {
	void ensure(String seqName, long minValue);
	SequenceHolder nextRange(String seqName);
}
