## zebra-api 用户手册

## 简介
`Zebra`是在jdbc协议层上开发的`数据库访问层`中间件，它具有以下的功能点：

1. 配置集中管理，动态刷新     
2. 支持读写分离、分库分表
3. SQL流控

## 使用说明

### 第一步:添加POM依赖

目前的最新版本为`2.8.3`，并配合数据监控组件`zebra-ds-monitor-client`一起使用

	<dependency>
    	<groupId>com.dianping.zebra</groupId>
	    <artifactId>zebra-api</artifactId>
    	<version>${version}</version>
	</dependency>
  <dependency>
      <groupId>com.dianping.zebra</groupId>
      <artifactId>zebra-ds-monitor-client</artifactId>
      <version>${version}</version>
  </dependency>

### 第二步:通过Spring方式使用

#### SingleDataSource(单数据源)配置
	<bean id="dataSource" class="com.dianping.zebra.single.jdbc.SingleDataSource" init-method="init" destroy-method="close">
        <!--必填，填写atlas的地址-->
        <property name="jdbcUrl" value="{jdbcUrl}"/>
        <!--必填，填写atlas的用户名-->
        <property name="user" value="{user}" />
        <!--必填，填写atlas的密码-->
        <property name="password" value="{password}" />
        <!-- 选填，默认值为"c3p0",还支持"tomcat-jdbc"或者"druid"-->
        <property name="driverClass" value="com.mysql.jdbc.Driver" />
        <property name="poolType" value="c3p0" />
        <!-- c3p0的minPoolSize,该值对应tomcat-jdbc或druid的"minIdle" -->
        <property name="minPoolSize" value="5" />
        <!-- c3p0的maxPoolSize,该值对应tomcat-jdbc或druid的"maxActive" -->
        <property name="maxPoolSize" value="30" />
        <!-- c3p0的initialPoolSize,该值对应tomcat-jdbc或druid的"initialSize" -->
        <property name="initialPoolSize" value="5" />
        <!-- c3p0的checkoutTimeout,该值对应tomcat-jdbc或druid的"maxWait" -->
        <property name="checkoutTimeout" value="1000" />
        <property name="maxIdleTime" value="1800" />
        <property name="idleConnectionTestPeriod" value="60" />
        <property name="acquireRetryAttempts" value="3" />
        <property name="acquireRetryDelay" value="300" />
        <property name="maxStatements" value="0" />
        <property name="maxStatementsPerConnection" value="100" />
        <property name="numHelperThreads" value="6" />
        <property name="maxAdministrativeTaskTime" value="5" />
        <property name="preferredTestQuery" value="SELECT 1" />
    </bean>

#### GroupDataSource(读写分离数据源)配置

    <bean id="dataSource" class="com.dianping.zebra.group.jdbc.GroupDataSource" init-method="init">
        <!-- jdbcRef决定需要访问的库的名字，这里指访问的是test库 -->
        <property name="jdbcRef" value="test" />
        <!-- 选择使用背后使用哪种数据源，"c3p0"或者"tomcat-jdbc"，可以不配，默认值为"c3p0" -->
        <property name="poolType" value="c3p0" />
        <!-- 选择配置源，默认是remote方式，这里使用的是local方式，意味着配置是本地文件 -->
        <property name="configManagerType" value="local" />
        <property name="maxIdleTime" value="1800" />
        <property name="idleConnectionTestPeriod" value="60" />
        <property name="acquireRetryAttempts" value="3" />
        <property name="acquireRetryDelay" value="300" />
        <property name="maxStatements" value="0" />
        <property name="maxStatementsPerConnection" value="100" />
        <property name="numHelperThreads" value="6" />
        <property name="maxAdministrativeTaskTime" value="5" />
        <property name="preferredTestQuery" value="SELECT 1" />
    </bean>

