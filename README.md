## zebra-api 用户手册

[![Build Status](https://travis-ci.org/dianping/zebra.svg?branch=master)](https://travis-ci.org/dianping/zebra)

## 简介
`Zebra`是在`c3p0`和`tomcat-jdbc`基础上进行包装成的点评内部使用的`动态数据源`，它具有以下的功能点：

1. 对业务隐藏数据源相关配置，比如主库或者从库的位置、用户名以及密码等
2. 实时响应配置变化，应用自刷新无需重启
3. 读写分离，支持多种场景的自定义路由策略
4. 支持分库分表，具体接入请[参考文档](/arch/zebra/blob/master/zebra-api/README_SHARD.md)
5. 底层多数据源支持:`c3p0`或者`tomcat-jdbc`或者`druid`
6. 丰富的监控信息在`CAT`上展现
7. 支持DBA方便的对数据库进行维护，如写库切换，读库上线下线，用户名密码变更等
8. 支持SQL流控，DBA可以在后台按照比例对指定SQL语句进行限制访问

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

zebra-ds-monitor-client还需要额外配置一个xml文件到应用spring加载路径中去。确保web.xml中引入额外的spring文件，

	classpath*:config/spring/common/appcontext-ds-replacer.xml

如果不想引入该文件，也可以自行添加一个spring的Bean。

	<bean class="com.dianping.zebra.monitor.spring.DataSourceAutoReplacer"/>

#### 其他依赖说明

* 如果想要在`CAT`中的心跳中看到数据源连接池的信息，需升级`cat-client`到`1.1.3`之上，`dpsf-net`升级到`2.1.21`之上,`lion-client`升级到`0.4.8`之上的版本。

### 第二步:通过Spring方式使用

#### 完整连接池配置

	<bean id="dataSource" class="com.dianping.zebra.group.jdbc.GroupDataSource" init-method="init">
        <!-- 唯一确定数据库的key，请咨询DBA使用哪个key -->
		<property name="jdbcRef" value="tuangou2010" />
        <!-- 选择使用背后使用哪种数据源，"c3p0"或者"tomcat-jdbc"，可以不配，默认值为"c3p0" -->
        <property name="poolType" value="c3p0" />
        <!-- 该值对应tomcat-jdbc的"minIdle" -->
		<property name="minPoolSize" value="${lion.key.minPoolSize}" />
        <!-- 该值对应tomcat-jdbc的"maxActive" -->
		<property name="maxPoolSize" value="${lion.key.maxPoolSize}" />
        <!-- 该值对应tomcat-jdbc的"initialSize" -->
        <property name="initialPoolSize" value="${lion.key.initialPoolSize}" />
        <!-- 该值对应tomcat-jdbc的"maxWait" -->
        <property name="checkoutTimeout" value="1000" />
        <!-- 关闭登录用户走写库，为兼容默认值是true，如无需要请关闭该特性 -->
        <property name="forceWriteOnLogin" value="false" />
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

支持c3p0所有数据源配置，而对于tomcat-jdbc，仅支持上述几个连接池大小的配置。
对于tomcat-jdbc的其他配置，zebra配置了一套默认值。

#### 简化版本连接池配置（使用默认配置）

    <bean id="dataSource" class="com.dianping.zebra.group.jdbc.GroupDataSource" init-method="init">
		<property name="jdbcRef" value="tuangou2010" />
		<!-- 选择使用背后使用哪种数据源，"c3p0"或者"tomcat-jdbc"，可以不配，默认值为"c3p0" -->
        <property name="poolType" value="c3p0" />
        <!-- 关闭登录用户走写库，为兼容默认值是true，如无需要请关闭该特性 -->
        <property name="forceWriteOnLogin" value="false" />
    </bean>

#### 配置说明

其中，`jdbcRef`属性是访问该数据库的key，一般是数据库名的全小写(`大小写敏感`)，`zebra`会自动根据这个key到`Lion`上查找`jdbcUrl`,`user`,`password`,`driverClass`和`C3P0`的参数。业务也可以使用自定义的C3P0参数覆盖掉默认值，具体来说，有以下几种情况：

1. C3P0参数值是在`bean`中直接写死，那么C3P0的参数将不具有动态刷新的功能。
2. C3P0参数值是在`bean`中配置的是`Lion`中key，那么该key的`Lion`值覆盖掉zebra的默认值。而且一旦修改了`Lion`上key的后，该数据源将进行自刷新。
3. 业务也可以不配置任何C3P0参数，所有参数将直接继承自`jdbcRef`所给出的默认配置。但不推荐这种方式，因为C3P0的配置属于业务方，使用默认配置无法做到业务隔离。

#### 事务处理
`zebra`是一个读写分离的数据源，如果在事务中，那默认所有在事务中的操作均将路由到主库进行操作。

#### 特殊配置介绍
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

2.关闭登录用户默认走写库的逻辑。目前，为了兼容老的DPDL登录用户走写库的逻辑，DAL也默认开启了，当然也可以通过在spring的配置中加入如下的property来关闭该功能。

    <bean id="datasource" class="com.dianping.zebra.group.jdbc.GroupDataSource" init-method="init">
    	<property name="jdbcRef" value="tuangou2010" />
        <property name="forceWriteOnLogin" value="false" /> <!-- 关闭登录用户走写库，默认值是true，表明开启该功能 -->
    </bean>

3.SocketTimeout支持。为了更及时的从数据库故障中恢复，线上将会对所有的jdbcurl的参数加入这个参数，默认值是60000(60秒)。这个的影响是，如果有慢SQL超过了60秒，SQL执行将会失败。所以业务可以在GroupDataSource中设置这个值来覆盖默认值。

    <property name="socketTimeout" value="600000"/>

### 第二步(Optional):直接代码中使用

		GroupDataSource dataSource = new GroupDataSource("jdbcRef");
		dataSource.setForceWriteOnLogin(false);

		//Set other datasource properties if you want

		dataSource.init();

		//Now dataSource is ready for use


### 常见问题
#### Q：为什么要加`init-method`，不加会怎么样？
A：`Zebra`内需要启动多线程，而在构造函数中启动线程是不安全的，所以需要这两个方法来启动和销毁线程。

#### Q：我想看jdbcRef的配置，在哪里可以看到？
A：想要理解并查看配置，请看文档 [README_CONFIG.md](/arch/zebra/blob/master/zebra-api/README_CONFIG.md)

#### Q：GroupDataSource是如何根据jdbcRef读取配置的?
A：根据jdbcRef可以找到groupds.{jdbcRef}.mapping这个key，从而读到这个值；根据里面的值再进一步的去寻找ds的值，从而构建出一份配置文件，然后进行初始化。

#### Q：GroupDataSource是如何做到动态刷新的？
A：利用Lion配置变更会通知的机制。一旦任何配置变更，GroupDataSource就进行自刷新。自刷新的逻辑是，重新建立新的DataSource，然后销毁老的DataSource。

#### Q：GroupDataSource是如何做到读重试的？
A：一旦从某台读库上取连接失败，那么会自动去另外一台读库上进行重试，重试一次。有两个条件：一、配置有两台读库；二、针对的是取连接失败动作才重试

#### Q：如何判断重试是否成功？
A：在CAT上的SQL报表中，可以看到重试的sql名字和原来的sql名字有区分，重试的sql名字后缀是`(retry-by-zebra)`，你可以对比原来sql的失败个数和重试sql的成功个数，一般都能对上。

#### Q：如何让一个请求中的所有SQL都走写库，或者如何处理`先写后读`的场景？
A: zebra开放出了两个接口来进行处理：

    /**
     * 使用本地的context来设置走主库，该方式只会影响本应用内本次请求的所有sql走主库，不会影响到piegon后端服务。
     * 调用过该方法后，一定要在请求的末尾调用clearLocalContext进行清理操作。
     * 优先级比forceMasterInPiegonContext低。
     */
    ZebraForceMasterHelper.forceMasterInLocalContext();

    /**
     * 通过使用piegon的context来设置走主库，该方式将会透传到后端应用，使后端应用同样走主库，使用该方式请慎用。
     * 因为使用piegon的context,所以piegon会自动的对该context进行清理，故调用完该方法后，无需进行清理动作。
     * forceMasterInPiegonContext比forceMasterInLocalContext有更高的优先级。
     */
    ZebraForceMasterHelper.forceMasterInPiegonContext();

    /**
     * 配合forceMasterInLocalContext进行使用，在请求的末尾调用该方法，对LocalContext进行清理。
     */
    ZebraForceMasterHelper.clearLocalContext();

#### Q：如何指定让具体某条SQL走写库？
A: 可以在SQL前面加一个`hint`，表明这个读请求强制走写库，其中, `/*+zebra:w*/`就是hint的格式，告诉zebra这条sql必须走写库。

    /*+zebra:w*/select * from test

#### Q：为什么会遇到这样的异常：java.sql.SQLException: An attempt by a client to checkout a Connection has timed out.
A：这个错误是c3p0报出来的错误，表明c3p0尝试去连接池中拿连接超时了。一般遇到这样的错误，可以有以下几个角度去解决：
    1. 是否是因为并发量太大的原因？如果是，请调整c3p0的参数，比如可以增加连接数，将maxIdleTime设置为0。
    2. 是否当时有慢查询，导致数据库拥堵？如果是，请联系DBA抓取慢查询，对sql进行优化。
    3. 是否当时网络有问题，比如网络拥堵了一下？如果是，请联系DBA排查。

#### Q：什么是数据源自动替换？
A：为了方便升级，不用业务修改代码，zebra可以对数据库级别对数据源进行动态替换。替换的技术是Spring加载完bean的时候对DataSource这个类型的bean进行替换。过程如下：

    1. 从datasource中获取jdbcUrl，从而知道是该datasource会访问哪个库
    2. 判断该数据库是否在`Lion`的白名单`groupds.autoreplace.database`配置过
    3. 如果配置过，则进行替换。替换时，判断用户名如果是读用户，则替换过的GroupDataSource只能读；如果是写用户，则替换过的GroupDataSource只能写。
替换的DataSource仅限于c3p0和dpdl两种数据源。原则上，该功能对新库不进行使用，只对老库进行使用。
