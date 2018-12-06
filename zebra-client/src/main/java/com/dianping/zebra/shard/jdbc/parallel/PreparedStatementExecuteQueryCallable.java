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
package com.dianping.zebra.shard.jdbc.parallel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.dianping.zebra.group.router.ZebraForceMasterHelper;
import com.dianping.zebra.group.util.DaoContextHolder;

public class PreparedStatementExecuteQueryCallable implements Callable<List<ResultSet>> {

	private List<PreparedStatement> stmts;
	private String sqlName;
	private boolean forceMaster;
	protected TaskExecuteResult taskExecuteResult;

	public PreparedStatementExecuteQueryCallable(List<PreparedStatement> stmts, String sqlName, boolean forceMaster) {
		super();
		this.stmts = stmts;
		this.sqlName = sqlName;
		this.forceMaster = forceMaster;
	}

	@Override
	public List<ResultSet> call() throws Exception {
		DaoContextHolder.setSqlName(sqlName);

		try {
			if (forceMaster) {
				ZebraForceMasterHelper.forceMasterInLocalContext();
			}

			List<ResultSet> rss = new ArrayList<ResultSet>();
			for (Statement stmt : stmts) {
				rss.add(((PreparedStatement) stmt).executeQuery());
			}
			return rss;
		} finally {
			DaoContextHolder.clearSqlName();
			if (forceMaster) {
				ZebraForceMasterHelper.clearLocalContext();
			}
		}
	}

	public TaskExecuteResult getTaskExecuteResult() {
		return taskExecuteResult;
	}

	public void setTaskExecuteResult(TaskExecuteResult taskExecuteResult) {
		this.taskExecuteResult = taskExecuteResult;
	}
}
