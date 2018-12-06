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
package com.dianping.zebra.shard.router;

import java.util.HashSet;
import java.util.Set;

import com.dianping.zebra.shard.parser.SQLHint;

public class RouterContext {

	private Set<String> tableSet = new HashSet<String>();

	private SQLHint sqlhint;
	
	public Set<String> getTableSet() {
		return tableSet;
	}

	public void setTableSets(Set<String> tableSet) {
		this.tableSet = tableSet;
	}

	public SQLHint getSqlhint() {
		return sqlhint;
	}

	public void setSqlhint(SQLHint sqlhint) {
		this.sqlhint = sqlhint;
	}

	public void setTableSet(Set<String> tableSet) {
		this.tableSet = tableSet;
	}
}
