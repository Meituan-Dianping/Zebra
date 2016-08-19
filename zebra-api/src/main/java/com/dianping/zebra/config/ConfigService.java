package com.dianping.zebra.config;

import java.beans.PropertyChangeListener;

public interface ConfigService {
	public void init();
	
	public void destroy();

	public String getProperty(String key);

	public void addPropertyChangeListener(PropertyChangeListener listener);
	
	public void removePropertyChangeListener(PropertyChangeListener listener);
}
