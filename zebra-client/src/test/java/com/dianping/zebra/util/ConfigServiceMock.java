package com.dianping.zebra.util;

import com.dianping.zebra.config.ConfigService;

import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dozer @ 2015-02
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ConfigServiceMock implements ConfigService {
    private final Map<String, String> configs = new ConcurrentHashMap<String, String>();

    @Override
    public void init(Map<String, Object> configs) {

    }

    @Override
    public String getProperty(String key) {
        String result = configs.get(key);
        System.out.println(String.format("Read Config: key = %s  value = %s", key, result));
        return result;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {

    }

    public ConfigServiceMock addProperty(String key, String value) {
        configs.put(key, value);
        return this;
    }

	@Override
   public void destroy() {
	   // TODO Auto-generated method stub
	   
   }

	@Override
   public void removePropertyChangeListener(PropertyChangeListener listener) {
	   // TODO Auto-generated method stub
	   
   }
}
