package com.dianping.zebra.shard.parser;

import java.util.List;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dianping.zebra.shard.exception.ShardParseException;

import junit.framework.Assert;

public class SQLParserResultTest {

	private long begin = 0L;

	@BeforeClass
	public static void init() {
		SQLParser.init();
	}

	@Before
	public void setup() {
		begin = System.currentTimeMillis();
	}

	@After
	public void after() {
		System.out.println(System.currentTimeMillis() - begin);
	}

	@Test
	public void testTableSetsForSelect1() throws ShardParseException {
		SQLParsedResult result = SQLParser
				.parseWithCache("/*+zebra:w*/select a,b from db where `c` in (select d from db2 where ss = '1');");

		Assert.assertEquals(2, result.getRouterContext().getTableSet().size());
		Assert.assertEquals(true, result.getRouterContext().getTableSet().contains("db"));
	}

	@Test
	public void testTableSetsForSelect2() throws ShardParseException {
		SQLParsedResult result = SQLParser
				.parseWithCache("/*+zebra:w*/select a,b from `db` where `c` in (select d from db2 where ss = '1');");

		Assert.assertEquals(2, result.getRouterContext().getTableSet().size());
		Assert.assertEquals(true, result.getRouterContext().getTableSet().contains("db"));
	}

	@Test
	public void testTableSetsForMin() throws ShardParseException {
		SQLParsedResult result = SQLParser.parseWithCache("/*+zebra:w*/select min(a),b from db where `c` = 1;");

		Assert.assertEquals(1, result.getRouterContext().getTableSet().size());
		Assert.assertEquals(true, result.getRouterContext().getTableSet().contains("db"));
	}

	@Test
	public void testTableSetsForSelectWithLimit() throws ShardParseException {
		SQLParsedResult result = SQLParser
				.parseWithCache("/*+zebra:w*/select a,b from db where `c` = 1 and `d` = 2 or `a` = ? limit 10,100");

		Assert.assertEquals(100, result.getMergeContext().getLimit());
		Assert.assertEquals(10, result.getMergeContext().getOffset());
		Assert.assertNull(result.getMergeContext().getOrderBy());
	}

	@Test
	public void testTableSetsForSelectWithOrderBy() throws ShardParseException {
		SQLParsedResult result = SQLParser.parseWithCache(
				"/*+zebra:w*/select a,b from db where `c` = 1 and `d` = 2 or `a` = ? Order by c,d desc");

		Assert.assertNotNull(result.getMergeContext().getOrderBy());
	}

	@Test
	public void testTableSetsForSelectWithDistinct() throws ShardParseException {
		SQLParsedResult result = SQLParser.parseWithCache(
				"/*+zebra:w*/select distinct a,b from db where `c` = 1 and `d` = 2 or `a` = ? Order by c,d desc");

		Assert.assertEquals(true, result.getMergeContext().isDistinct());
	}

	@Test
	public void testTableSetsForSelectWithGroupby() throws ShardParseException {
		SQLParsedResult result = SQLParser.parseWithCache("/*+zebra:w*/select a,b from db where `c` = 1 group by c,d");

		Assert.assertEquals(2, result.getMergeContext().getGroupByColumns().size());
	}

	@Test
	public void testTableSetsForUpdate() throws ShardParseException {
		SQLParsedResult result = SQLParser.parseWithCache("update a set `a` = 1 where `b`=1;");

		Assert.assertEquals(1, result.getRouterContext().getTableSet().size());
		Assert.assertEquals(true, result.getRouterContext().getTableSet().contains("a"));
	}

	@Test
	public void testTableSetsForDelete() throws ShardParseException {
		SQLParsedResult result = SQLParser.parseWithCache("delete from a where `a`=1;");

		Assert.assertEquals(1, result.getRouterContext().getTableSet().size());
		Assert.assertEquals(true, result.getRouterContext().getTableSet().contains("a"));
	}

	@Test
	public void testTableSetsForInsert() throws ShardParseException {
		SQLParsedResult result = SQLParser.parseWithCache(
				"INSERT INTO DP_GroupNoteScoreLog (UserID, NoteType, NoteID, Score, Comment, AddDate) VALUES (?, ?, ?, ?, ?, NOW());");

		Assert.assertEquals(1, result.getRouterContext().getTableSet().size());
		Assert.assertEquals(true, result.getRouterContext().getTableSet().contains("DP_GroupNoteScoreLog"));
	}

	@Test
	public void testTableSetsForInsertValues() throws ShardParseException {
		SQLParsedResult result = SQLParser.parseWithCache(
				"INSERT INTO DP_GroupNoteScoreLog (UserID, NoteType, NoteID, Score, Comment, AddDate) VALUES (?, 1212, ?, ?, ?, NOW());");

		MySqlInsertStatement stmt = (MySqlInsertStatement) result.getStmt();
		SQLInsertStatement.ValuesClause vc = stmt.getValues();
		System.out.println(vc);
		List<SQLExpr> values = vc.getValues();
		for (SQLExpr sqlExpr : values) {
			System.out.println("Type : " + sqlExpr.getClass().getSimpleName() + " value = " + sqlExpr.toString());
		}
		System.out.println(stmt.getValuesList());

		result = SQLParser.parseWithCache(
				"INSERT INTO DP_GroupNoteScoreLog (UserID, NoteType, NoteID, Score, Comment, AddDate) VALUES (?, ?, ?, ?, ?, NOW()),(?, ?, ?, ?, ?, NOW());");

		stmt = (MySqlInsertStatement) result.getStmt();
		System.out.println(stmt.getValues());
		System.out.println(stmt.getValuesList());
	}

	@Test
	public void performanceTest() {
		for (int i = 0; i < 10; i++) {
			long now = System.currentTimeMillis();
			SQLParser.parseWithCache("/*+zebra:w*/select min(a),b from db where `c` = " + i + ";");
			long cost = System.currentTimeMillis() - now;
			System.out.println(cost);
		}
	}

	@Test
	public void testTableSetsForReplace() throws ShardParseException {
		SQLParsedResult result = SQLParser.parseWithCache("REPLACE INTO tb (UserID, NoteType, NoteID) VALUES (?, ?, ?);");
		Assert.assertEquals(1, result.getRouterContext().getTableSet().size());
		Assert.assertEquals(true, result.getRouterContext().getTableSet().contains("tb"));

		result = SQLParser.parseWithCache("REPLACE INTO tb (UserID, NoteType, NoteID) VALUES (?, ?, ?), (?, ?, ?);");
		Assert.assertEquals(1, result.getRouterContext().getTableSet().size());
		Assert.assertEquals(true, result.getRouterContext().getTableSet().contains("tb"));

		result = SQLParser.parseWithCache("REPLACE INTO tb Set UserID = 1, NoteType = 2, NoteID = 3;");
		Assert.assertEquals(1, result.getRouterContext().getTableSet().size());
		Assert.assertEquals(true, result.getRouterContext().getTableSet().contains("tb"));

		result = SQLParser.parseWithCache("REPLACE INTO tb (UserID, NoteType, NoteID) SELECT UserID, NoteType, NoteID FROM tb WHERE UserID = 1;");
		Assert.assertEquals(1, result.getRouterContext().getTableSet().size());
		Assert.assertEquals(true, result.getRouterContext().getTableSet().contains("tb"));
	}

}
