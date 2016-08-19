/**
 * Project: zebra-client
 * 
 * File Created at 2011-6-22
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
package com.dianping.zebra.shard.merge.groupby;

import com.dianping.zebra.shard.merge.groupby.aggregate.AggregateException;

/**
 * <p>
 * 聚合函数的数据处理器接口
 * <p>
 * 
 * @author Leo Liang
 * 
 */
public interface Aggregator {

	/**
	 * 根据旧值和当前值计算新值并返回<br>
	 * 
	 * <strong>注意：如果旧值为<tt>null</tt>则应该根据当前值返回一个合适的初始值</strong>
	 * 
	 * 
	 * @param oldValue
	 *            旧值
	 * @param currentValue
	 *            当前值
	 * @return 新的计算结果值
	 * @throws AggregateException
	 */
	public Object process(Object oldValue, Object currentValue) throws AggregateException;

}
