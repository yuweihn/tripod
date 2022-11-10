package com.yuweix.assist4j.data.elect;


import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * @author yuwei
 */
public abstract class AbstractElector implements Elector {
	private final String localNode;

	public AbstractElector() {
		this.localNode = getLocalInnerIP();
	}

	@Override
	public String getLocalNode() {
		return localNode;
	}

	@PostConstruct
	public void init() {

	}

	@PreDestroy
	public void destroy() {

	}

	/**
	 * 获取本机内网IP
	 */
	private static String getLocalInnerIP() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}
}