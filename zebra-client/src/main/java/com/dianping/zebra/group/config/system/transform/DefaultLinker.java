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
package com.dianping.zebra.group.config.system.transform;

import java.util.ArrayList;
import java.util.List;

import com.dianping.zebra.group.config.system.entity.SqlFlowControl;
import com.dianping.zebra.group.config.system.entity.SystemConfig;

public class DefaultLinker implements ILinker {
	@SuppressWarnings("unused")
	private boolean m_deferrable;

	private List<Runnable> m_deferedJobs = new ArrayList<Runnable>();

	public DefaultLinker(boolean deferrable) {
		m_deferrable = deferrable;
	}

	public void finish() {
		for (Runnable job : m_deferedJobs) {
			job.run();
		}
	}

	@Override
	public boolean onSqlFlowControl(final SystemConfig parent, final SqlFlowControl sqlFlowControl) {
		parent.addSqlFlowControl(sqlFlowControl);
		return true;
	}
}
