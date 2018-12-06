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
import java.util.List;
import java.util.concurrent.Callable;

import com.dianping.zebra.group.util.DaoContextHolder;

public class PreparedStatementExecuteUpdateCallable implements Callable<UpdateResult> {

	private List<PreparedStatement> stmts;

	private String sqlName;

	protected TaskExecuteResult taskExecuteResult;

	public PreparedStatementExecuteUpdateCallable(List<PreparedStatement> stmts, String sqlName) {
		super();
		this.stmts = stmts;
		this.sqlName = sqlName;
	}

	@Override
	public UpdateResult call() throws Exception {
		try {
			DaoContextHolder.setSqlName(sqlName);

			int result = 0;

			for (PreparedStatement stmt : stmts) {
				result += stmt.executeUpdate();
			}

			return new UpdateResult(result);
		} finally {
			DaoContextHolder.clearSqlName();
		}
	}

	public TaskExecuteResult getTaskExecuteResult() {
		return taskExecuteResult;
	}

	public void setTaskExecuteResult(TaskExecuteResult taskExecuteResult) {
		this.taskExecuteResult = taskExecuteResult;
	}
}
