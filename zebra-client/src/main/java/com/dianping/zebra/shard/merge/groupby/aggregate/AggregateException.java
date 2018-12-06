/**
 * Project: zebra-client
 * 
 * File Created at 2011-6-24
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
package com.dianping.zebra.shard.merge.groupby.aggregate;

/**
 * <tt>AggregateDataProcessor</tt>处理中抛出的异常
 * 
 * @author Leo Liang
 * 
 */
public class AggregateException extends Exception {

	private static final long	serialVersionUID	= -4563750786940638103L;

	public AggregateException(String msg) {
		super(msg);
	}

}
