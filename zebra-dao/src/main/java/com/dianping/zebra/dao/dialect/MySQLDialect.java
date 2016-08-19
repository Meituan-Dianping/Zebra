package com.dianping.zebra.dao.dialect;

import java.util.List;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.dianping.zebra.dao.plugin.page.MysqlCountOutputVisitor;

/**
 * 
 * @author damonzhu
 *
 */
public class MySQLDialect extends Dialect {

	@Override
	public String getCountSql(String sql) {
		SQLStatementParser parser = new MySqlStatementParser(sql);
		List<SQLStatement> stmtList = parser.parseStatementList();

		// 将AST通过visitor输出
		StringBuilder out = new StringBuilder();
		MysqlCountOutputVisitor visitor = new MysqlCountOutputVisitor(out);

		for (SQLStatement stmt : stmtList) {
			if (stmt instanceof SQLSelectStatement) {
				stmt.accept(visitor);
				out.append(";");
			}
		}

		return out.toString();
	}

	@Override
	public String getLimitString(String sql, int offset, String offsetPlaceholder, int limit, String limitPlaceholder) {
		if (offset > 0) {
			return sql + " limit " + offsetPlaceholder + "," + limitPlaceholder;
		} else {
			return sql + " limit " + limitPlaceholder;
		}
	}
}