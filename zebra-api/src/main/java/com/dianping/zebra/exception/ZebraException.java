package com.dianping.zebra.exception;

public class ZebraException extends RuntimeException {

	private static final long serialVersionUID = -8628442877335107998L;

	public ZebraException() {
		super();
	}

	public ZebraException(String message) {
		super(message);
	}

	public ZebraException(String message, Throwable cause) {
		super(message, cause);
	}

	public ZebraException(Throwable cause) {
		super(cause);
	}
}
