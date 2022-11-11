package com.yuweix.assist4j.sequence.base;


import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.yuweix.assist4j.sequence.bean.SequenceHolder;
import com.yuweix.assist4j.sequence.dao.SequenceDao;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;


/**
 * @author yuwei
 */
public class DefaultSequence implements Sequence {
	private final Lock lock = new ReentrantLock();
	@AnnSequenceDao
	private SequenceDao sequenceDao;
	@AnnSequenceName
	private String name;
	@AnnSequenceMinValue
	private long minValue;
	private volatile SequenceHolder sequenceHolder;

	public DefaultSequence() {

	}

	@PostConstruct
	public void init() {
		synchronized(this) {
			sequenceDao.ensure(name, minValue);
		}
	}

	@Override
	public long nextValue() {
		if (sequenceHolder == null) {
			lock.lock();
			try {
				if (sequenceHolder == null) {
					sequenceHolder = sequenceDao.nextRange(name);
				}
			} finally {
				lock.unlock();
			}
		}

		long value = sequenceHolder.getAndIncrement();
		if (value <= 0L) {
			lock.lock();
			try {
				do {
					sequenceHolder = sequenceDao.nextRange(name);
					value = sequenceHolder.getAndIncrement();
				} while(value <= 0L);
			} finally {
				lock.unlock();
			}
		}
		
		return value;
	}

	@PreDestroy
	public void destroy() {

	}

	public void setSequenceDao(SequenceDao sequenceDao) {
		this.sequenceDao = sequenceDao;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setMinValue(long minValue) {
		this.minValue = minValue;
	}
}
