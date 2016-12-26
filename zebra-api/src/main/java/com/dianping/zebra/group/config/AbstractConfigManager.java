package com.dianping.zebra.group.config;

import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.log.LoggerLoader;
import com.dianping.zebra.util.StringUtils;
import org.apache.logging.log4j.Logger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractConfigManager {

	protected static final Logger logger = LoggerLoader.getLogger(AbstractConfigManager.class);

	protected final ConfigService configService;

	protected final InnerPropertyChangeListener innerPropertyChangeListener;

	protected List<PropertyChangeListener> listeners = new CopyOnWriteArrayList<PropertyChangeListener>();

	public AbstractConfigManager(ConfigService configService) {
		this.configService = configService;
		this.innerPropertyChangeListener = new InnerPropertyChangeListener();
		this.configService.addPropertyChangeListener(this.innerPropertyChangeListener);
	}

	public void close() {
		configService.removePropertyChangeListener(innerPropertyChangeListener);
	}

	//TODO T getProperty(String key);

	public boolean getProperty(String key, boolean defaultValue) {
		String value = configService.getProperty(key);

		if ("true".equalsIgnoreCase(value)) {
			return true;
		} else if ("false".equalsIgnoreCase(value)) {
			return false;
		}

		return defaultValue;
	}

	protected int getProperty(String key, int defaultValue) {
		String value = configService.getProperty(key);

		if (StringUtils.isNotBlank(value)) {
			return Integer.parseInt(value);
		} else {
			return defaultValue;
		}
	}

	protected long getProperty(String key, long defaultValue) {
		String value = configService.getProperty(key);

		if (StringUtils.isNotBlank(value)) {
			return Long.parseLong(value);
		} else {
			return defaultValue;
		}
	}

	protected String getProperty(String key, String defaultValue) {
		String value = configService.getProperty(key);

		if (StringUtils.isNotBlank(value)) {
			return value;
		} else {
			return defaultValue;
		}
	}

	private void notifyListeners(final PropertyChangeEvent evt) {
		for (final PropertyChangeListener listener : listeners) {
			listener.propertyChange(evt);
		}
	}

	protected abstract void onPropertyUpdated(PropertyChangeEvent evt);

	class InnerPropertyChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			try {
				onPropertyUpdated(evt);
				notifyListeners(evt);
			} catch (Exception e) {
				logger.error("fail to update property, apply old config!", e);
			}
		}
	}
}
