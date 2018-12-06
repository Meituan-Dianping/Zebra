package com.dianping.zebra.shard.parser;

import org.junit.Test;

import com.dianping.zebra.shard.exception.ShardParseException;

import junit.framework.Assert;

import java.util.HashMap;
import java.util.Map;

public class SQLRewriteTest {

	@Test
	public void test() throws ShardParseException {
		DefaultSQLRewrite rewriter = new DefaultSQLRewrite();
		SQLParsedResult result = SQLParser
				.parseWithCache("/*+zebra:w*/select a,b /*comment**/ from db where `c` = 1 limit 10,100 #this is comment");

		String newSql = rewriter.rewrite(result, "db", "db1");

		Assert.assertEquals("/*+zebra:w*/", result.getRouterContext().getSqlhint().getForceMasterComment());
		Assert.assertEquals("SELECT a, b\nFROM db1\nWHERE `c` = 1\nLIMIT 0, 110", newSql);
	}

	@Test
	public void test1() throws ShardParseException {
		DefaultSQLRewrite rewriter = new DefaultSQLRewrite();
		SQLParsedResult result = SQLParser
				.parseWithCache("/*+zebra:w*/select `a`,`b` /*comment**/ from `db` where `c` = 1 limit 10,100 #this is comment");

		String newSql = rewriter.rewrite(result, "db", "db1");

		System.out.println(newSql);
		Assert.assertEquals("/*+zebra:w*/", result.getRouterContext().getSqlhint().getForceMasterComment());
		Assert.assertEquals("SELECT `a`, `b`\nFROM `db1`\nWHERE `c` = 1\nLIMIT 0, 110", newSql);
	}

	@Test
	public void testNoComment() throws ShardParseException {
		DefaultSQLRewrite rewriter = new DefaultSQLRewrite();
		SQLParsedResult result = SQLParser
				.parseWithCache("select a,b /*comment**/ from db where `c` = 1 limit 10,100 #this is comment");

		String newSql = rewriter.rewrite(result, "db", "db1");

		Assert.assertNotNull(result.getRouterContext().getSqlhint());
		Assert.assertEquals("SELECT a, b\nFROM db1\nWHERE `c` = 1\nLIMIT 0, 110", newSql);
	}

	@Test
	public void testLimit() throws ShardParseException {
		DefaultSQLRewrite rewriter = new DefaultSQLRewrite();
		SQLParsedResult result = SQLParser
				.parseWithCache("select a,b /*comment**/ from db where `c` = 1 limit 10 #this is comment");

		String newSql = rewriter.rewrite(result, "db", "db1");

		Assert.assertNotNull(result.getRouterContext().getSqlhint());
		Assert.assertEquals("SELECT a, b\nFROM db1\nWHERE `c` = 1\nLIMIT 10", newSql);
	}

	@Test
	public void testInsert() throws ShardParseException {
		DefaultSQLRewrite rewriter = new DefaultSQLRewrite();
		SQLParsedResult result = SQLParser
				.parseWithCache("INSERT INTO db (UserID, NoteType, NoteID, Score, Comment,AddDate) VALUES (?, ?, ?, ?, ?, NOW());");

		String newSql = rewriter.rewrite(result, "db", "db1");

		Assert.assertEquals("INSERT INTO db1 (UserID, NoteType, NoteID, Score, Comment\n\t, AddDate)\nVALUES (?, ?, ?, ?, ?\n\t, NOW())", newSql);
	}

	@Test
	public void testInsertIgnore() throws ShardParseException {
		DefaultSQLRewrite rewriter = new DefaultSQLRewrite();
		SQLParsedResult result = SQLParser
				.parseWithCache("INSERT Ignore INTO db (UserID, NoteType, NoteID, Score, Comment,AddDate) VALUES (?, ?, ?, ?, ?, NOW()), (?, ?, ?, ?, ?, NOW());");

		String newSql = rewriter.rewrite(result, "db", "db1");

		Assert.assertEquals("INSERT IGNORE INTO db1 (UserID, NoteType, NoteID, Score, Comment\n\t, AddDate)\nVALUES (?, ?, ?, ?, ?\n\t\t, NOW()),\n\t(?, ?, ?, ?, ?\n\t\t, NOW())", newSql);
	}

	@Test
	public void testLimit2() throws ShardParseException {
		DefaultSQLRewrite rewriter = new DefaultSQLRewrite();
		SQLParsedResult result = SQLParser
				.parseWithCache("select a,b /*comment**/ from db where `c` = 1 limit 10 offset 10 #this is comment");

		String newSql = rewriter.rewrite(result, "db", "db1");

		Assert.assertNotNull(result.getRouterContext().getSqlhint());
		Assert.assertEquals(result.getMergeContext().getLimit(),10);
		Assert.assertEquals(result.getMergeContext().getOffset(),10);
		Assert.assertEquals("SELECT a, b\nFROM db1\nWHERE `c` = 1\nLIMIT 0, 20", newSql);
	}

