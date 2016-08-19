package com.dianping.zebra.group.exception;

import com.dianping.zebra.exception.ZebraException;

public class SlaveDsDisConnectedException extends ZebraException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2417999991648922583L;

	public SlaveDsDisConnectedException() {
		super();
	}

	public SlaveDsDisConnectedException(String message) {
		super(message);
	}

	public SlaveDsDisConnectedException(String message, Throwable cause) {
		super(message, cause);
	}

	public SlaveDsDisConnectedException(Throwable cause) {
		super(cause);
	}
}
