package com.dianping.zebra.group.config;

import java.beans.PropertyChangeEvent;

public class AdvancedPropertyChangeEvent extends PropertyChangeEvent {

	public AdvancedPropertyChangeEvent(Object source, String propertyName, Object oldValue, Object newValue) {
		super(source, propertyName, oldValue, newValue);
	}

	public Object getOldValue() {
		throw new UnsupportedOperationException();
	}
}
