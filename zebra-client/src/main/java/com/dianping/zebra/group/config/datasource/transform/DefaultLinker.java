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

import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DefaultLinker implements ILinker {
	private boolean m_deferrable;
	private List<Runnable> m_deferedJobs = new ArrayList();

	public DefaultLinker(boolean deferrable) {
		this.m_deferrable = deferrable;
	}

	public void finish() {
		Iterator var1 = this.m_deferedJobs.iterator();

		while(var1.hasNext()) {
			Runnable job = (Runnable)var1.next();
			job.run();
		}

	}

	public boolean onDataSourceConfig(final GroupDataSourceConfig parent, final DataSourceConfig dataSourceConfig) {
		if (this.m_deferrable) {
			this.m_deferedJobs.add(new Runnable() {
				public void run() {
					parent.addDataSourceConfig(dataSourceConfig);
				}
			});
		} else {
			parent.addDataSourceConfig(dataSourceConfig);
		}

		return true;
	}
}
