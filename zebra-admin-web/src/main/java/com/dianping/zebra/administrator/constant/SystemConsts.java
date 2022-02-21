package com.dianping.zebra.administrator.constant;

public class SystemConsts {

	public static final String DEFAULR_CHARSET = "UTF-8";

	public static final String GROUP_CONFIG_NAME_PATTERN = "zebra.group.%s";

	public static final String DS_CONFIG_PATTERN = "zebra.ds.%s";

	public static final String SHARD_CONFIG_NAME_PATTERN = "zebra.shard.%s";

	public static final String DS_NAME = "%s-n%s";

	public static final String JDBC_URL = "jdbc:mysql://%s/%s?characterEncoding=UTF8&socketTimeout=60000&allowMultiQueries=true";

	public static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";

	public static final String PROPERTIES = "initialPoolSize=3&maxPoolSize=20&minPoolSize=3&" +
			"idleConnectionTestPeriod=60&acquireRetryAttempts=50&acquireRetryDelay=300&" +
			"maxStatements=0&numHelperThreads=6&maxAdministrativeTaskTime=5&" +
			"preferredTestQuery=SELECT 1&checkoutTimeout=1000";
}
