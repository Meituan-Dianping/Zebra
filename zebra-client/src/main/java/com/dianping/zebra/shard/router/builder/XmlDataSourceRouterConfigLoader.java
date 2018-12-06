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
package com.dianping.zebra.shard.router.builder;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.shard.config.ExceptionalDimensionConfig;
import com.dianping.zebra.shard.config.RouterRuleConfig;
import com.dianping.zebra.shard.config.TableShardDimensionConfig;
import com.dianping.zebra.shard.config.TableShardRuleConfig;

/**
 * @author danson.liu
 *
 */
public class XmlDataSourceRouterConfigLoader {

	public RouterRuleConfig loadConfig(String routerRuleFile) {
		try {
			RouterRuleConfig ruleConfig = new RouterRuleConfig();
			ClassLoader classLoader = getDefaultClassLoader();
			URL resource = classLoader.getResource(routerRuleFile);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document configDoc = builder.parse(resource.openStream());
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
			ruleConfig.setTableShardConfigs(parseTableShardConfig(configDoc,
			      xpath.compile("/router-rule/table-shard-rule")));
			return ruleConfig;
		} catch (ZebraConfigException e) {
			throw e;
		} catch (Exception e) {
			throw new ZebraConfigException("Load router rule config failed, cause: ", e);
		}
	}

	private List<TableShardRuleConfig> parseTableShardConfig(Document document, XPathExpression expression)
	      throws XPathExpressionException {
		List<TableShardRuleConfig> shardConfigs = new ArrayList<TableShardRuleConfig>();
		NodeList shardConfigNodes = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
		for (int i = 0; i < shardConfigNodes.getLength(); i++) {
			TableShardRuleConfig shardConfig = new TableShardRuleConfig();
			Element configItem = (Element) shardConfigNodes.item(i);
			shardConfig.setTableName(configItem.getAttribute("table"));
			shardConfig.setDimensionConfigs(parseDimensionConfigs(configItem));
			validateShardConfig(shardConfig);
			shardConfigs.add(shardConfig);
		}
		return shardConfigs;
	}

	private void validateShardConfig(TableShardRuleConfig shardConfig) {
		List<TableShardDimensionConfig> dimensionConfigs = shardConfig.getDimensionConfigs();
		if (dimensionConfigs == null || dimensionConfigs.isEmpty()) {
			throw new ZebraConfigException("A shard-dimension must be set in table-shard-rule["
			      + shardConfig.getTableName() + "] element at least.");
		}
		int masterCount = 0;
		for (TableShardDimensionConfig dimensionConfig : dimensionConfigs) {
			if (dimensionConfig.isMaster()) {
				masterCount++;
			}
		}
		if (masterCount > 1) {
			throw new ZebraConfigException("More than one master shard-dimension exists in table-shard-rule["
			      + shardConfig.getTableName() + "].");
		} else if (dimensionConfigs.size() > 1 && masterCount == 0) {
			throw new ZebraConfigException("Must set a master shard-dimension when more than one dimension exists.");
		}
	}

	private List<TableShardDimensionConfig> parseDimensionConfigs(Element configItem) {
		List<TableShardDimensionConfig> dimensionConfigs = new ArrayList<TableShardDimensionConfig>();
		NodeList dimensionNodes = configItem.getElementsByTagName("shard-dimension");
		for (int i = 0; i < dimensionNodes.getLength(); i++) {
			TableShardDimensionConfig dimensionConfig = new TableShardDimensionConfig();
			Element dimensionItem = (Element) dimensionNodes.item(i);
			dimensionConfig.setTableName(configItem.getAttribute("table"));
			dimensionConfig.setDbRule(dimensionItem.getAttribute("dbRule"));
			dimensionConfig.setDbIndexes(dimensionItem.getAttribute("dbIndexes"));
			dimensionConfig.setTbRule(dimensionItem.getAttribute("tbRule"));
			dimensionConfig.setTbSuffix(dimensionItem.getAttribute("tbSuffix"));
			String zeroPadding = dimensionItem.getAttribute("tbSuffixZeroPadding");
			if (zeroPadding != null) {
				dimensionConfig.setTbSuffixZeroPadding(Boolean.parseBoolean(zeroPadding));
			}
			String isMaster = dimensionItem.getAttribute("isMaster");
			if (isMaster != null) {
				dimensionConfig.setMaster(Boolean.parseBoolean(isMaster));
			}
			dimensionConfig.setExceptionalDimensionConfig(parseExceptionConfigs(dimensionItem));
			dimensionConfigs.add(dimensionConfig);
		}
		return dimensionConfigs;
	}

	private List<ExceptionalDimensionConfig> parseExceptionConfigs(Element dimensionItem) {
		List<ExceptionalDimensionConfig> exceptionConfigs = new ArrayList<ExceptionalDimensionConfig>();
		NodeList exceptionNodes = dimensionItem.getElementsByTagName("exception");
		for (int i = 0; i < exceptionNodes.getLength(); i++) {
			ExceptionalDimensionConfig exceptionConfig = new ExceptionalDimensionConfig();
			Element exceptionItem = (Element) exceptionNodes.item(i);
			exceptionConfig.setCondition(exceptionItem.getAttribute("condition"));
			exceptionConfig.setDb(exceptionItem.getAttribute("db"));
			exceptionConfig.setTable(exceptionItem.getAttribute("table"));
			exceptionConfigs.add(exceptionConfig);
		}
		return exceptionConfigs;
	}

	private ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch (Exception ex) {
			// Cannot access thread context ClassLoader - falling back to system
			// class loader...
		}
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			cl = XmlDataSourceRouterConfigLoader.class.getClassLoader();
		}
		return cl;
	}

}