创建 test.properties 到 src/main/resources下，文件名必须是以`jdbcRef.properties`命名

    groupds.dbname.mapping=(db2:1,db3:1),(db1,db2)

    #zebra.ds.db1.jdbc
    ds.db1.jdbc.active=true
    ds.db1.jdbc.url=jdbc:mysql://192.168.0.1:3306/test?characterEncoding=UTF8&socketTimeout=60000
    ds.db1.jdbc.username=root
    ds.db1.jdbc.password=123456
    ds.db1.jdbc.driverClass=com.mysql.jdbc.Driver
    #other properties
    ds.db1.jdbc.properties=idleConnectionTestPeriod=60&acquireRetryAttempts=50&acquireRetryDelay=300&maxStatements=0
    ds.db1.jdbc.testReadOnlySql=SELECT 1

    #zebra.ds.db2.jdbc
    ds.db2.jdbc.active=true
    ds.db2.jdbc.url=jdbc:mysql://192.168.0.1:3306/test?characterEncoding=UTF8&socketTimeout=60000
    ds.db2.jdbc.username=sa
    ds.db2.jdbc.password=123456
    ds.db2.jdbc.driverClass=com.mysql.jdbc.Driver
    ds.db2.jdbc.testReadOnlySql=SELECT 1

    #zebra.ds.db3.jdbc
    ds.db3.jdbc.active=true
    ds.db3.jdbc.url=jdbc:mysql://192.168.0.1:3306/test?characterEncoding=UTF8&socketTimeout=60000
    ds.db3.jdbc.username=root
    ds.db3.jdbc.password=123456
    ds.db3.jdbc.driverClass=com.mysql.jdbc.Driver
    ds.db3.jdbc.properties=idleConnectionTestPeriod=80&acquireRetryAttempts=50&acquireRetryDelay=300&maxStatements=1
    ds.db3.jdbc.testReadOnlySql=call readonly()

##### 本地(local)配置和远端(remote)配置
如果要实现配置的集中化管理和动态刷新功能，请自行实现`RemoteConfigService`中的相应方法。点评内部是基于内部的配置系统来进行配置的，在开源版本中剥离了这个依赖，所以请自行实现。
目前仅支持local方式的使用GroupDataSource

##### 事务处理
`zebra`是一个读写分离的数据源，如果在事务中，那默认所有在事务中的操作均将路由到主库进行操作。

##### 特殊配置介绍
1.如果业务对主从延迟要求很高，不能容忍一点延迟，比如支付等业务，可以根据需要配置两个数据源，其中一个`只走读库`，另外一个`只走写库`，可以在spring的配置中加入如下的property。一般情况下，除非对主从延迟要求很高，一般应用`不建议`使用该配置。默认值是`master-slave`。

    <bean id="readDs" class="com.dianping.zebra.group.jdbc.GroupDataSource" init-method="init">
    	<property name="jdbcRef" value="tuangou2010" />
        <property name="routerType" value="slave-only" /> <!-- 只取已配的读账号连接，如需使用请升级到2.8.3版本以上 -->
    </bean>

    <bean id="writeDs" class="com.dianping.zebra.group.jdbc.GroupDataSource" init-method="init">
    	<property name="jdbcRef" value="tuangou2010" />
        <property name="routerType" value="master-only" /><!-- 只取已配的写账号连接，如需使用请升级到2.8.3版本以上 -->
    </bean>
注意这里是说的是账号连接，有可能读账号也在真正的主库上。

