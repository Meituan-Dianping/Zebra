/**
 * Project: zebra-client
 * 
 * File Created at 2011-6-27
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
package com.dianping.zebra.shard.jdbc.base;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Leo Liang
 * 
 */
public abstract class BaseTestCase {
	protected abstract String[] getSupportedOps();

	protected abstract Object getTestObj();

	private static Map<String, Object> primitiveTypeMapping = new HashMap<String, Object>();

	static {
		primitiveTypeMapping.put("int", new Integer(0));
		primitiveTypeMapping.put("boolean", Boolean.TRUE);
		primitiveTypeMapping.put("long", new Long(1));
		primitiveTypeMapping.put("byte", (byte) 1);
		primitiveTypeMapping.put("double", new Double(1.0));
		primitiveTypeMapping.put("float", new Float(1.0));
		primitiveTypeMapping.put("short", (short) 1);
	}

	@Test
	public void testUnsupportedOps() throws Exception {
		Method[] methods = getTestObj().getClass().getDeclaredMethods();
		for (Method method : methods) {
			if (!Arrays.asList(getSupportedOps()).contains(method.getName()) && method.getModifiers() == 1) {
				Class<?>[] paramTypes = method.getParameterTypes();
				List<Object> paramList = new ArrayList<Object>(paramTypes.length);
				for (Class<?> paramType : paramTypes) {
					if (paramType.isPrimitive()) {
						paramList.add(primitiveTypeMapping.get(paramType.getName()));
					} else {
						paramList.add(null);
					}
				}

				try {
					System.out.println(
							"Test unsupported Ops for method: " + getTestObj().getClass() + "#" + method.getName());
					method.invoke(getTestObj(), paramList.toArray(new Object[0]));
					Assert.fail("Unsupported method doesn't throw UnsupportedOperationException. Method:"
							+ method.getName());
				} catch (InvocationTargetException e) {
					Assert.assertTrue((e.getCause() instanceof UnsupportedOperationException));
				}
			}
		}
	}
}
