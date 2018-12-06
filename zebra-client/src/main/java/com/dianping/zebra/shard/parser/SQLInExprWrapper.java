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
package com.dianping.zebra.shard.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLListExpr;
import com.alibaba.druid.sql.ast.expr.SQLValuableExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;

import java.util.*;

public class SQLInExprWrapper {
	private Set<String> shardColumns = new HashSet<String>();

	// SQLValuableExpr, SQLVariantRefExpr or SQLListExpr
	private final SQLExpr sqlExpr;

	private int hash;

	private boolean initHash;

	private boolean initValueRefSet;

	private Set<Integer> valueRefSet = new LinkedHashSet<Integer>();

	public SQLInExprWrapper(SQLExpr sqlExpr) {
		this.sqlExpr = sqlExpr;
	}

	public SQLInExprWrapper(String shardColumn, SQLExpr sqlExpr) {
		this.sqlExpr = sqlExpr;
		this.shardColumns.add(shardColumn);
	}

	public SQLInExprWrapper(Set<String> shardColumns, SQLExpr sqlExpr) {
		this.sqlExpr = sqlExpr;
		if (shardColumns != null) {
			this.shardColumns = shardColumns;
		}
	}

	public Set<Integer> getValueRefSet() {
		if (!initValueRefSet) {
			if (sqlExpr instanceof SQLVariantRefExpr) {
				valueRefSet.add(((SQLVariantRefExpr) sqlExpr).getIndex());
			} else if (sqlExpr instanceof SQLListExpr) {
				List<SQLExpr> items = ((SQLListExpr) sqlExpr).getItems();
				if (items != null) {
					for (SQLExpr expr : items) {
						if (expr instanceof SQLVariantRefExpr) {
							valueRefSet.add(((SQLVariantRefExpr) expr).getIndex());
						}
					}
				}
			}
			initValueRefSet = true;
		}
		if (valueRefSet == null) {
			valueRefSet = new HashSet<Integer>();
		}
		return valueRefSet;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		SQLInExprWrapper wrapper = (SQLInExprWrapper) obj;
		if (sqlExpr == wrapper.sqlExpr) {
			return true;
		}

		if (sqlExpr != null && wrapper.sqlExpr != null) {
			if (sqlExpr.getClass() != wrapper.sqlExpr.getClass()) {
				return false;
			}
			if (sqlExpr instanceof SQLVariantRefExpr && wrapper.sqlExpr instanceof SQLVariantRefExpr) {
				SQLVariantRefExpr ref1 = (SQLVariantRefExpr) sqlExpr;
				SQLVariantRefExpr ref2 = (SQLVariantRefExpr) wrapper.sqlExpr;
				return (ref1.equals(ref2) && ref1.getIndex() == ref2.getIndex());
			} else if (sqlExpr instanceof SQLListExpr && wrapper.sqlExpr instanceof SQLListExpr) {
				List<SQLExpr> list1 = ((SQLListExpr) sqlExpr).getItems();
				List<SQLExpr> list2 = ((SQLListExpr) wrapper.sqlExpr).getItems();
				if (list1 == list2) {
					return true;
				} else if (list1 != null && list2 != null) {
					Iterator<SQLExpr> it1 = list1.iterator();
					Iterator<SQLExpr> it2 = list2.iterator();
					while (it1.hasNext() && it2.hasNext()) {
						SQLExpr expr1 = it1.next();
						SQLExpr expr2 = it2.next();
						if (expr1 == expr2) {
							continue;
						} else if (expr1 != null && expr2 != null) {
							if (expr1 instanceof SQLVariantRefExpr && expr2 instanceof SQLVariantRefExpr) {
								return expr1.equals(expr2)
										&& (((SQLVariantRefExpr) expr1).getIndex() == ((SQLVariantRefExpr) expr2).getIndex());
							}
							return expr1.equals(expr2);
						} else {
							return false;
						}
					}
					return !(it1.hasNext() || it2.hasNext());
				} else {
					return false;
				}
			} else {
				return sqlExpr.equals(wrapper.sqlExpr);
			}
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		if (!initHash) {
			if (sqlExpr instanceof SQLValuableExpr) {
				hash = sqlExpr.hashCode();
			} else if (sqlExpr instanceof SQLVariantRefExpr) {
				SQLVariantRefExpr ref = (SQLVariantRefExpr) sqlExpr;
				hash = ref.hashCode() * 31 + ref.getIndex();
				valueRefSet.add(ref.getIndex());
			} else if (sqlExpr instanceof SQLListExpr) {
				SQLListExpr listExpr = (SQLListExpr) sqlExpr;
				List<SQLExpr> items = listExpr.getItems();
				hash = 1;
				if (items != null) {
					for (SQLExpr expr : items) {
						if (expr instanceof SQLVariantRefExpr) {
							hash = (hash * 31 + expr.hashCode()) * 31 + ((SQLVariantRefExpr) expr).getIndex();
							valueRefSet.add(((SQLVariantRefExpr) expr).getIndex());
						} else {
							hash = hash * 31 + expr.hashCode();
						}
					}
				}
			}
			initValueRefSet = true;
			initHash = true;
		}
		return hash;
	}
}