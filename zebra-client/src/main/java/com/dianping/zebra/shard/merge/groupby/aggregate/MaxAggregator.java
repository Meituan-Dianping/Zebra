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

import java.math.BigDecimal;
import java.math.BigInteger;

import com.dianping.zebra.shard.merge.groupby.Aggregator;

/**
 * <p>
 * 处理max()函数的数据处理器
 * </p>
 * 
 * <p>
 * 现在支持的类型包括<tt>Integer</tt>，<tt>Long</tt>，<tt>Short</tt>，<tt>Float</tt>， <tt>Double</tt>，<tt>BigDecimal</tt>
 * </p>
 * 
 * @author Leo Liang
 * 
 */
public class MaxAggregator implements Aggregator {

	/**
	 * 根据旧值(处理当前列之前的最大值)和当前值返回当前值和旧值的最大值
	 *
	 * @param oldValue
	 *           旧值
	 * @param currentValue
	 *           当前值
	 * @return max结果
	 * @throws AggregateException
	 */
	public Object process(Object oldValue, Object currentValue) throws AggregateException {
		if (currentValue == null) {
			return oldValue;
		}

		Class<?> type = currentValue.getClass();

		if (oldValue == null) {
			return currentValue;
		}

		if (oldValue.getClass() != type) {
			throw new AggregateException("oldValue.class != currentValue.class");
		}

		if (currentValue instanceof Integer) {
			return (Integer) oldValue > (Integer) currentValue ? (Integer) oldValue : (Integer) currentValue;
		} else if (currentValue instanceof Long) {
			return (Long) oldValue > (Long) currentValue ? (Long) oldValue : (Long) currentValue;
		} else if (currentValue instanceof Short) {
			return (Short) oldValue > (Short) currentValue ? (Short) oldValue : (Short) currentValue;
		} else if (currentValue instanceof Float) {
			return (Float) oldValue > (Float) currentValue ? (Float) oldValue : (Float) currentValue;
		} else if (currentValue instanceof Double) {
			return (Double) oldValue > (Double) currentValue ? (Double) oldValue : (Double) currentValue;
		} else if (currentValue instanceof BigDecimal) {
			return ((BigDecimal) oldValue).compareTo((BigDecimal) currentValue) == 1 ? (BigDecimal) oldValue
			      : (BigDecimal) currentValue;
		} else if (currentValue instanceof BigInteger) {
			return ((BigInteger) oldValue).compareTo((BigInteger) currentValue) == 1 ? (BigInteger) oldValue
			      : (BigInteger) currentValue;
		} else {
			throw new AggregateException("Can not process groupby function max() for type: " + currentValue.getClass());
		}
	}
}
