/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.zebra.dao.plugin.page;

import com.alibaba.druid.sql.ast.SQLSetQuantifier;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;

import java.util.List;

/**
 * @author damonzhu
 *
 */
public class MysqlCountOutputVisitor extends MySqlOutputVisitor {

	private boolean subSelect = false;

	public MysqlCountOutputVisitor(Appendable appender) {
		super(appender);
	}

	public boolean visit(MySqlSelectQueryBlock x) {
		if (x.getOrderBy() != null) {
			x.getOrderBy().setParent(x);
		}

		boolean rewriteDistinct = false;
		if (x.getSelectList() != null) {
			rewriteDistinct = visitSelectItems(x.getSelectList(), SQLSetQuantifier.DISTINCT == x.getDistionOption());
		}

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

		if (rewriteDistinct) {
			print0(") ZebraDaoDistinctTable");
		}

		return false;
	}

	private boolean visitSelectItems(List<SQLSelectItem> selectItems, boolean distinct) {
		boolean rewriteDistinct = false;
		if (this.subSelect) {
			// sub select
			print0(ucase ? "SELECT " : "select ");
			if (distinct) {
				print0(ucase ? "DISTINCT " : "distinct ");
			}
			printSelectItems(selectItems);
		} else {
			if (distinct) {
				// select distinct a,b,... from xxx
				print0(ucase ? "SELECT COUNT(*) FROM (SELECT DISTINCT " : "select count(*) from (select distinct ");
				printSelectItems(selectItems);
				rewriteDistinct = true;
			} else {
				// normal select
				print0(ucase ? "SELECT COUNT(*) " : "select count(*) ");
			}
		}
		this.subSelect = true;
		return rewriteDistinct;
	}

	private void printSelectItems(List<SQLSelectItem> selectItems) {
		for (int i = 0; i < selectItems.size(); ++i) {
			SQLSelectItem item = selectItems.get(i);
			if (i > 0) {
				print0(",");
			}
			item.accept(this);
		}
	}

}
