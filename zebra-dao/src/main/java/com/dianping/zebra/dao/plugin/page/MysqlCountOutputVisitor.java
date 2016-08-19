package com.dianping.zebra.dao.plugin.page;

import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;

/**
 * @author damonzhu
 *
 */
public class MysqlCountOutputVisitor extends MySqlOutputVisitor {

	public MysqlCountOutputVisitor(Appendable appender) {
		super(appender);
	}
	
	public boolean visit(MySqlSelectQueryBlock x) {
        if (x.getOrderBy() != null) {
            x.getOrderBy().setParent(x);
        }

        print0(ucase ? "SELECT COUNT(*) " : "select count(*) ");

        if (x.getFrom() != null) {
            println();
            print0(ucase ? "FROM " : "from ");
            x.getFrom().accept(this);
        }

        if (x.getWhere() != null) {
            println();
            print0(ucase ? "WHERE " : "where ");
            x.getWhere().setParent(x);
            x.getWhere().accept(this);
        }

        if (x.getGroupBy() != null) {
            println();
            x.getGroupBy().accept(this);
        }

        if (x.getOrderBy() != null) {
            println();
            x.getOrderBy().accept(this);
        }

        return false;
    }

}
