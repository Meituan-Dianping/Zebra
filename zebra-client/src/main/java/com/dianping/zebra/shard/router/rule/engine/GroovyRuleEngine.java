/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
