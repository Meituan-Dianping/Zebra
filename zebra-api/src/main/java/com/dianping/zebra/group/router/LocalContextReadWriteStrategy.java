package com.dianping.zebra.group.router;

import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;

public class LocalContextReadWriteStrategy implements ReadWriteStrategy {

	private static InheritableThreadLocal<Boolean> forceMaster = new InheritableThreadLocal<Boolean>();

	@Override
	public boolean shouldReadFromMaster() {
		Boolean shouldReadFromMaster = forceMaster.get();

		if (shouldReadFromMaster == null || !shouldReadFromMaster) {
			return false;
		} else {
			return true;
		}
	}

	protected static void setReadFromMaster() {
		forceMaster.set(true);
	}

	protected static void clearContext() {
		forceMaster.remove();
	}

	@Override
	public void setGroupDataSourceConfig(GroupDataSourceConfig config) {
	}
}
