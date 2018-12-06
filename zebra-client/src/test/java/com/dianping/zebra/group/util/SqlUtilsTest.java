package com.dianping.zebra.group.util;

import com.dianping.zebra.util.SqlType;
import com.dianping.zebra.util.SqlUtils;
import com.dianping.zebra.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SqlUtilsTest {

	private static final Pattern SELECT_FOR_UPDATE_PATTERN = Pattern.compile("^select\\s+.*\\s+for\\s+update.*$",
			Pattern.CASE_INSENSITIVE);

	@Test
	public void testComment() throws SQLException {
		String sql = "/*sdasdf*/select * from xx";
		SqlType sqlType = SqlUtils.getSqlType(sql);
		Assert.assertEquals(SqlType.SELECT, sqlType);
		Assert.assertEquals(true, sqlType.isRead());
	}


	@Test
	public void testComment2() throws SQLException {
		String sql = "/*sdasdf*/select * from /*123123*/ xx";
		SqlType sqlType = SqlUtils.getSqlType(sql);
		Assert.assertEquals(SqlType.SELECT, sqlType);
		Assert.assertEquals(true, sqlType.isRead());
	}

	@Test
	public void testComment3() throws SQLException {
		String sql = "/*sdasdf*/delete from xx";
		SqlType sqlType = SqlUtils.getSqlType(sql);
		Assert.assertEquals(SqlType.DELETE, sqlType);
		Assert.assertEquals(false, sqlType.isRead());
	}


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
		Assert.assertEquals(SqlType.UNKNOWN_SQL_TYPE, sqlType);
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
	public void testSelectFOrUpdate2() throws SQLException {
		String sql = "select \n \n id, begin_time, end_time, process, status, create_time, update_time from account_buffer_summary_task where id = ? for update";
		SqlType sqlType = SqlUtils.getSqlType(sql);

		Assert.assertEquals(SqlType.SELECT_FOR_UPDATE, sqlType);
		Assert.assertEquals(false, sqlType.isRead());
		Assert.assertEquals(true, sqlType.isWrite());
	}

	@Test
	public void testSelectFOrUpdate3() throws SQLException {
		String sql = "select \n \n id, begin_time, end_time, process, status, create_time, update_time from account_buffer_summary_task where id = ? FOR UPDATE";
		SqlType sqlType = SqlUtils.getSqlType(sql);

		Assert.assertEquals(SqlType.SELECT_FOR_UPDATE, sqlType);
		Assert.assertEquals(false, sqlType.isRead());
		Assert.assertEquals(true, sqlType.isWrite());
	}

	@Test
	public void testSelectFOrUpdate4() throws SQLException {
		String sql = "select \n \n id, \n begin_time, end_time, process, status, create_time, update_time from account_buffer_summary_task \n where id = ? FOR UPDATE \n";
		SqlType sqlType = SqlUtils.getSqlType(sql);

		Assert.assertEquals(SqlType.SELECT_FOR_UPDATE, sqlType);
		Assert.assertEquals(false, sqlType.isRead());
		Assert.assertEquals(true, sqlType.isWrite());
	}

	@Test
	public void testSelectFOrUpdate5() throws SQLException {
		String sql = "select \n \n id, begin_time, end_time, process, status, create_time, update_time from account_buffer_summary_task where id = ? for \n update";
		SqlType sqlType = SqlUtils.getSqlType(sql);

		Assert.assertEquals(SqlType.SELECT_FOR_UPDATE, sqlType);
		Assert.assertEquals(false, sqlType.isRead());
		Assert.assertEquals(true, sqlType.isWrite());
	}

	@Test
	public void testSelectFOrUpdate6() throws SQLException {
		String sql = "select \n \n id, `for`, `update`, process, status, create_time, update_time from account_buffer_summary_task where id = ?";
		SqlType sqlType = SqlUtils.getSqlType(sql);

		Assert.assertEquals(SqlType.SELECT, sqlType);
		Assert.assertEquals(true, sqlType.isRead());
		Assert.assertEquals(false, sqlType.isWrite());
	}

	@Test
	public void testSelectFOrUpdate7() throws SQLException {
		String sql = "select \n \n id, for, `update`, process, status, create_time, update_time from account_buffer_summary_task where id = ?";
		SqlType sqlType = SqlUtils.getSqlType(sql);

		Assert.assertEquals(SqlType.SELECT, sqlType);
		Assert.assertEquals(true, sqlType.isRead());
		Assert.assertEquals(false, sqlType.isWrite());
	}

	@Test
	public void testSelectFOrUpdate8() throws SQLException {
		String sql = "/*+zebra:w*/select\nID\nfrom/*god*/ TS_ExpenseBalance\n where BusinessCode = #expenseDTO.businessCode#\nFOR\nUPDATE;";
		SqlType sqlType = SqlUtils.getSqlType(sql);

		Assert.assertEquals(SqlType.SELECT_FOR_UPDATE, sqlType);
		Assert.assertEquals(false, sqlType.isRead());
		Assert.assertEquals(true, sqlType.isWrite());
	}

	@Test
	public void testSelectFOrUpdate9() throws SQLException {
		String sql = "/*+zebra:w*/select\nID\nfrom/*@@ideneity*/ TS_ExpenseBalance\n where BusinessCode = #expenseDTO.businessCode#\nFOR\nUPDATE;";
		SqlType sqlType = SqlUtils.getSqlType(sql);

		Assert.assertEquals(SqlType.SELECT_FOR_UPDATE, sqlType);
		Assert.assertEquals(false, sqlType.isRead());
		Assert.assertEquals(true, sqlType.isWrite());
	}

	@Test
	public void testSelectFOrUpdate10() throws SQLException {
		String sql = "/*+zebra:w*/select\nID\nfrom/*@@ideneity*/ TS_ExpenseBalance\n where BusinessCode = #expenseDTO.businessCode#\nFOR\nUPDATE";
		SqlType sqlType = SqlUtils.getSqlType(sql);

		Assert.assertEquals(SqlType.SELECT_FOR_UPDATE, sqlType);
		Assert.assertEquals(false, sqlType.isRead());
		Assert.assertEquals(true, sqlType.isWrite());
	}

	@Test
	public void testSelectFOrUpdate11() throws SQLException {
		String sql = "/*+zebra:w*/select\nID\nfrom/*@@ideneity*/ TS_ExpenseBalance\n where BusinessCode = #expenseDTO.businessCode# FOR UPDATE";
		SqlType sqlType = SqlUtils.getSqlType(sql);

		Assert.assertEquals(SqlType.SELECT_FOR_UPDATE, sqlType);
		Assert.assertEquals(false, sqlType.isRead());
		Assert.assertEquals(true, sqlType.isWrite());
	}

	@Test
	public void testSelect10() throws SQLException {
		String sql = "/*+zebra:w*/select\nID\nfrom/*for update*/ TS_ExpenseBalance\n where BusinessCode = #expenseDTO.businessCode#";
		SqlType sqlType = SqlUtils.getSqlType(sql);

		Assert.assertEquals(SqlType.SELECT, sqlType);
		Assert.assertEquals(true, sqlType.isRead());
		Assert.assertEquals(false, sqlType.isWrite());
	}

	@Test
	public void testUpdate2() throws SQLException {
		String sql = "update table where for = 1 set a = 1";
		SqlType sqlType = SqlUtils.getSqlType(sql);

		Assert.assertEquals(SqlType.UPDATE, sqlType);
		Assert.assertEquals(false, sqlType.isRead());
		Assert.assertEquals(true, sqlType.isWrite());
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

		String sql2 = "select last_insert_id();";
		SqlType sqlType2 = SqlUtils.getSqlType(sql2);
		Assert.assertEquals(SqlType.SELECT_FOR_IDENTITY, sqlType2);
		Assert.assertEquals(false, sqlType.isRead());
	}

	@Test
	public void testSelectIdentity1() throws SQLException {
		String sql = "select @@identity;";
		SqlType sqlType = SqlUtils.getSqlType(sql);
		Assert.assertEquals(SqlType.SELECT_FOR_IDENTITY, sqlType);
		Assert.assertEquals(false, sqlType.isRead());

		String sql2 = "select last_insert_id()";
		SqlType sqlType2 = SqlUtils.getSqlType(sql2);
		Assert.assertEquals(SqlType.SELECT_FOR_IDENTITY, sqlType2);
		Assert.assertEquals(false, sqlType.isRead());
	}

	@Test
	public void testbuildSqlType() throws SQLException {
		String sql = "SELECT CityID, CityName, Status, AddTime, UpdateTime FROM GP_BackCity WHERE Status=1";

		String type = SqlUtils.buildSqlType(sql);

		System.out.println(type);
	}

	@Test
	public void testbuildSqlType2() throws SQLException {
		String sql = "SELECT CategoryID, ParentCategoryID, CategoryOrderID, CityID, IsMain, AddDate FROM DP_CategoryTree";

		String type = SqlUtils.buildSqlType(sql);

		System.out.println(type);
	}

	@Test
	public void testPerformance() {
		List<String> sqls = new ArrayList<String>();
		sqls.add("SELECT CityID, CityName, Status, AddTime, UpdateTime FROM GP_BackCity WHERE Status=1");
		sqls.add("select @@identity;");
		sqls.add("INSERT into table select from table2");
		sqls.add("/*+zebra:w*/select\nID\nfrom/*for update*/ TS_ExpenseBalance\n where BusinessCode = #expenseDTO.businessCode#");
		sqls.add("/*+zebra:w*/select\nID\nfrom/*@@ideneity*/ TS_ExpenseBalance\n where BusinessCode = #expenseDTO.businessCode#\nFOR\nUPDATE");
		sqls.add("select \n \n id, \n begin_time, end_time, process, status, create_time, update_time from account_buffer_summary_task \n where id = ? FOR UPDATE \n");
		sqls.add("select pf.planid,p.processtype,p.status, pf.status as planstatus,p.updatetime from MRB_PlanFuture pf join MAD_Process p on pf.planid = p.subjectid and planid in (1307743) where p.updatetime >= '2017-06-27 00:00:00' and p.updatetime <= '2017-06-29 23:59:59' limit 1000;");
		sqls.add("select a.ownerId from TPDA_DealGroupExtend a,TPDA_DealProfit b where  a.DealGroupID=b.DealGroupID \n and a.ApprovalStatus='2' and b.saleProfit is null");
		sqls.add("select hp_cal_dt, count(*) from dprpt_platform_wide_source_active_d  where  hp_cal_dt  >= '2017-05-01' group by hp_cal_dt for update");

		long begin = System.currentTimeMillis();
		for (int i = 1; i < 1000000; ++i) {
			int rm = i % sqls.size();
			SqlUtils.getSqlType(sqls.get(rm));
		}
		System.out.println("new time:" + (System.currentTimeMillis() - begin));

		begin = System.currentTimeMillis();
		for (int i = 1; i < 1000000; ++i) {
			int rm = i % sqls.size();
			oldGetSqlType(sqls.get(rm));
		}
		System.out.println("old time:" + (System.currentTimeMillis() - begin));
	}

	@SuppressWarnings("deprecation")
	private SqlType oldGetSqlType(String sql) {
		SqlType sqlType = null;
		String noCommentsSql = sql;
		if (sql.trim().startsWith("/*")) {
			noCommentsSql = StringUtils.stripComments(sql, "'\"", "'\"", true, false, true, true).trim();
		}
		if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "select")) {
			String lowerCaseSql = noCommentsSql.toLowerCase();
			if (lowerCaseSql.contains(" for ") && SELECT_FOR_UPDATE_PATTERN.matcher(noCommentsSql).matches()) {
				sqlType = SqlType.SELECT_FOR_UPDATE;
			} else if (lowerCaseSql.contains("@@identity") || lowerCaseSql.contains("last_insert_id()")) {
				sqlType = SqlType.SELECT_FOR_IDENTITY;
			} else {
				sqlType = SqlType.SELECT;
			}
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "insert")) {
			sqlType = SqlType.INSERT;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "update")) {
			sqlType = SqlType.UPDATE;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "delete")) {
			sqlType = SqlType.DELETE;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "show")) {
			sqlType = SqlType.SHOW;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "replace")) {
			sqlType = SqlType.REPLACE;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "truncate")) {
			sqlType = SqlType.TRUNCATE;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "create")) {
			sqlType = SqlType.CREATE;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "drop")) {
			sqlType = SqlType.DROP;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "load")) {
			sqlType = SqlType.LOAD;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "merge")) {
			sqlType = SqlType.MERGE;
		} else if (noCommentsSql.toLowerCase().contains("call")) {
			sqlType = SqlType.EXECUTE;
		} else {
			sqlType = SqlType.UNKNOWN_SQL_TYPE;
		}
		return sqlType;
	}

	@Test
	public void keyWordTest() {
		Assert.assertEquals(SqlUtils.isStartWithKeyWord("set session group_concat_max_len=1024000",0, "select"), false);
		Assert.assertEquals(SqlUtils.isStartWithKeyWord("select * from test",0, "select"), true);
		Assert.assertEquals(SqlUtils.isStartWithKeyWord("select" ,0, "select"), true);
		Assert.assertEquals(SqlUtils.isStartWithKeyWord("set" ,0, "select"), false);
		Assert.assertEquals(SqlUtils.isStartWithKeyWord(" set" ,0, "select"), false);
		Assert.assertEquals(SqlUtils.isStartWithKeyWord(" select" ,0, "select"), false);
		Assert.assertEquals(SqlUtils.isStartWithKeyWord("select " ,0, "select"), true);
		Assert.assertEquals(SqlUtils.isStartWithKeyWord("select1" ,0, "select"), false);
		Assert.assertEquals(SqlUtils.isStartWithKeyWord("select1 " ,0, "select"), false);
	}
}
