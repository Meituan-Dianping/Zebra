package com.dianping.zebra.shard.parser;

import org.junit.Test;

import junit.framework.Assert;

import java.util.HashSet;
import java.util.Set;

public class SQLHintTest {

	@Test
	public void test0() {
		String line = "";

		SQLHint hint = SQLHint.parseZebraHint(line);

		Assert.assertNotNull(hint);
	}

	@Test
	public void test1() {
		String line = "/*+zebra:w*/";

		SQLHint hint = SQLHint.parseZebraHint(line);

		Assert.assertEquals(true, hint.isForceMaster());
	}

	@Test
	public void test2() {
		String line = "/*+zebra:sk=UserId|w*/";

		SQLHint hint = SQLHint.parseZebraHint(line);

		Assert.assertEquals(true, hint.isForceMaster());
		Assert.assertEquals(buildColumnSet("UserId"), hint.getShardColumns());
	}

	@Test
	public void test3() {
		String line = "/*+zebra:sk=UserId*/";

		SQLHint hint = SQLHint.parseZebraHint(line);

		Assert.assertEquals(false, hint.isForceMaster());
		Assert.assertEquals(buildColumnSet("UserId"), hint.getShardColumns());
	}

	@Test
	public void test4() {
		String line = "/*+zebra:w|sk=UserId*/";

		SQLHint hint = SQLHint.parseZebraHint(line);

		Assert.assertEquals(true, hint.isForceMaster());
		Assert.assertEquals(buildColumnSet("UserId"), hint.getShardColumns());
	}

	@Test
	public void test5() {
		SQLParser.HintCommentHandler handler = new SQLParser.HintCommentHandler();

		handler.handle(null,"/*+zebra:w*/");
		handler.handle(null,"/*master*/");

		SQLHint hint = SQLHint.parseHint(handler);

		Assert.assertEquals(true, hint.isForceMaster());
		Assert.assertEquals("/*master*/", hint.getExtreHint());
	}

	@Test
	public void testMultiShardKey() {
		SQLHint hint = SQLHint.parseZebraHint("/*+zebra:w|sk=ColumnName1*/");
		Assert.assertEquals(true, hint.isForceMaster());
		Assert.assertEquals(buildColumnSet("ColumnName1"), hint.getShardColumns());

		hint = SQLHint.parseZebraHint("/*+zebra:sk=ColumnName1|w*/");
		Assert.assertEquals(true, hint.isForceMaster());
		Assert.assertEquals(buildColumnSet("ColumnName1"), hint.getShardColumns());

		hint = SQLHint.parseZebraHint("/*+zebra:sk=ColumnName1 + ColumnName2 */");
		Assert.assertEquals(false, hint.isForceMaster());
		Assert.assertEquals(buildColumnSet("ColumnName1", "ColumnName2"), hint.getShardColumns());

		hint = SQLHint.parseZebraHint("/*+zebra:sk=+ColumnName1 + ColumnName2+*/");
		Assert.assertEquals(false, hint.isForceMaster());
		Assert.assertEquals(buildColumnSet("ColumnName1", "ColumnName2"), hint.getShardColumns());
	}


	@Test
	public void testParseColumn() {
		Set<String> expected = SQLHint.parseShardColumns("col0");
		Assert.assertEquals(buildColumnSet("col0"), expected);

		expected = SQLHint.parseShardColumns("col0+col1");
		Assert.assertEquals(buildColumnSet("col0", "col1"), expected);

		expected = SQLHint.parseShardColumns("col0+column1");
		Assert.assertEquals(buildColumnSet("col0", "column1"), expected);

		expected = SQLHint.parseShardColumns("col0+column1+");
		Assert.assertEquals(buildColumnSet("col0", "column1"), expected);

		expected = SQLHint.parseShardColumns("+col0++");
		Assert.assertEquals(buildColumnSet("col0"), expected);

		expected = SQLHint.parseShardColumns("");
		Assert.assertEquals(buildColumnSet(), expected);

		expected = SQLHint.parseShardColumns("+");
		Assert.assertEquals(buildColumnSet(), expected);

	}


	@Test
	public void testTableParallel() {
		String line = "/*+zebra:w*/";
		SQLHint hint = SQLHint.parseZebraHint(line);
		testHint(hint, true, 0);

		line = "/*+zebra:cl=1*/";
		hint = SQLHint.parseZebraHint(line);
		testHint(hint, false, 1);

		line = "/*+zebra:w|cl=1*/";
		hint = SQLHint.parseZebraHint(line);
		testHint(hint, true, 1);

		line = "/*+zebra:cl=1|w*/";
		hint = SQLHint.parseZebraHint(line);
		testHint(hint, true, 1);

		line = "/*+zebra:cl=3|sk=Id+Uid|w*/";
		hint = SQLHint.parseZebraHint(line);
		testHint(hint, true, 3, buildColumnSet("Id","Uid"));
		Assert.assertEquals(buildColumnSet("Id","Uid"), hint.getShardColumns());

		line = "/*+zebra:w|cl=4|sk=Id+Uid*/";
		hint = SQLHint.parseZebraHint(line);
		testHint(hint, true, 4, buildColumnSet("Id","Uid"));

		line = "/*+zebra:w|sk=Id+Uid|cl=9*/";
		hint = SQLHint.parseZebraHint(line);
		testHint(hint, true, 9, buildColumnSet("Id","Uid"));
	}

	private void testHint(SQLHint hint, boolean w, int cl) {
		Assert.assertEquals(w, hint.isForceMaster());
		Assert.assertEquals(cl, hint.getConcurrencyLevel());
	}

	private void testHint(SQLHint hint, boolean w, int cl, Set<String> sks) {
		testHint(hint, w, cl);
		Assert.assertEquals(sks, hint.getShardColumns());
	}


	private Set<String> buildColumnSet(String... cols) {
		Set<String> colSet = new HashSet<String>();
		for(String colName: cols) {
			colSet.add(colName);
		}
		return colSet;
	}

}
