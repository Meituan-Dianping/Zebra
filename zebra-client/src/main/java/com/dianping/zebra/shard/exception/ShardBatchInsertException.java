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
package com.dianping.zebra.shard.exception;

import com.dianping.zebra.exception.ZebraException;
import com.dianping.zebra.shard.jdbc.parallel.TaskExecuteResult;

import java.util.List;

public class ShardBatchInsertException extends ZebraException {

	private static final long serialVersionUID = 5563628861682988067L;

	private List<TaskExecuteResult> taskExecuteResults;

	public ShardBatchInsertException(List<TaskExecuteResult> taskExecuteResults) {
		super();
		this.taskExecuteResults = taskExecuteResults;
	}

	public ShardBatchInsertException(String message, List<TaskExecuteResult> taskExecuteResults) {
		super(message);
		this.taskExecuteResults = taskExecuteResults;
	}

	public ShardBatchInsertException(Throwable cause, List<TaskExecuteResult> taskExecuteResults) {
		super(cause);
		this.taskExecuteResults = taskExecuteResults;
	}

	public ShardBatchInsertException(String message, Throwable cause, List<TaskExecuteResult> taskExecuteResults) {
		super(message, cause);
		this.taskExecuteResults = taskExecuteResults;
	}

	public List<TaskExecuteResult> getTaskExecuteResults() {
		return taskExecuteResults;
	}

	public void setTaskExecuteResults(List<TaskExecuteResult> taskExecuteResults) {
		this.taskExecuteResults = taskExecuteResults;
	}
}
