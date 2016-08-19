package com.dianping.zebra.group.util;

import java.sql.SQLException;

import com.dianping.zebra.util.SqlType;
import com.dianping.zebra.util.SqlUtils;
import org.junit.Assert;
import org.junit.Test;

public class SqlUtilsTest {

	@Test
	public void testSelect() throws SQLException {
		String sql = "select * from xx";
		SqlType sqlType = SqlUtils.getSqlType(sql);
		Assert.assertEquals(SqlType.SELECT, sqlType);
		Assert.assertEquals(true, sqlType.isRead());
	}

	@Test
	public void testSelectUnion() throws SQLException {
		String sql = "select * from xx union select * from xx";
		SqlType sqlType = SqlUtils.getSqlType(sql);
		Assert.assertEquals(SqlType.SELECT, sqlType);
	}

	@Test
	public void testSelectUnionAll() throws SQLException {
		String sql = "(select * from xx) union all (select * from xx)";
		SqlType sqlType = SqlUtils.getSqlType(sql);
		Assert.assertEquals(SqlType.DEFAULT_SQL_TYPE, sqlType);
	}

	@Test
	public void testSelectFOrUpdate() throws SQLException {
		String sql = "select * from xx for update";
		SqlType sqlType = SqlUtils.getSqlType(sql);
		Assert.assertEquals(SqlType.SELECT_FOR_UPDATE, sqlType);
		Assert.assertEquals(false, sqlType.isRead());
	}
	
	@Test
	public void testSelectFOrUpdate1() throws SQLException {
		String sql = "SELECT type, value, addtime, updatetime, description FROM AC_SystemLock WHERE type=? AND value=? for update";
		SqlType sqlType = SqlUtils.getSqlType(sql);
		Assert.assertEquals(SqlType.SELECT_FOR_UPDATE, sqlType);
		Assert.assertEquals(false, sqlType.isRead());
	}

	@Test
	public void testUpdate() throws SQLException {
		String sql = "update table set xx=1";
		SqlType sqlType = SqlUtils.getSqlType(sql);
		Assert.assertEquals(SqlType.UPDATE, sqlType);
		Assert.assertEquals(false, sqlType.isRead());
	}

	@Test
	public void testInsert() throws SQLException {
		String sql = "INSERT into table select from table2";
		SqlType sqlType = SqlUtils.getSqlType(sql);
		Assert.assertEquals(SqlType.INSERT, sqlType);
		Assert.assertEquals(false, sqlType.isRead());
	}

	@Test
	public void testDelete() throws SQLException {
		String sql = "delete from table where id = 1";
		SqlType sqlType = SqlUtils.getSqlType(sql);
		Assert.assertEquals(SqlType.DELETE, sqlType);
		Assert.assertEquals(false, sqlType.isRead());
	}

	@Test
	public void testExecute() throws SQLException {
		String sql = "{call sp_proc}";
		SqlType sqlType = SqlUtils.getSqlType(sql);
		Assert.assertEquals(SqlType.EXECUTE, sqlType);
		Assert.assertEquals(false, sqlType.isRead());
	}

	@Test
	public void testSelectIdentity() throws SQLException {
		String sql = "select @@identity";
		SqlType sqlType = SqlUtils.getSqlType(sql);
		Assert.assertEquals(SqlType.SELECT_FOR_IDENTITY, sqlType);
		Assert.assertEquals(false, sqlType.isRead());

		String sql2 = "select last_insert_id()";
		SqlType sqlType2 = SqlUtils.getSqlType(sql2);
		Assert.assertEquals(SqlType.SELECT_FOR_IDENTITY, sqlType2);
		Assert.assertEquals(false, sqlType.isRead());
	}
	
	@Test
	public void testbuildSqlType() throws SQLException{
		String sql = "SELECT CityID, CityName, Status, AddTime, UpdateTime FROM GP_BackCity WHERE Status=1";
		
		String type = SqlUtils.buildSqlType(sql);
		
		System.out.println(type);
	}
	
	@Test
	public void testbuildSqlType2() throws SQLException{
		String sql = "SELECT CategoryID, ParentCategoryID, CategoryOrderID, CityID, IsMain, AddDate FROM DP_CategoryTree";
		
		String type = SqlUtils.buildSqlType(sql);
		
		System.out.println(type);
	}
	
	@Test
	public void testSqlCommnent() throws SQLException{
		String sql = "/*+zebra:w*/ SELECT CategoryID, ParentCategoryID, CategoryOrderID, CityID, IsMain, AddDate FROM DP_CategoryTree";
		
		String type = SqlUtils.parseSqlComment(sql);
		
		Assert.assertEquals("/*+zebra:w*/", type);
	}
	
	@Test
	public void testSqlCommnent2() throws SQLException{
		String sql = "    /*+zebra:w*/ SELECT CategoryID, ParentCategoryID, CategoryOrderID, CityID, IsMain, AddDate FROM DP_CategoryTree";
		
		String type = SqlUtils.parseSqlComment(sql);
		
		Assert.assertEquals("/*+zebra:w*/", type);
	}
	
	@Test
	public void testSqlCommnent3() throws SQLException{
		String sql = "  SELECT CategoryID, ParentCategoryID, CategoryOrderID, CityID, IsMain, AddDate FROM DP_CategoryTree";
		
		String type = SqlUtils.parseSqlComment(sql);
		
		Assert.assertNull("isnull", type);;
	}

}
