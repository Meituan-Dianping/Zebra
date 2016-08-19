/**
 * Project: com.dianping.zebra.zebra-client-0.1.0
 * 
 * File Created at 2011-6-16
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.zebra.shard.exception;

import com.dianping.zebra.exception.ZebraException;

/**
 * @author danson.liu
 *
 */
public class ShardRouterException extends ZebraException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8724570679105220336L;

	public ShardRouterException() {
		super();
	}

	/**
	 * Constructs a new runtime exception with the specified detail message.
	 */
	public ShardRouterException(String message) {
		super(message);
	}

	/**
	 * Constructs a new runtime exception with the specified detail message and
	 * cause.
	 */
	public ShardRouterException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new runtime exception with the specified cause.
	 */
	public ShardRouterException(Throwable cause) {
		super(cause);
	}
	
}
