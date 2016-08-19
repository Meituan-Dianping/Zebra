package com.dianping.zebra.group.config.system.transform;

import static com.dianping.zebra.group.config.system.Constants.ATTR_ALLOW_PERCENT;
import static com.dianping.zebra.group.config.system.Constants.ATTR_APP;
import static com.dianping.zebra.group.config.system.Constants.ATTR_IP_PREFIX;
import static com.dianping.zebra.group.config.system.Constants.ATTR_NAME;
import static com.dianping.zebra.group.config.system.Constants.ATTR_SQL_ID;

import org.xml.sax.Attributes;

import com.dianping.zebra.group.config.system.entity.DataCenter;
import com.dianping.zebra.group.config.system.entity.SqlFlowControl;
import com.dianping.zebra.group.config.system.entity.SystemConfig;

public class DefaultSaxMaker implements IMaker<Attributes> {

   @Override
   public DataCenter buildDataCenter(Attributes attributes) {
      String name = attributes.getValue(ATTR_NAME);
      String ipPrefix = attributes.getValue(ATTR_IP_PREFIX);
      DataCenter dataCenter = new DataCenter(name);

      if (ipPrefix != null) {
         dataCenter.setIpPrefix(ipPrefix);
      }

      return dataCenter;
   }

   @Override
   public SqlFlowControl buildSqlFlowControl(Attributes attributes) {
      String sqlId = attributes.getValue(ATTR_SQL_ID);
      String allowPercent = attributes.getValue(ATTR_ALLOW_PERCENT);
      String app = attributes.getValue(ATTR_APP);
      SqlFlowControl sqlFlowControl = new SqlFlowControl(sqlId);

      if (allowPercent != null) {
         sqlFlowControl.setAllowPercent(convert(Integer.class, allowPercent, 0));
      }

      if (app != null) {
         sqlFlowControl.setApp(app);
      }

      return sqlFlowControl;
   }

   @Override
   public SystemConfig buildSystemConfig(Attributes attributes) {
      SystemConfig systemConfig = new SystemConfig();

      return systemConfig;
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
