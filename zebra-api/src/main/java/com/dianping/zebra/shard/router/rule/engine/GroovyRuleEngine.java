/**
 * Project: com.dianping.zebra.zebra-client-0.1.0
 * <p/>
 * File Created at 2011-6-15
 * $Id$
 * <p/>
 * Copyright 2010 dianping.com.
 * All rights reserved.
 * <p/>
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.zebra.shard.router.rule.engine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.shard.router.rule.dimension.AbstractDimensionRule;
import com.dianping.zebra.shard.router.rule.dimension.DimensionRule;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

/**
 *
 * @author danson.liu, Dozer
 */
public class GroovyRuleEngine implements RuleEngine {

	private final GroovyObject engineObj;

	private static final Map<String, Class<?>> RULE_CLASS_CACHE = new ConcurrentHashMap<String, Class<?>>();

	public GroovyRuleEngine(String rule) {
		try {
			engineObj = getGroovyObject(rule);
		} catch (Exception e) {
			throw new ZebraConfigException("Construct groovy rule engine failed, cause: ", e);
		}
	}

	@SuppressWarnings("resource")
	private static final GroovyObject getGroovyObject(String rule)
			throws IllegalAccessException, InstantiationException {
		if (!RULE_CLASS_CACHE.containsKey(rule)) {
			synchronized (GroovyRuleEngine.class) {
				if (!RULE_CLASS_CACHE.containsKey(rule)) {
					Matcher matcher = DimensionRule.RULE_COLUMN_PATTERN.matcher(rule);
					StringBuilder engineClazzImpl = new StringBuilder(200)
							.append("class RuleEngineBaseImpl extends " + RuleEngineBase.class.getName() + "{")
							.append("Object execute(Map context) {").append(matcher.replaceAll("context.get(\"$1\")"))
							.append("}").append("}");
					GroovyClassLoader loader = new GroovyClassLoader(AbstractDimensionRule.class.getClassLoader());
					Class<?> engineClazz = loader.parseClass(engineClazzImpl.toString());
					RULE_CLASS_CACHE.put(rule, engineClazz);
				}
			}
		}
		return (GroovyObject) RULE_CLASS_CACHE.get(rule).newInstance();
	}

	@Override
	public Object eval(Map<String, Object> valMap) {
		return engineObj.invokeMethod("execute", valMap);
	}
}
