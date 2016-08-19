package com.dianping.zebra.shard.parser;

import java.util.List;

import org.junit.Test;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement.ValuesClause;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.dianping.zebra.shard.exception.ShardParseException;

import junit.framework.Assert;

public class SQLParserResultTest {

	@Test
	public void testTableSetsForSelect() throws ShardParseException {
		SQLParsedResult result = SQLParser
				.parse("/*zebra:+w*/select a,b from db where `c` in (select d from db2 where ss = '1');");

		Assert.assertEquals(2, result.getRouterContext().getTableSet().size());
		Assert.assertEquals(true, result.getRouterContext().getTableSet().contains("db"));
	}

	@Test
	public void testTableSetsForMin() throws ShardParseException {
		SQLParsedResult result = SQLParser.parse("/*zebra:+w*/select min(a),b from db where `c` = 1;");

		Assert.assertEquals(1, result.getRouterContext().getTableSet().size());
		Assert.assertEquals(true, result.getRouterContext().getTableSet().contains("db"));
	}

	@Test
	public void testTableSetsForSelectWithLimit() throws ShardParseException {
		SQLParsedResult result = SQLParser
				.parse("/*zebra:+w*/select a,b from db where `c` = 1 and `d` = 2 or `a` = ? limit 10,100");

		Assert.assertEquals(100, result.getMergeContext().getLimit());
		Assert.assertEquals(10, result.getMergeContext().getOffset());
		Assert.assertNull(result.getMergeContext().getOrderBy());
	}

	@Test
	public void testTableSetsForSelectWithOrderBy() throws ShardParseException {
		SQLParsedResult result = SQLParser
				.parse("/*zebra:+w*/select a,b from db where `c` = 1 and `d` = 2 or `a` = ? Order by c,d desc");

		Assert.assertNotNull(result.getMergeContext().getOrderBy());
	}

	@Test
	public void testTableSetsForSelectWithDistinct() throws ShardParseException {
		SQLParsedResult result = SQLParser.parse(
				"/*zebra:+w*/select distinct a,b from db where `c` = 1 and `d` = 2 or `a` = ? Order by c,d desc");

		Assert.assertEquals(true, result.getMergeContext().isDistinct());
	}

	@Test
	public void testTableSetsForSelectWithGroupby() throws ShardParseException {
		SQLParsedResult result = SQLParser.parse("/*zebra:+w*/select a,b from db where `c` = 1 group by c,d");

		Assert.assertEquals(2, result.getMergeContext().getGroupByColumns().size());
	}

	@Test
	public void testTableSetsForUpdate() throws ShardParseException {
		SQLParsedResult result = SQLParser.parse("update a set `a` = 1 where `b`=1;");

		Assert.assertEquals(1, result.getRouterContext().getTableSet().size());
		Assert.assertEquals(true, result.getRouterContext().getTableSet().contains("a"));
	}

	@Test
	public void testTableSetsForDelete() throws ShardParseException {
		SQLParsedResult result = SQLParser.parse("delete from a where `a`=1;");

		Assert.assertEquals(1, result.getRouterContext().getTableSet().size());
		Assert.assertEquals(true, result.getRouterContext().getTableSet().contains("a"));
	}

	@Test
	public void testTableSetsForInsert() throws ShardParseException {
		SQLParsedResult result = SQLParser.parse(
				"INSERT INTO DP_GroupNoteScoreLog (UserID, NoteType, NoteID, Score, Comment, AddDate) VALUES (?, ?, ?, ?, ?, NOW());");

		Assert.assertEquals(1, result.getRouterContext().getTableSet().size());
		Assert.assertEquals(true, result.getRouterContext().getTableSet().contains("DP_GroupNoteScoreLog"));
	}

	@Test
	public void testTableSetsForInsertValues() throws ShardParseException {
		SQLParsedResult result = SQLParser.parse(
				"INSERT INTO DP_GroupNoteScoreLog (UserID, NoteType, NoteID, Score, Comment, AddDate) VALUES (?, 1212, ?, ?, ?, NOW());");

		MySqlInsertStatement stmt = (MySqlInsertStatement) result.getStmt();
		ValuesClause vc = stmt.getValues();
		System.out.println(vc);
		List<SQLExpr> values = vc.getValues();
		for (SQLExpr sqlExpr : values) {
			System.out.println("Type : " + sqlExpr.getClass().getSimpleName() + " value = " + sqlExpr.toString());
		}
		System.out.println(stmt.getValuesList());

		result = SQLParser.parse(
				"INSERT INTO DP_GroupNoteScoreLog (UserID, NoteType, NoteID, Score, Comment, AddDate) VALUES (?, ?, ?, ?, ?, NOW()),(?, ?, ?, ?, ?, NOW());");

		stmt = (MySqlInsertStatement) result.getStmt();
		System.out.println(stmt.getValues());
		System.out.println(stmt.getValuesList());
	}
	
}
