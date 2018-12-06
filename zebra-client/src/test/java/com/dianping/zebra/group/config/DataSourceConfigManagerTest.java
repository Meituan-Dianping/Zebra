package com.dianping.zebra.group.config;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.dianping.zebra.Constants;
import com.dianping.zebra.config.ServiceConfigBuilder;
import com.dianping.zebra.group.config.datasource.entity.GroupConfig;
import com.dianping.zebra.group.config.datasource.entity.SingleConfig;
import com.dianping.zebra.util.JaxbUtils;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.transform.DefaultSaxParser;

public class DataSourceConfigManagerTest {

	public void testAssert(String config, GroupConfig groupConfig) {
		Map<String, SingleConfig> singleMap = new LinkedHashMap<>();

		GroupConfig expected = JaxbUtils.fromXml(config, GroupConfig.class);
		for (SingleConfig single : expected.getSingleConfigs()) {
			single.checkConfig();
			singleMap.put(single.getName(), single);
		}

		Assert.assertEquals(expected.getSingleConfigs().size(), groupConfig.getSingleConfigs().size());

		for (SingleConfig single : groupConfig.getSingleConfigs()) {
			single.checkConfig();
			SingleConfig expectedSingle = singleMap.get(single.getName());

			Assert.assertNotNull(expectedSingle);
			Assert.assertEquals(expectedSingle.getReadWeight(), single.getReadWeight());
			Assert.assertEquals(expectedSingle.getWriteWeight(), single.getWriteWeight());
		}
	}

	@Test
	public void testConfig() throws SAXException, IOException {
		String dataSourceResourceId = "sample.ds.v2";
		Map<String, Object> configs = ServiceConfigBuilder.newInstance()
		      .putValue(Constants.CONFIG_SERVICE_NAME_KEY, dataSourceResourceId).getConfigs();
		DataSourceConfigManager dataSourceConfigManager = DataSourceConfigManagerFactory
		      .getConfigManager(Constants.CONFIG_MANAGER_TYPE_LOCAL, configs);
		Map<String, DataSourceConfig> config = dataSourceConfigManager.getGroupDataSourceConfig().getDataSourceConfigs();

		Map<String, DataSourceConfig> dataSourceConfigs = DefaultSaxParser
		      .parse(getClass().getClassLoader().getResourceAsStream("model/datasources.xml")).getDataSourceConfigs();

		for (DataSourceConfig entry : config.values()) {
			DataSourceConfig dataSourceConfig = dataSourceConfigs.get(entry.getId());
			Assert.assertEquals(dataSourceConfig.toString(), entry.toString());
		}
	}

	@Test
	public void testParseConfig() {
		String config = "<groupConfig>\n" + "    <singleConfig>\n" + "        <name>mobile-n1</name>\n"
		      + "        <writeWeight>1</writeWeight>\n" + "        <readWeight>1</readWeight>\n"
		      + "    </singleConfig>\n" + "    <singleConfig>\n" + "        <name>mobile-n2</name>\n"
		      + "        <writeWeight>0</writeWeight>\n" + "        <readWeight>1</readWeight>\n"
		      + "        <active>true</active>\n" + "    </singleConfig>\n" + "</groupConfig>";

		GroupConfig groupConfig = new GroupConfig();
		List<SingleConfig> singleConfigs = new LinkedList<>();
		SingleConfig mobile1 = new SingleConfig("mobile-n1", 1, 1);
		SingleConfig mobile2 = new SingleConfig("mobile-n2", 0, 1);
		singleConfigs.add(mobile1);
		singleConfigs.add(mobile2);
		groupConfig.setSingleConfigs(singleConfigs);

		testAssert(config, groupConfig);
	}
}
