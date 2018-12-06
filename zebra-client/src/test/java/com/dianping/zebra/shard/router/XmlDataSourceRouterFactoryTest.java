package com.dianping.zebra.shard.router;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.dianping.zebra.shard.exception.ShardParseException;
import com.dianping.zebra.shard.exception.ShardRouterException;
import com.dianping.zebra.shard.router.RouterResult.RouterTarget;
import com.dianping.zebra.shard.router.builder.XmlResourceRouterBuilder;

import junit.framework.Assert;

public class XmlDataSourceRouterFactoryTest {

	@Test
	public void test() throws ShardRouterException, ShardParseException {
		RouterBuilder routerFactory = new XmlResourceRouterBuilder("db-router-xml-rule.xml");
		ShardRouter router = routerFactory.build();

		List<Object> param = new ArrayList<Object>();
		param.add(100);
		param.add(200);

		RouterResult router2 = router.router("insert into risk_zebra_test (colOne,colTwo) values(?,?)", param);

		for (RouterTarget rt : router2.getSqls()) {
			Assert.assertEquals("id0", rt.getDatabaseName());
			for (String sql : rt.getSqls()) {
				Assert.assertEquals("INSERT INTO risk_zebra_test0 (colOne, colTwo)\nVALUES (?, ?)", sql);
			}
		}
	}

	@Test
	public void testZeroPadding0() throws ShardRouterException, ShardParseException {
		RouterBuilder routerFactory = new XmlResourceRouterBuilder("db-router-xml-rule-zeropadding.xml");
		ShardRouter router = routerFactory.build();

		List<Object> param = new ArrayList<Object>();
		param.add(0);
		param.add(200);

		RouterResult router2 = router.router("insert into risk_zebra_test (colOne,colTwo) values(?,?)", param);

		for (RouterTarget rt : router2.getSqls()) {
			Assert.assertEquals("id0", rt.getDatabaseName());
			for (String sql : rt.getSqls()) {
				Assert.assertEquals("INSERT INTO risk_zebra_test00 (colOne, colTwo)\nVALUES (?, ?)", sql);
			}
		}
	}

	@Test
	public void testZeroPadding1() throws ShardRouterException, ShardParseException {
		RouterBuilder routerFactory = new XmlResourceRouterBuilder("db-router-xml-rule-zeropadding.xml");
		ShardRouter router = routerFactory.build();

		List<Object> param = new ArrayList<Object>();
		param.add(100);
		param.add(200);

		RouterResult router2 = router.router("insert into risk_zebra_test (colOne,colTwo) values(?,?)", param);

		for (RouterTarget rt : router2.getSqls()) {
			Assert.assertEquals("id0", rt.getDatabaseName());
			for (String sql : rt.getSqls()) {
				Assert.assertEquals("INSERT INTO risk_zebra_test04 (colOne, colTwo)\nVALUES (?, ?)", sql);
			}
		}
	}

	@Test
	public void testZeroPadding2() throws ShardRouterException, ShardParseException {
		RouterBuilder routerFactory = new XmlResourceRouterBuilder("db-router-xml-rule-zeropadding.xml");
		ShardRouter router = routerFactory.build();

		List<Object> param = new ArrayList<Object>();
		param.add(31);
		param.add(200);

		RouterResult router2 = router.router("insert into risk_zebra_test (colOne,colTwo) values(?,?)", param);

		for (RouterTarget rt : router2.getSqls()) {
			Assert.assertEquals("id0", rt.getDatabaseName());
			for (String sql : rt.getSqls()) {
				Assert.assertEquals("INSERT INTO risk_zebra_test31 (colOne, colTwo)\nVALUES (?, ?)", sql);
			}
		}
	}
}
