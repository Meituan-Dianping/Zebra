package com.dianping.zebra.config;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.lion.client.LionException;
import com.dianping.zebra.Constants;
import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.exception.ZebraTransportException;
import com.dianping.zebra.group.config.AdvancedPropertyChangeEvent;
import com.dianping.zebra.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by root on 16-12-22.
 */
public class LionConfigService implements ConfigService {

    protected static final Logger logger = LoggerLoader.getLogger(LionConfigService.class);

    private static volatile LionConfigService configService;

    private List<PropertyChangeListener> listeners = new CopyOnWriteArrayList<PropertyChangeListener>();

    private ConfigChange configChange;

    private LionConfigService() {

    }

    public static LionConfigService getInstance() {
        if (configService == null) {
            synchronized (LionConfigService.class) {
                if (configService == null) {
                    configService = new LionConfigService();
                    configService.init();
                }
            }
        }
        return configService;
    }

    @Override
    public void init() {
        try {
            configChange = new ConfigChange() {
                @Override
                public void onChange(String key, String value) {
                    logger.info(String.format("Receive Lion change notification: key[%s],value[%s]",key,value));
                    final PropertyChangeEvent event = new AdvancedPropertyChangeEvent(this, key, null, value);
                    for (PropertyChangeListener listener : listeners) {
                        listener.propertyChange(event);
                    }
                }
            };
            ConfigCache.getInstance().addChange(configChange);
        } catch (LionException le) {
            logger.error("fail to initilize Remote Config Manager for DAL", le);
            throw new ZebraConfigException(le);
        }
    }

    @Override
    public void destroy() {
        if (configChange != null) {
            try {
                ConfigCache.getInstance().removeChange(configChange);
            } catch (LionException e) {
                logger.warn("fail to destroy Remote Config Manager for DAL", e);
            } catch (Throwable e) {
                logger.warn("Please Update lion-client version up to 2.4.8", e);
            }
        }
    }

    @Override
    public String getProperty(String key) {
        try {
            String value = ConfigCache.getInstance().getProperty(Constants.DEFAULT_DATASOURCE_LION_PREFIX+key);
            return value == null? null : value.trim();
        } catch (LionException le) {
            throw new ZebraTransportException(String.format("fail to get key:[%s] from Lion",key), le);
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }
}
