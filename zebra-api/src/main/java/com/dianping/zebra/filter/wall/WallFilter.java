package com.dianping.zebra.filter.wall;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.zebra.filter.DefaultJdbcFilter;
import com.dianping.zebra.filter.JdbcFilter;
import com.dianping.zebra.group.config.SystemConfigManager;
import com.dianping.zebra.group.config.SystemConfigManagerFactory;
import com.dianping.zebra.group.config.system.entity.SqlFlowControl;
import com.dianping.zebra.group.util.DaoContextHolder;
import com.dianping.zebra.util.StringUtils;

/**
 * Created by Dozer on 9/24/14.
 * 
 * @author hao.zhu modified on 2/11/2015
 */
public class WallFilter extends DefaultJdbcFilter {
	private static final int MAX_ID_LENGTH = 8;

	private Map<String, String> sqlIDCache = new ConcurrentHashMap<String, String>(1024);

	private Map<String, SqlFlowControl> flowControl;

	private SystemConfigManager systemConfigManager;

	private Random random;

	protected void checkFlowControl(String id) throws SQLException {
		if (StringUtils.isNotBlank(id) && flowControl.containsKey(id)) {
			if (generateFlowPercent() >= flowControl.get(id).getAllowPercent()) {
				throw new SQLException("The SQL is in the blacklist. Please contact with dba!", "SQL.Blacklist");
			}
		}
	}

	protected int generateFlowPercent() {
		return random.nextInt(100);
	}

	protected String generateId(String dsId, String sqlAlias) throws NoSuchAlgorithmException {
		String token = String.format("/*%s*/%s", dsId, sqlAlias);
		String resultId = sqlIDCache.get(String.format("/*%s*/%s", dsId, sqlAlias));

		if (resultId != null) {
			return resultId;
		} else {
			resultId = StringUtils.md5(token).substring(0, MAX_ID_LENGTH);
			sqlIDCache.put(token, resultId);

			return resultId;
		}
	}

	public int getOrder() {
		return MIN_ORDER;
	}

	@Override
	public void init() {
		super.init();
		this.random = new Random();
		this.initFlowControl();
	}

	private void initFlowControl() {
		this.systemConfigManager = SystemConfigManagerFactory.getConfigManger(configManagerType);
		this.flowControl = this.systemConfigManager.getSystemConfig().getSqlFlowControls();

		this.systemConfigManager.addListerner(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
				synchronized (flowControl) {
					flowControl = systemConfigManager.getSystemConfig().getSqlFlowControls();
				}
			}
		});
	}

	@Override
	public String processSQL(String dsId, String sql, boolean isPreparedStmt, JdbcFilter chain) throws SQLException {
		if (chain != null) {
			sql = chain.processSQL(dsId, sql, isPreparedStmt, chain);
		}
		String sqlAlias = DaoContextHolder.getSqlName();

		if (isPreparedStmt && dsId != null && StringUtils.isNotBlank(sqlAlias)) {
			try {
				String id = generateId(dsId, sqlAlias);

				checkFlowControl(id);

				return String.format("/*id:%s*/%s", id, sql);
			} catch (NoSuchAlgorithmException e) {
				return sql;
			}
		} else {
			return sql;
		}
	}

	public Map<String, SqlFlowControl> getFlowControl() {
		return flowControl;
	}
}