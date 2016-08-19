package com.dianping.zebra.config;

import com.dianping.zebra.Constants;
import com.dianping.zebra.util.StringUtils;

public final class LionKey {
    private LionKey() {
    }

    public static String getJdbcRefConfigKey(String jdbcRef) {
        return String.format("%s.%s.%s", Constants.DEFAULT_DATASOURCE_GROUP_PRFIX, jdbcRef, "mapping");
    }
    
    public static String getDatabaseSecurityConfigKey(String database) {
        return String.format("%s.%s.%s", Constants.DEFAULT_DATASOURCE_GROUP_PRFIX, database, "security");
    }
    
    public static String getDatabaseSecuritySwitchKey() {
    	return "zebra.system.security";
    }

    public static String getDsJdbcUrlConfigKey(String dsName) {
        return String.format("%s.%s.%s", Constants.DEFAULT_DATASOURCE_SINGLE_PRFIX, dsName, "jdbc.url");
    }

    public static String getDsUsernameConfigKey(String dsName) {
        return String.format("%s.%s.%s", Constants.DEFAULT_DATASOURCE_SINGLE_PRFIX, dsName, "jdbc.username");
    }

    public static String getDsPasswordConfigKey(String dsName) {
        return String.format("%s.%s.%s", Constants.DEFAULT_DATASOURCE_SINGLE_PRFIX, dsName, "jdbc.password");
    }

    public static String getDsActiveConfigKey(String dsName) {
        return String.format("%s.%s.%s", Constants.DEFAULT_DATASOURCE_SINGLE_PRFIX, dsName, "jdbc.active");
    }

    public static String getDsPropertiesConfigKey(String dsName) {
        return String.format("%s.%s.%s", Constants.DEFAULT_DATASOURCE_SINGLE_PRFIX, dsName, "jdbc.properties");
    }

    public static String getDsDriverClassConfigKey(String dsName) {
        return String.format("%s.%s.%s", Constants.DEFAULT_DATASOURCE_SINGLE_PRFIX, dsName, "jdbc.driverClass");
    }

    public static String getShardConfigKey(String ruleName) {
        return String.format("%s.%s.%s", Constants.DEFAULT_SHARDING_PRFIX, ruleName, "shard");
    }

    public static String getShardSiwtchOnKey(String ruleName) {
        return String.format("%s.%s.%s", Constants.DEFAULT_SHARDING_PRFIX, ruleName, "switch");
    }

    public static boolean isShardConfigKey(String key) {
        return StringUtils.isNotBlank(key) && key.endsWith("shard");
    }

    public static String getRuleNameFromShardKey(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        String[] array = key.split("\\.");
        if (array.length != 3) {
            return null;
        }
        return array[1];
    }
}
