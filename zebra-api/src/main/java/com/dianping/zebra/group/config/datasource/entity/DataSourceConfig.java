package com.dianping.zebra.group.config.datasource.entity;

import static com.dianping.zebra.group.config.datasource.Constants.ATTR_ID;
import static com.dianping.zebra.group.config.datasource.Constants.ENTITY_DATA_SOURCE_CONFIG;

import java.util.ArrayList;
import java.util.List;

import com.dianping.zebra.group.config.datasource.BaseEntity;
import com.dianping.zebra.group.config.datasource.IVisitor;

public class DataSourceConfig extends BaseEntity<DataSourceConfig> {
   private String m_id;

   private int m_weight = 1;

   private boolean m_canRead = false;

   private boolean m_canWrite = false;

   private boolean m_active;

   private String m_type = "c3p0";

   private String m_testReadOnlySql = "select @@read_only";

   private long m_timeWindow = 0L;

   private long m_punishLimit = 0L;

   private String m_jdbcUrl = "";

   private String m_username = "";

   private String m_driverClass = "";

   private String m_password = "";

   private String m_tag = "";

   private int m_warmupTime = 0;

   private List<Any> m_properties = new ArrayList<Any>();

   public DataSourceConfig() {
   }

   public DataSourceConfig(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitDataSourceConfig(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof DataSourceConfig) {
         DataSourceConfig _o = (DataSourceConfig) obj;
         String id = _o.getId();

         return m_id == id || m_id != null && m_id.equals(id);
      }

      return false;
   }

   public List<Any> getProperties() {
      return m_properties;
   }

   public boolean getActive() {
      return m_active;
   }

   public boolean getCanRead() {
      return m_canRead;
   }

   public boolean getCanWrite() {
      return m_canWrite;
   }

   public String getDriverClass() {
      return m_driverClass;
   }

   public String getId() {
      return m_id;
   }

   public String getJdbcUrl() {
      return m_jdbcUrl;
   }

   public String getPassword() {
      return m_password;
   }

   public long getPunishLimit() {
      return m_punishLimit;
   }

   public String getTag() {
      return m_tag;
   }

   public String getTestReadOnlySql() {
      return m_testReadOnlySql;
   }

   public long getTimeWindow() {
      return m_timeWindow;
   }

   public String getType() {
      return m_type;
   }

   public String getUsername() {
      return m_username;
   }

   public int getWarmupTime() {
      return m_warmupTime;
   }

   public int getWeight() {
      return m_weight;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());

      return hash;
   }

   public boolean isActive() {
      return m_active;
   }

   public boolean isCanRead() {
      return m_canRead;
   }

   public boolean isCanWrite() {
      return m_canWrite;
   }

   @Override
   public void mergeAttributes(DataSourceConfig other) {
      assertAttributeEquals(other, ENTITY_DATA_SOURCE_CONFIG, ATTR_ID, m_id, other.getId());

      m_weight = other.getWeight();

      m_canRead = other.getCanRead();

      m_canWrite = other.getCanWrite();

      m_active = other.getActive();

      if (other.getType() != null) {
         m_type = other.getType();
      }

      if (other.getTag() != null) {
         m_tag = other.getTag();
      }
   }

   public void setProperties(List<Any> properties) {
      m_properties = properties;
   }

   public DataSourceConfig setActive(boolean active) {
      m_active = active;
      return this;
   }

   public DataSourceConfig setCanRead(boolean canRead) {
      m_canRead = canRead;
      return this;
   }

   public DataSourceConfig setCanWrite(boolean canWrite) {
      m_canWrite = canWrite;
      return this;
   }

   public DataSourceConfig setDriverClass(String driverClass) {
      m_driverClass = driverClass;
      return this;
   }

   public DataSourceConfig setId(String id) {
      m_id = id;
      return this;
   }

   public DataSourceConfig setJdbcUrl(String jdbcUrl) {
      m_jdbcUrl = jdbcUrl;
      return this;
   }

   public DataSourceConfig setPassword(String password) {
      m_password = password;
      return this;
   }

   public DataSourceConfig setPunishLimit(long punishLimit) {
      m_punishLimit = punishLimit;
      return this;
   }

   public DataSourceConfig setTag(String tag) {
      m_tag = tag;
      return this;
   }

   public DataSourceConfig setTestReadOnlySql(String testReadOnlySql) {
      m_testReadOnlySql = testReadOnlySql;
      return this;
   }

   public DataSourceConfig setTimeWindow(long timeWindow) {
      m_timeWindow = timeWindow;
      return this;
   }

   public DataSourceConfig setType(String type) {
      m_type = type;
      return this;
   }

   public DataSourceConfig setUsername(String username) {
      m_username = username;
      return this;
   }

   public DataSourceConfig setWarmupTime(int warmupTime) {
      m_warmupTime = warmupTime;
      return this;
   }

   public DataSourceConfig setWeight(int weight) {
      m_weight = weight;
      return this;
   }

}
