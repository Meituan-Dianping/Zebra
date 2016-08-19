package com.dianping.zebra.config;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RemoteConfigService implements ConfigService {
	
	private static RemoteConfigService configService;

	private List<PropertyChangeListener> listeners = new CopyOnWriteArrayList<PropertyChangeListener>();

	private RemoteConfigService() {
	}

	public static RemoteConfigService getInstance() {
		if (configService == null) {
			synchronized (RemoteConfigService.class) {
				if (configService == null) {
					configService = new RemoteConfigService();
					configService.init();
				}
			}
		}

		return configService;
	}

	//TODO customize this method to get config from remote
	@Override
	public String getProperty(String key) {
		return null;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.remove(listener);
	}

	//TODO customize this method
	@Override
	public void init() {
	}

	//TODO customize this method
	@Override
	public void destroy() {
	}
}
