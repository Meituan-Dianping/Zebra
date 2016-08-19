package com.dianping.zebra.group.router;

import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;

import java.util.ArrayList;
import java.util.List;

public class ReadWriteStrategyWrapper implements ReadWriteStrategy {
	private List<ReadWriteStrategy> items = new ArrayList<ReadWriteStrategy>();

	public void addStrategy(ReadWriteStrategy strategy) {
		items.add(strategy);
	}

	@Override
	public boolean shouldReadFromMaster() {
		for (ReadWriteStrategy strategy : items) {
			if (strategy.shouldReadFromMaster()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void setGroupDataSourceConfig(GroupDataSourceConfig config) {
		for (ReadWriteStrategy strategy : items) {
			strategy.setGroupDataSourceConfig(config);
		}
	}
}