#### ShardDataSource(分库分表数据源)配置

    <bean id="zebraDs0" class="com.dianping.zebra.single.jdbc.SingleDataSource" init-method="init" destroy-method="close">
        <property name="jdbcUrl" value="jdbc:mysql://192.168.0.1:6002/zebraatlas?characterEncoding=UTF8"/>
        <property name="user" value="zebraatlas_wr" />
        <property name="password" value="FsdRg5d3j01" />
        <property name="driverClass" value="com.mysql.jdbc.Driver" />
        <property name="poolType" value="c3p0" />
        <property name="minPoolSize" value="2" />
        <property name="maxPoolSize" value="10" />
        <property name="initialPoolSize" value="2" />
        <property name="checkoutTimeout" value="2000" />
        <property name="maxIdleTime" value="1800" />
        <property name="idleConnectionTestPeriod" value="60" />
        <property name="acquireRetryAttempts" value="3" />
        <property name="acquireRetryDelay" value="300" />
        <property name="maxStatements" value="0" />
        <property name="maxStatementsPerConnection" value="100" />
        <property name="numHelperThreads" value="6" />
        <property name="maxAdministrativeTaskTime" value="5" />
        <property name="preferredTestQuery" value="SELECT 1" />
    </bean>
    <bean id="zebraDs1" class="com.dianping.zebra.single.jdbc.SingleDataSource" init-method="init" destroy-method="close">
        <property name="jdbcUrl" value="jdbc:mysql://192.168.0.1:6002/zebraatlas?characterEncoding=UTF8"/>
        <property name="user" value="zebraatlas_wr" />
        <property name="password" value="FsdRg5d3j01" />
        <property name="driverClass" value="com.mysql.jdbc.Driver" />
        <property name="poolType" value="c3p0" />
        <property name="minPoolSize" value="2" />
        <property name="maxPoolSize" value="10" />
        <property name="initialPoolSize" value="2" />
        <property name="checkoutTimeout" value="2000" />
        <property name="maxIdleTime" value="1800" />
        <property name="idleConnectionTestPeriod" value="60" />
        <property name="acquireRetryAttempts" value="3" />
        <property name="acquireRetryDelay" value="300" />
        <property name="maxStatements" value="0" />
        <property name="maxStatementsPerConnection" value="100" />
        <property name="numHelperThreads" value="6" />
        <property name="maxAdministrativeTaskTime" value="5" />
        <property name="preferredTestQuery" value="SELECT 1" />
    </bean>
    <bean id="zebraDs2" class="com.dianping.zebra.single.jdbc.SingleDataSource" init-method="init" destroy-method="close">
        <property name="jdbcUrl" value="jdbc:mysql://192.168.0.1:6002/zebraatlas?characterEncoding=UTF8"/>
        <property name="user" value="zebraatlas_wr" />
        <property name="password" value="FsdRg5d3j01" />
        <property name="driverClass" value="com.mysql.jdbc.Driver" />
        <property name="poolType" value="c3p0" />
        <property name="minPoolSize" value="2" />
        <property name="maxPoolSize" value="10" />
        <property name="initialPoolSize" value="2" />
        <property name="checkoutTimeout" value="2000" />
        <property name="maxIdleTime" value="1800" />
        <property name="idleConnectionTestPeriod" value="60" />
        <property name="acquireRetryAttempts" value="3" />
        <property name="acquireRetryDelay" value="300" />
        <property name="maxStatements" value="0" />
        <property name="maxStatementsPerConnection" value="100" />
        <property name="numHelperThreads" value="6" />
        <property name="maxAdministrativeTaskTime" value="5" />
        <property name="preferredTestQuery" value="SELECT 1" />
    </bean>
    <bean id="zebraDs3" class="com.dianping.zebra.single.jdbc.SingleDataSource" init-method="init" destroy-method="close">
        <property name="jdbcUrl" value="jdbc:mysql://192.168.0.1:6002/zebraatlas?characterEncoding=UTF8"/>
        <property name="user" value="zebraatlas_wr" />
        <property name="password" value="FsdRg5d3j01" />
        <property name="driverClass" value="com.mysql.jdbc.Driver" />
        <property name="poolType" value="c3p0" />
        <property name="minPoolSize" value="2" />
        <property name="maxPoolSize" value="10" />
        <property name="initialPoolSize" value="2" />
        <property name="checkoutTimeout" value="2000" />
        <property name="maxIdleTime" value="1800" />
        <property name="idleConnectionTestPeriod" value="60" />
        <property name="acquireRetryAttempts" value="3" />
        <property name="acquireRetryDelay" value="300" />
        <property name="maxStatements" value="0" />
        <property name="maxStatementsPerConnection" value="100" />
        <property name="numHelperThreads" value="6" />
        <property name="maxAdministrativeTaskTime" value="5" />
        <property name="preferredTestQuery" value="SELECT 1" />
    </bean>

    <bean id="zebraDS" class="com.dianping.zebra.shard.jdbc.ShardDataSource" init-method="init">
        <property name="dataSourcePool">
            <map>
                <entry key="id0" value-ref="zebraDs0"/>
                <entry key="id1" value-ref="zebraDs1"/>
                <entry key="id2" value-ref="zebraDs2"/>
                <entry key="id3" value-ref="zebraDs3"/>
            </map>   
        </property>
        <property name="routerFactory">
            <bean class="com.dianping.zebra.shard.router.builder.XmlResourceRouterBuilder">
                <constructor-arg value="spring/shard/router-local-rule.xml"/>
            </bean>
        </property>
        <!--业务自行调整并发查询的线程池参数 -->
        <property name="parallelCorePoolSize" value="16" />
        <!--业务自行调整并发查询的线程池参数 -->
        <property name="parallelMaxPoolSize" value="32" />
        <!--业务自行调整并发查询的线程池参数 -->
        <property name="parallelWorkQueueSize" value="500" />
        <!--业务自行调整逻辑SQL在线程池里面的超时时间，可以在beta环境设置的大一点 -->
        <property name="parallelExecuteTimeOut" value="3000" />
    </bean>

在src/main/resources目录下创建spring/shard/router-local-rule.xml文件

    <?xml version="1.0" encoding="UTF-8"?>
    <router-rule>
        <table-shard-rule table="Feed" generatedPK="id">
            <shard-dimension dbRule="(#id#.intValue() % 8).intdiv(2)"
                dbIndexes="id0,id1,id2,id3"
                tbRule="#id#.intValue() % 2"
                tbSuffix="alldb:[0,7]"
                isMaster="true">
            </shard-dimension>
        </table-shard-rule>
    </router-rule>
