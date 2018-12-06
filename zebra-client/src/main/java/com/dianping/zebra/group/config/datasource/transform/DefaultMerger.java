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
package com.dianping.zebra.group.config.datasource.transform;

import com.dianping.zebra.group.config.datasource.IEntity;
import com.dianping.zebra.group.config.datasource.IVisitor;
import com.dianping.zebra.group.config.datasource.entity.Any;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;

import java.util.Iterator;
import java.util.Stack;

public class DefaultMerger implements IVisitor {
	private Stack<Object> m_objs = new Stack();

	private GroupDataSourceConfig m_groupDataSourceConfig;

	public DefaultMerger() {
	}

	public DefaultMerger(GroupDataSourceConfig groupDataSourceConfig) {
		this.m_groupDataSourceConfig = groupDataSourceConfig;
		this.m_objs.push(groupDataSourceConfig);
	}

	public GroupDataSourceConfig getGroupDataSourceConfig() {
		return this.m_groupDataSourceConfig;
	}

	protected Stack<Object> getObjects() {
		return this.m_objs;
	}

	public <T> void merge(IEntity<T> to, IEntity<T> from) {
		this.m_objs.push(to);
		from.accept(this);
		this.m_objs.pop();
	}

	protected void mergeDataSourceConfig(DataSourceConfig to, DataSourceConfig from) {
		to.mergeAttributes(from);
		to.getProperties().addAll(from.getProperties());
		to.setTimeWindow(from.getTimeWindow());
		to.setPunishLimit(from.getPunishLimit());
		to.setJdbcUrl(from.getJdbcUrl());
		to.setUsername(from.getUsername());
		to.setDriverClass(from.getDriverClass());
		to.setPassword(from.getPassword());
		to.setWarmupTime(from.getWarmupTime());
	}

	protected void mergeGroupDataSourceConfig(GroupDataSourceConfig to, GroupDataSourceConfig from) {
		to.mergeAttributes(from);
	}

	public void visitAny(Any any) {
	}

	public void visitDataSourceConfig(DataSourceConfig from) {
		DataSourceConfig to = (DataSourceConfig) this.m_objs.peek();
		this.mergeDataSourceConfig(to, from);
		this.visitDataSourceConfigChildren(to, from);
	}

	protected void visitDataSourceConfigChildren(DataSourceConfig to, DataSourceConfig from) {
		to.getProperties().addAll(from.getProperties());
	}

	public void visitGroupDataSourceConfig(GroupDataSourceConfig from) {
		GroupDataSourceConfig to = (GroupDataSourceConfig) this.m_objs.peek();
		this.mergeGroupDataSourceConfig(to, from);
		this.visitGroupDataSourceConfigChildren(to, from);
	}

	protected void visitGroupDataSourceConfigChildren(GroupDataSourceConfig to, GroupDataSourceConfig from) {
		Iterator var3 = from.getDataSourceConfigs().values().iterator();

		while (var3.hasNext()) {
			DataSourceConfig source = (DataSourceConfig) var3.next();
			DataSourceConfig target = to.findDataSourceConfig(source.getId());
			if (target == null) {
				target = new DataSourceConfig(source.getId());
				to.addDataSourceConfig(target);
			}

			this.m_objs.push(target);
			source.accept(this);
			this.m_objs.pop();
		}

	}
}