package com.dianping.zebra.group.config;

import com.dianping.zebra.group.config.system.entity.SystemConfig;

import java.beans.PropertyChangeListener;

public interface SystemConfigManager {
	public void init();

	public void addListerner(PropertyChangeListener listener);

	public SystemConfig getSystemConfig();
}
