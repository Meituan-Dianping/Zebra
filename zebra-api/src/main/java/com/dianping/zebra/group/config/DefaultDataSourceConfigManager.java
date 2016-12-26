package com.dianping.zebra.group.config;

import com.dianping.zebra.Constants;
import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.group.config.datasource.entity.Any;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;
import com.dianping.zebra.group.config.datasource.transform.BaseVisitor;
import com.dianping.zebra.util.AppPropertiesUtils;
import com.dianping.zebra.util.JdbcDriverClassHelper;
import com.dianping.zebra.util.Splitters;
import com.dianping.zebra.util.StringUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DefaultDataSourceConfigManager extends AbstractConfigManager implements DataSourceConfigManager {

	private final char keyValueSeparator = '=';

	private final char pairSeparator = '&';

	private GroupDataSourceConfigBuilder builder;

	private String jdbcRef;

	public DefaultDataSourceConfigManager(String jdbcRef, ConfigService configService) {
		super(configService);
		this.jdbcRef = jdbcRef;
	}

	@Override
	public void addListerner(PropertyChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public synchronized void init() {
		try {
			this.builder = new GroupDataSourceConfigBuilder();
		} catch (Exception e) {
			throw new ZebraConfigException(String.format(
			      "Fail to initialize DefaultDataSourceConfigManager with config key[%s].", this.jdbcRef), e);
		}
	}

	@Override
	public GroupDataSourceConfig getGroupDataSourceConfig() {
		return initGroupDataSourceConfig();
	}

	private GroupDataSourceConfig initGroupDataSourceConfig() {
		GroupDataSourceConfig config = new GroupDataSourceConfig();
		this.builder.visitGroupDataSourceConfig(config);
		return config;
	}

	@Override
	protected synchronized void onPropertyUpdated(PropertyChangeEvent evt) {
	}

	private void validateConfig(Map<String, DataSourceConfig> dataSourceConfigs) {
		int readNum = 0, writeNum = 0;
		for (Entry<String, DataSourceConfig> entry : dataSourceConfigs.entrySet()) {
			if (entry.getValue().getCanRead()) {
				readNum += 1;
			}
			if (entry.getValue().getCanWrite()) {
				writeNum += 1;
			}
		}

		if (readNum < 1 || writeNum < 1) {
			throw new ZebraConfigException(String.format("Not enough read or write dataSources[read:%s, write:%s].",
			      readNum, writeNum));
		}
	}

	public static class ReadOrWriteRole {
		private boolean isRead;

		private boolean isWrite;

		private int weight;

		public static Map<String, ReadOrWriteRole> parseConfig(String config) {
			Map<String, ReadOrWriteRole> dataSources = new LinkedHashMap<String, ReadOrWriteRole>();
			if (StringUtils.isBlank(config)) {
				return dataSources;
			}
			StringBuilder name = new StringBuilder(20);
			StringBuilder role = new StringBuilder(5);

			boolean isName = false;
			for (int i = 0; i < config.length(); i++) {
				char c = config.charAt(i);

				if (c == '(') {
					isName = true;
				} else if (c == ')') {
					setNameAndRole(dataSources, name, role);

					isName = false;
					name.setLength(0);
					role.setLength(0);
				} else if (c == ':') {
					isName = false;
				} else if (c == ',') {
					if (name.length() > 0) {
						setNameAndRole(dataSources, name, role);

						isName = true;
						name.setLength(0);
						role.setLength(0);
					} else {
						isName = false;
					}
				} else {
					if (isName) {
						name.append(c);
					} else {
						role.append(c);
					}
				}
			}

			return dataSources;
		}

		private static void setNameAndRole(Map<String, ReadOrWriteRole> dataSources, StringBuilder name,
		      StringBuilder role) {
			String key = name.toString().trim();
			String value = role.toString().trim();

			ReadOrWriteRole readOrWrite = dataSources.get(key);
			if (readOrWrite == null) {
				readOrWrite = new ReadOrWriteRole();
				dataSources.put(key, readOrWrite);
			}

			if (value.length() > 0) {
				readOrWrite.setRead(true);
				readOrWrite.setWeight(Integer.parseInt(value));
			} else {
				readOrWrite.setWrite(true);
			}
		}

		public int getWeight() {
			return weight;
		}

		public void setWeight(int weight) {
			this.weight = weight;
		}

		public boolean isRead() {
			return isRead;
		}

		public void setRead(boolean isRead) {
			this.isRead = isRead;
		}

		public boolean isWrite() {
			return isWrite;
		}

		public void setWrite(boolean isWrite) {
			this.isWrite = isWrite;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(5);

			if (isWrite) {
				sb.append('w');
			}

			if (isRead) {
				sb.append('r');
				sb.append(weight);
			}

			return sb.toString();
		}
	}

	class GroupDataSourceConfigBuilder extends BaseVisitor {

		private String getGroupDataSourceKey() {
			return String.format("%s.%s.mapping", Constants.DEFAULT_DATASOURCE_GROUP_PRFIX, jdbcRef);
		}

		private String getGroupDataSourceKeyForApp() {
			return String.format("%s.%s", getGroupDataSourceKey(), AppPropertiesUtils.getAppName());
		}

		private String getGroupDataSourceKeyForAppUpdateFlag() {
			return String.format("%s.%s", Constants.DEFAULT_DATASOURCE_GROUP_PRFIX, Constants.ELEMENT_APP_REFRESH_FLAG);
		}

		private String getSingleDataSourceKey(String key, String dsId) {
			return String.format("%s.%s.jdbc.%s", Constants.DEFAULT_DATASOURCE_SINGLE_PRFIX, dsId, key);
		}

		@Override
		public void visitDataSourceConfig(DataSourceConfig dsConfig) {
			String dsId = dsConfig.getId();

			dsConfig.setId(dsId);
			dsConfig.setActive(getProperty(getSingleDataSourceKey(Constants.ELEMENT_ACTIVE, dsId), dsConfig.getActive()));
			dsConfig.setTestReadOnlySql(getProperty(getSingleDataSourceKey(Constants.ELEMENT_TEST_READONLY_SQL, dsId),
			      dsConfig.getTestReadOnlySql()));
			dsConfig.setPunishLimit(getProperty(getSingleDataSourceKey(Constants.ELEMENT_PUNISH_LIMIT, dsId),
			      dsConfig.getPunishLimit()));
			dsConfig.setTimeWindow(getProperty(getSingleDataSourceKey(Constants.ELEMENT_TIME_WINDOW, dsId),
			      dsConfig.getTimeWindow()));

			String jdbcUrl = getProperty(getSingleDataSourceKey(Constants.ELEMENT_JDBC_URL, dsId), dsConfig.getJdbcUrl());
			dsConfig.setJdbcUrl(jdbcUrl);

			String driverClass = getProperty(getSingleDataSourceKey(Constants.ELEMENT_DRIVER_CLASS, dsId),
			      dsConfig.getDriverClass());
			if (StringUtils.isBlank(driverClass)) {
				driverClass = JdbcDriverClassHelper.getDriverClassNameByJdbcUrl(jdbcUrl);
			}

			dsConfig.setDriverClass(driverClass);
			dsConfig.setTag(
				getProperty(getSingleDataSourceKey(Constants.ELEMENT_TAG, dsId), dsConfig.getTag()));
			dsConfig.setPassword(getProperty(getSingleDataSourceKey(Constants.ELEMENT_PASSWORD, dsId),
			      dsConfig.getPassword()));
			dsConfig.setWarmupTime(getProperty(getSingleDataSourceKey(Constants.ELEMENT_WARMUP_TIME, dsId),
			      dsConfig.getWarmupTime()));
			dsConfig
			      .setUsername(getProperty(getSingleDataSourceKey(Constants.ELEMENT_USER, dsId), dsConfig.getUsername()));
			dsConfig.setType(getProperty(Constants.ELEMENT_POOL_TYPE, Constants.CONNECTION_POOL_TYPE_C3P0));

			String properies = getProperty(getSingleDataSourceKey(Constants.ELEMENT_PROPERTIES, dsId), null);

			if (properies != null) {
				Map<String, String> sysMap = Splitters.by(pairSeparator, keyValueSeparator).trim().split(properies);

				for (Entry<String, String> property : sysMap.entrySet()) {
					Any any = new Any();
					any.setName(property.getKey());
					any.setValue(property.getValue());

					dsConfig.getProperties().add(any);
				}

				// hack for maxStatementsPerConnection, since the lion key ${ds.$.jdbc.properties} does not set this value.
				if (!sysMap.containsKey("maxStatementsPerConnection")) {
					Any any = new Any();
					any.setName("maxStatementsPerConnection");
					any.setValue("100");

					dsConfig.getProperties().add(any);
				}
			}
		}

		@Override
		public void visitGroupDataSourceConfig(GroupDataSourceConfig groupDsConfig) {
			String config = configService.getProperty(getGroupDataSourceKeyForApp());
			if (StringUtils.isBlank(config)) {
				config = configService.getProperty(getGroupDataSourceKey());

				// 监听该属性的自动触发
				configService.getProperty(getGroupDataSourceKeyForAppUpdateFlag());
			}

			if (config != null && config.length() > 0) {
				Map<String, ReadOrWriteRole> pairs = ReadOrWriteRole.parseConfig(config);

				for (Entry<String, ReadOrWriteRole> pair : pairs.entrySet()) {
					String key = pair.getKey();
					ReadOrWriteRole role = pair.getValue();

					DataSourceConfig dataSource = groupDsConfig.findOrCreateDataSourceConfig(key);
					visitDataSourceConfig(dataSource);
					dataSource.setCanRead(role.isRead());
					dataSource.setWeight(role.getWeight());
					dataSource.setCanWrite(role.isWrite());
				}

				validateConfig(groupDsConfig.getDataSourceConfigs());
			}

			groupDsConfig.setFilters("cat,wall");
//			groupDsConfig.setFilters(getProperty(
//			      String.format("%s.default.filters", Constants.DEFAULT_DATASOURCE_ZEBRA_PRFIX), null));
		}
	}
}
