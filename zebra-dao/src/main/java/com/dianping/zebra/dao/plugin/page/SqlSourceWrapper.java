package com.dianping.zebra.dao.plugin.page;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;

/**
 * 
 * @author damonzhu
 *
 */
public class SqlSourceWrapper implements SqlSource {
	private BoundSql boundSql;

	public SqlSourceWrapper(BoundSql boundSql) {
		this.boundSql = boundSql;
	}

	public BoundSql getBoundSql(Object parameterObject) {
		return boundSql;
	}
}