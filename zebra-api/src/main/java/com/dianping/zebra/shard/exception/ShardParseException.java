package com.dianping.zebra.shard.exception;

import com.dianping.zebra.exception.ZebraException;

public class ShardParseException extends ZebraException {

	/**
	 * 
	 */
   private static final long serialVersionUID = -1814311695297681608L;

	public ShardParseException() {
		super();
	}

	/**
	 * Constructs a new runtime exception with the specified detail message.
	 */
	public ShardParseException(String message) {
		super(message);
	}

	/**
	 * Constructs a new runtime exception with the specified detail message and
	 * cause.
	 */
	public ShardParseException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new runtime exception with the specified cause.
	 */
	public ShardParseException(Throwable cause) {
		super(cause);
	}
	
}