	@Test
	public void testReplace1() throws ShardParseException {
		DefaultSQLRewrite reWriter = new DefaultSQLRewrite();
		SQLParsedResult result = SQLParser.parseWithCache("REPLACE INTO tb (UserID, NoteType, NoteID, Score, Comment,AddDate) VALUES (?, ?, ?, ?, ?, NOW());");
		String newSql = reWriter.rewrite(result, "tb", "tb1");
		Assert.assertEquals("REPLACE INTO tb1 (UserID, NoteType, NoteID, Score, Comment, AddDate)\nVALUES (?, ?, ?, ?, ?\n\t, NOW())", newSql);

		result = SQLParser.parseWithCache("REPLACE INTO tb (UserID, NoteType, NoteID, Score, Comment,AddDate) VALUES (?, ?, ?, ?, ?, NOW()), (?, ?, ?, ?, ?, NOW());");
		newSql = reWriter.rewrite(result, "tb", "tb1");
		Assert.assertEquals("REPLACE INTO tb1 (UserID, NoteType, NoteID, Score, Comment, AddDate)\nVALUES (?, ?, ?, ?, ?\n\t, NOW()), (?, ?, ?, ?, ?\n\t, NOW())", newSql);

		result = SQLParser.parseWithCache("REPLACE INTO tb (UserID, NoteType, NoteID, Score, Comment,AddDate) VALUES (1, 2, 3, 4, 'xxx', NOW())");
		newSql = reWriter.rewrite(result, "tb", "tb1");
		Assert.assertEquals("REPLACE INTO tb1 (UserID, NoteType, NoteID, Score, Comment, AddDate)\nVALUES (1, 2, 3, 4, 'xxx'\n\t, NOW())", newSql);
	}

	@Test
	public void testReplace2() throws ShardParseException {
		DefaultSQLRewrite reWriter = new DefaultSQLRewrite();
		SQLParsedResult result = SQLParser.parseWithCache("REPLACE INTO tb SET UserID = 1, NoteType = 2, NoteID = 3;");
		String newSql = reWriter.rewrite(result, "tb", "tb1");
		Assert.assertEquals("REPLACE INTO tb1 (UserID, NoteType, NoteID)\nVALUES (1, 2, 3)", newSql);
	}

	@Test
	public void testReplace3() throws ShardParseException {
		DefaultSQLRewrite reWriter = new DefaultSQLRewrite();
		SQLParsedResult result = SQLParser.parseWithCache("REPLACE INTO tb (UserID, NoteType, NoteID) SELECT UserID, NoteType, NoteID FROM tb WHERE UserID = 1");
		String newSql = reWriter.rewrite(result, "tb", "tb1");
		Assert.assertEquals("REPLACE INTO tb1 (UserID, NoteType, NoteID)\n\tSELECT UserID, NoteType, NoteID\n\tFROM tb1\n\tWHERE UserID = 1", newSql);
	}

	@Test
	public void testTableParser() throws ShardParseException {
//		String sql = "SELECT FN.FollowNoteID AS NoteID, N.GroupID, FN.UserID, CONCAT('RE:', N.NoteTitle) AS NoteTitle, '' AS NoteBody, FN.AddTime, FN.LastIP, G.GroupName, G.GroupPermaLink, FN.NoteID AS GroupNoteID FROM tb FN JOIN mb N ON N.NoteID = FN.NoteID JOIN gb G ON N.GroupID = G.GroupID WHERE FN.NoteClass = 3 GROUP BY FN.AddTime ORDER BY FN.AddTime LIMIT 0, 10";
//		String sql = "SELECT tb.LastName, mb.FirstName, mb.OrderNo FROM tb INNER JOIN mb ON tb.Id_P = mb.Id_P ORDER BY tb.LastName";
//		String sql = "SELECT id,mb.name FROM tb,mb WHERE id in (SELECT id FROM mb WHERE name=10)";
//		String sql = "INSERT INTO tb VALUES ('Gates', 'Bill', 'Xuanwumen 10', 'Beijing')";
//		String sql = "update tb set name = 'x' where id < 100 limit 10";
		String sql = "SELECT `tb`.`ID`, tb.`NAME` FROM `tb` WHERE ID = ? limit 2";
//		String sql = "delete from tb";
//		String sql = "SELECT tb.E_Name FROM tb UNION SELECT E_Name FROM mb";

		SQLParsedResult parserResult = SQLParser.parseWithCache(sql);
		Map<String,String> rewriteTableMapping = new HashMap<String,String>();
		rewriteTableMapping.put("tb", "_shadow_tb_");
		rewriteTableMapping.put("mb", "_shadow_mb_");
		rewriteTableMapping.put("gb", "_shadow_gb_");

		String result = new DefaultSQLRewrite().rewrite(parserResult.getStmt(),rewriteTableMapping);

		System.out.println(result);
//		Assert.assertEquals(result,"select * from _shadow_tb_ limit 10");

	}


	@Test
	public void testSelectForUpdate() throws ShardParseException {
		DefaultSQLRewrite rewriter = new DefaultSQLRewrite();
		SQLParsedResult result = SQLParser.parseWithCache("select a,b from db where `c` = 1 for update");
		String newSql = rewriter.rewrite(result, "db", "db1");
		Assert.assertNotNull(result.getRouterContext().getSqlhint());
		Assert.assertEquals("SELECT a, b\nFROM db1\nWHERE `c` = 1\nFOR UPDATE", newSql);

		result = SQLParser.parseWithCache("select a from db where `c` = ? for update");
		newSql = rewriter.rewrite(result, "db", "db0");
		Assert.assertNotNull(result.getRouterContext().getSqlhint());
		Assert.assertEquals("SELECT a\nFROM db0\nWHERE `c` = ?\nFOR UPDATE", newSql);
	}
}
