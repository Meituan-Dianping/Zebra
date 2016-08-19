package com.dianping.zebra.single.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.junit.Test;

import com.dianping.zebra.Constants;
import com.dianping.zebra.group.config.datasource.entity.Any;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.single.jdbc.SingleDataSource;
import com.dianping.zebra.single.pool.ZebraPoolManager;

import java.sql.Statement;

public class ZebraPoolManagerTest {

	@Test
	public void testDruid() throws SQLException {

		List<Any> propsList = new ArrayList<Any>();
		Any any = new Any();
		any.setName("initialPoolSize");
		any.setValue("5");
		propsList.add(any);
		any = new Any();
		any.setName("maxPoolSize");
		any.setValue("20");
		propsList.add(any);
		any = new Any();
		any.setName("minPoolSize");
		any.setValue("5");
		propsList.add(any);
		any = new Any();
		any.setName("idleConnectionTestPeriod");
		any.setValue("60");
		propsList.add(any);
		any = new Any();
		any.setName("acquireRetryAttempts");
		any.setValue("50");
		propsList.add(any);
		any = new Any();
		any.setName("acquireRetryDelay");
		any.setValue("300");
		propsList.add(any);
		any = new Any();
		any.setName("maxStatements");
		any.setValue("0");
		propsList.add(any);
		any = new Any();
		any.setName("numHelperThreads");
		any.setValue("6");
		propsList.add(any);
		any = new Any();
		any.setName("maxAdministrativeTaskTime");
		any.setValue("5");
		propsList.add(any);
		any = new Any();
		any.setName("preferredTestQuery");
		any.setValue("select 1");
		propsList.add(any);
		any = new Any();
		any.setName("checkoutTimeout");
		any.setValue("5000");
		propsList.add(any);

		DataSourceConfig config = new DataSourceConfig();

		config.setType(Constants.CONNECTION_POOL_TYPE_DRUID);
		config.setActive(true);
		config.setCanRead(true);
		config.setCanWrite(true);
		config.setDriverClass("com.mysql.jdbc.Driver");
		config.setId("test-druid");
		config.setJdbcUrl("jdbc:mysql://10.1.77.20:3306/zebra?characterEncoding=UTF8&socketTimeout=60000");
		config.setUsername("zebra_a");
		config.setPassword("dp!@aFDceborN");
		config.setProperties(propsList);

		DataSource ds = ZebraPoolManager.buildDataSource(config);

		Connection con = ds.getConnection();
		Statement st = con.createStatement();
		ResultSet result = st.executeQuery("select * from `Cluster` where id=20");

		while (result.next()) {
			System.out.println(result.getString(2));
		}

		ZebraPoolManager.close(new SingleDataSource(config, null));
	}
}
