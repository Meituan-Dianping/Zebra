package com.dianping.zebra.group.config.datasource.transform;

import static com.dianping.zebra.group.config.datasource.Constants.ATTR_ACTIVE;
import static com.dianping.zebra.group.config.datasource.Constants.ATTR_CANREAD;
import static com.dianping.zebra.group.config.datasource.Constants.ATTR_CANWRITE;
import static com.dianping.zebra.group.config.datasource.Constants.ATTR_FILTERS;
import static com.dianping.zebra.group.config.datasource.Constants.ATTR_FORCE_WRITE_ON_LOGIN;
import static com.dianping.zebra.group.config.datasource.Constants.ATTR_ID;
import static com.dianping.zebra.group.config.datasource.Constants.ATTR_ROUTER_STRATEGY;
import static com.dianping.zebra.group.config.datasource.Constants.ATTR_TAG;
import static com.dianping.zebra.group.config.datasource.Constants.ATTR_TYPE;
import static com.dianping.zebra.group.config.datasource.Constants.ATTR_WEIGHT;

import org.xml.sax.Attributes;

import com.dianping.zebra.group.config.datasource.entity.Any;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;

public class DefaultSaxMaker implements IMaker<Attributes> {

   @Override
   public Any buildAny(Attributes attributes) {
      throw new UnsupportedOperationException("Not needed!");
   }

   @Override
   public DataSourceConfig buildDataSourceConfig(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String weight = attributes.getValue(ATTR_WEIGHT);
      String canRead = attributes.getValue(ATTR_CANREAD);
      String canWrite = attributes.getValue(ATTR_CANWRITE);
      String active = attributes.getValue(ATTR_ACTIVE);
      String type = attributes.getValue(ATTR_TYPE);
      String tag = attributes.getValue(ATTR_TAG);
      DataSourceConfig dataSourceConfig = new DataSourceConfig(id);

      if (weight != null) {
         dataSourceConfig.setWeight(convert(Integer.class, weight, 0));
      }

      if (canRead != null) {
         dataSourceConfig.setCanRead(convert(Boolean.class, canRead, false));
      }

      if (canWrite != null) {
         dataSourceConfig.setCanWrite(convert(Boolean.class, canWrite, false));
      }

      if (active != null) {
         dataSourceConfig.setActive(convert(Boolean.class, active, false));
      }

      if (type != null) {
         dataSourceConfig.setType(type);
      }

      if (tag != null) {
         dataSourceConfig.setTag(tag);
      }

      return dataSourceConfig;
   }

   @Override
   public GroupDataSourceConfig buildGroupDataSourceConfig(Attributes attributes) {
      String routerStrategy = attributes.getValue(ATTR_ROUTER_STRATEGY);
      String filters = attributes.getValue(ATTR_FILTERS);
      String forceWriteOnLogin = attributes.getValue(ATTR_FORCE_WRITE_ON_LOGIN);
      GroupDataSourceConfig groupDataSourceConfig = new GroupDataSourceConfig();

      if (routerStrategy != null) {
         groupDataSourceConfig.setRouterStrategy(routerStrategy);
      }

      if (filters != null) {
         groupDataSourceConfig.setFilters(filters);
      }

      if (forceWriteOnLogin != null) {
         groupDataSourceConfig.setForceWriteOnLogin(convert(Boolean.class, forceWriteOnLogin, false));
      }

      return groupDataSourceConfig;
   }

   @SuppressWarnings("unchecked")
   protected <T> T convert(Class<T> type, String value, T defaultValue) {
      if (value == null) {
         return defaultValue;
      }

      if (type == Boolean.class) {
         return (T) Boolean.valueOf(value);
      } else if (type == Integer.class) {
         return (T) Integer.valueOf(value);
      } else if (type == Long.class) {
         return (T) Long.valueOf(value);
      } else if (type == Short.class) {
         return (T) Short.valueOf(value);
      } else if (type == Float.class) {
         return (T) Float.valueOf(value);
      } else if (type == Double.class) {
         return (T) Double.valueOf(value);
      } else if (type == Byte.class) {
         return (T) Byte.valueOf(value);
      } else if (type == Character.class) {
         return (T) (Character) value.charAt(0);
      } else {
         return (T) value;
      }
   }
}
