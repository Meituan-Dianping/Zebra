package com.dianping.zebra.shard.router.rule;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.dianping.zebra.shard.router.rule.tableset.TableSets;
import com.dianping.zebra.shard.router.rule.tableset.DefaultTableSetsManager;

import junit.framework.Assert;

public class SimpleDataSourceProviderTest {
	@Test
	public void test_split_db() throws Exception {
		DefaultTableSetsManager target = new DefaultTableSetsManager("UOD_Order","a,b_[2-4]","alldb:[_Operation0,_Operation31]");
		Set<String> hashSet = new HashSet<String>();
		hashSet.add("a");
		hashSet.add("b_2");
		hashSet.add("b_3");
		hashSet.add("b_4");
		Assert.assertEquals(hashSet, target.getAllTableSets().keySet());
	}

	@Test
	public void test_split_db1() throws Exception {
		DefaultTableSetsManager target = new DefaultTableSetsManager("UOD_Order","a,b_[2-4]_dev","alldb:[_Operation0,_Operation31]");
		Set<String> hashSet = new HashSet<String>();
		hashSet.add("a");
		hashSet.add("b_2_dev");
		hashSet.add("b_3_dev");
		hashSet.add("b_4_dev");
		Assert.assertEquals(hashSet, target.getAllTableSets().keySet());
	}


	@Test
	public void test_split_table1() throws Exception {
		DefaultTableSetsManager target = new DefaultTableSetsManager("DP_GroupFollowNote","mysqldianpinggroup_dbo","alldb:[]");

		Map<String, Set<String>> allDBAndTables = target.getAllTableSets();
		Assert.assertEquals(1, allDBAndTables.size());
		Assert.assertEquals(1, allDBAndTables.get("mysqldianpinggroup_dbo").size());
		Assert.assertEquals(true, allDBAndTables.get("mysqldianpinggroup_dbo").contains("DP_GroupFollowNote"));
	}

	@Test
	public void test_split_table2() throws Exception {
		DefaultTableSetsManager target = new DefaultTableSetsManager("DP_GroupFollowNote","mysqldianpinggroup_dbo","alldb:[*]");

		Map<String, Set<String>> allDBAndTables = target.getAllTableSets();
		Assert.assertEquals(1, allDBAndTables.size());
		Assert.assertEquals(1, allDBAndTables.get("mysqldianpinggroup_dbo").size());
		Assert.assertEquals(true, allDBAndTables.get("mysqldianpinggroup_dbo").contains("DP_GroupFollowNote"));
	}

	@Test
	public void test_split_table() throws Exception {
		DefaultTableSetsManager target = new DefaultTableSetsManager("UOD_Order","unifiedorder_operation","alldb:[_Operation0,_Operation0]");

		Map<String, Set<String>> allDBAndTables = target.getAllTableSets();
		Assert.assertEquals(1, allDBAndTables.size());
		Assert.assertEquals(1, allDBAndTables.get("unifiedorder_operation").size());
		Assert.assertEquals(true, allDBAndTables.get("unifiedorder_operation").contains("UOD_Order_Operation0"));

		TableSets dataSource = target.getTableSetsByPos(0);
		Assert.assertEquals(1, dataSource.getTableSets().size());
	}

	@Test
	public void test_rule_parser_alldb() throws Exception {
		DefaultTableSetsManager target = new DefaultTableSetsManager("UOD_Order","a,b_[2-4]","alldb:[_Operation0,_Operation31]");

		Map<String, Set<String>> allDBAndTables = target.getAllTableSets();
		Assert.assertEquals(4, allDBAndTables.size());
		Assert.assertEquals(8, allDBAndTables.get("a").size());
		Assert.assertEquals(8, allDBAndTables.get("b_2").size());
		Assert.assertEquals(8, allDBAndTables.get("b_3").size());
		Assert.assertEquals(8, allDBAndTables.get("b_4").size());

		TableSets dataSource = target.getTableSetsByPos(0);
		Assert.assertEquals(8, dataSource.getTableSets().size());
		Assert.assertTrue(dataSource.getTableSets().contains("UOD_Order_Operation0"));
		Assert.assertTrue(dataSource.getTableSets().contains("UOD_Order_Operation7"));
		Assert.assertFalse(dataSource.getTableSets().contains("UOD_Order_Operation8"));
	}

	@Test
	public void test_rule_parser_everydb() throws Exception {
		DefaultTableSetsManager target = new DefaultTableSetsManager("UOD_Order","a,b","everydb:[_Operation0,_Operation7]");

		Map<String, Set<String>> allDBAndTables = target.getAllTableSets();
		Assert.assertEquals(2, allDBAndTables.size());
		Assert.assertEquals(8, allDBAndTables.get("a").size());
		Assert.assertEquals(8, allDBAndTables.get("b").size());

		TableSets dataSource = target.getTableSetsByPos(0);
		Assert.assertEquals(8, dataSource.getTableSets().size());
		Assert.assertTrue(dataSource.getTableSets().contains("UOD_Order_Operation0"));
		Assert.assertTrue(dataSource.getTableSets().contains("UOD_Order_Operation7"));

		dataSource = target.getTableSetsByPos(1);
		Assert.assertEquals(8, dataSource.getTableSets().size());
		Assert.assertTrue(dataSource.getTableSets().contains("UOD_Order_Operation0"));
		Assert.assertTrue(dataSource.getTableSets().contains("UOD_Order_Operation7"));
	}
}