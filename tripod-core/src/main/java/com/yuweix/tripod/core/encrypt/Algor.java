package com.yuweix.tripod.core.encrypt;


/**
 * @author yuwei
 */
public enum Algor {
	MD5("MD5"),
	SHA1("SHA-1"),
	DES("DES");

	private String code;
	Algor(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
