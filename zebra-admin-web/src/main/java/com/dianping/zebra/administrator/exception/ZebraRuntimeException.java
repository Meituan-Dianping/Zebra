package com.dianping.zebra.administrator.exception;

public class ZebraRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -511024199002140052L;

	public ZebraRuntimeException(String message) {
		super(message);
	}

	public ZebraRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
