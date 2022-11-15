package com.yuweix.assist4j.session;



/**
 * 避免同一账号多处登录。
 * 可将判重条件用RepeatKey包装，然后存入session即可
 * @author yuwei
 */
public class RepeatKey {
	private String value;

	public RepeatKey(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
}
