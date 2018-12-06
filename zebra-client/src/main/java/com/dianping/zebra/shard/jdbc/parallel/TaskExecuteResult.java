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

import java.util.ArrayList;
import java.util.List;

public class TaskExecuteResult {
	private String database;

	private List<String> sqls = new ArrayList<String>();

	private List<String> physicalTables = new ArrayList<String>();

	private boolean done;

	public TaskExecuteResult() {
	}

	public TaskExecuteResult(String database) {
		this.database = database;
	}

	public TaskExecuteResult(String database, List<String> physicalTables, List<String> sqls) {
		this.database = database;
		this.physicalTables = physicalTables;
		this.sqls = sqls;
	}

	public void addTableAndSql(String physicalTable, String sql) {
		this.physicalTables.add(physicalTable);
		this.sqls.add(sql);
	}

	public void addPhysicalTable(String physicalTable) {
		this.physicalTables.add(physicalTable);
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public List<String> getSqls() {
		return sqls;
	}

	public void setSqls(List<String> sqls) {
		this.sqls = sqls;
	}

	public List<String> getPhysicalTables() {
		return physicalTables;
	}

	public void setPhysicalTables(List<String> physicalTables) {
		this.physicalTables = physicalTables;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}
}
