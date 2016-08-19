# zebra-dao: An asynchronous and paginated dao

## 简介
`zebra-dao`是在`mybatis`基础上进一步封装的`异步数据源`,同时它也支持分页功能。它的产生背景是公司要求服务完全异步化，一个服务可能包括RPC调用请求、MemCached请求、KV存储请求以及MySQL数据库调用，目前其它三种请求的组件都有异步化的接口，但是数据库调用并没有。所以，在这个情况下，开发了这个异步化的DAO。目前，大众点评内部已有多个业务接入使用，已经接受了线上环境的验证和考验。

## 主要功能

1. 支持`Callback`和`Future`两种异步化方式
2. 支持分页功能
3. 其他功能的使用方式和[`mybatis`](https://mybatis.github.io/mybatis-3/zh/)一致

## 准备工作
下载源代码和编译

	git clone https://github.com/ainilife/zebra-dao.git
	mvn clean install -DskipTests

配置pom，引入zebra-dao。自行添加Spring，Mybatis以及Mybatis-Spring相关依赖。

	<dependency>
		<groupId>com.dianping.zebra</groupId>
		<artifactId>zebra-dao</artifactId>
		<version>0.1.5</version>
	</dependency>

在spring的appcontext-dao.xml文件中配置一个如下的bean。因为zebra-dao背后异步的实现方式，是使用线程池执行的方式的，所以需要在这里设置线程池的大小。该部分配置与原生的mybatis有差异，也是唯一差异的地方。

	<bean class="com.dianping.zebra.dao.mybatis.ZebraMapperScannerConfigurer">
        <property name="basePackage" value="com.dianping.zebra.dao.mapper" />
        <!--Optional，Default is 20 -->
        <property name="initPoolSize" value="20"></property>
        <!--Optional，Default is 200-->
        <property name="maxPoolSize" value="200"></property>
        <!--Optional，Default is 500-->
        <property name="queueSize" value="500"></property>
    </bean>
    
	<bean id="datasource" class="com.mchange.v2.c3p0.ComboPooledDataSource"          
        destroy-method="close">         
    	<property name="driverClass" value="com.mysql.jdbc.Driver"/>         
    	<property name="jdbcUrl" value="jdbc:mysql://localhost:3306/zebra?characterEncoding=UTF8"/>         
    	<property name="user" value="admin"/>         
    	<property name="password" value="123456"/>
	</bean>   

	<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
		<!--dataource-->
		<property name="dataSource" ref="datasource"/>
		<!--Mapper files-->
		<property name="mapperLocations" value="classpath*:config/sqlmap/**/*.xml" />
		<!--Entity package-->
		<property name="typeAliasesPackage" value="com.dianping.zebra.dao.entity" />
	</bean>
    
## 如何使用

### `Callback` for Asynchronous API
1.举例来说，在UserMapper中，有一个同步方法`findUserById`,如果想要有异步的通过回调方式的接口，则可以增加一个相同方法名，并且参数列表中多一个`AsyncDaoCallback`的参数的方法。

	public interface UserMapper {
		/**
		* Normal synchronization dao method.
		*/
		public UserEntity findUserById(@Param("userId") int userId);
		
		/**
		* Asynchronous callback method. Return void and only one
		* callback method required.
		*/
		public void findUserById(@Param("userId") int userId, AsyncDaoCallback<UserEntity> callback);
	}

2.在业务代码中使用`UserMapper`时，需要在调用时实现自己的`AsyncDaoCallback`，例如：

	@Autowired
	private UserMapper dao;
	
	......
	
	//asynchronous invoke
	dao.findUserById(1, new AsyncDaoCallback<UserEntity>() {
		@Override
		public void onSuccess(UserEntity user) {
			System.out.println(user);
			
			//another asynchronous invoke in the asynchronous invoke
			dao.findUserById(2, new AsyncDaoCallback<UserEntity>() {
				@Override
				public void onSuccess(UserEntity user) {
					System.out.println(user);
				}

				@Override
           		public void onException(Exception e) {
           		}
			});
			
			//synchronization invoke in the  asynchronous invoke
			UserEntity entity = dao.findUserById(3);
			System.out.println(entity);
		}

		@Override
     	public void onException(Exception e) {
     	}
	});
3.需要注意的是，必须要同时定义同步和异步的方法，方法名和异步方法扣除回调参数的其他参数都要相同。如果异步的方法想要定义一个和同步方法名不一样的名字，需要使用`TargetMethod`这个annotation指定所需要的同步方法是哪个。举例来说：

	//synchronization invoke
	public UserEntity findUserById(@Param("userId") int userId);
	
	//asynchronous invoke with a different method name
	@TargetMethod(name = "findUserById")
	public void findUserById2(@Param("userId") int userId, AsyncDaoCallback<UserEntity> callback);

### `Future` for Asynchronous API
1.举例来说，在UserMapper中，有一个同步方法`getAll`,如果想要有异步的通过`Future`方式的接口，则可以增加一个方法，并使用`@TargetMethod`指定新方法需要对应到哪一个同步方法。

	public interface UserMapper {
 		/**
		* Normal synchronization dao method.
		*/
		public List<UserEntity> getAll();

		/**
		* Asynchronous future method. Return future and must have the 
		* same params as synchronization method.
		*/
		@TargetMethod(name = "getAll")
		public Future<List<UserEntity>> getAll1();
	}

2.在业务代码中使用`UserMapper`时，需要通过Future接口获取到返回值，例如：

	@Autowired
	private UserMapper dao;
	
	......

	Future<List<UserEntity>> future = dao.getAll1();
	List<UserEntity> list = future.get();
	
	for(UserEntity user : list){
		System.out.println(user);
	}

## 分页功能
#### 逻辑分页
逻辑分页是指将数据库中的所有数据取出，然后通过Java代码控制分页。一般是通过JDBC协议中定位游标的位置进行操作的，使用`absolute`方法。`MyBatis`中原生也是通过这种方式进行分页的。下面举例说明：

在HeartbeatMapper.xml中

    <select id="getAll" parameterType="map" resultType="HeartbeatEntity">
       	SELECT * FROM heartbeat
    </select>

在HeartbeatMapper.java中，使用`RowBounds`中定义分页的`offset`和`limit`：

	List<HeartbeatEntity> getAll(RowBounds rb);
	
#### 物理分页
物理分页指的是在SQL查询过程中实现分页，依托与不同的数据库厂商，实现也会不同。zebra-dao扩展了一个拦截器，实现了改写SQL达到了物理分页的功能。下面举例说明如何使用：

1.修改Spring的配置中的sqlSessionFactory，添加configLocation

	<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
		<property name="dataSource" ref="dataSource" />
		<property name="configLocation" value="classpath:config/mybatis/mybatis-configuration.xml" />
	</bean>

2.增加`mybatis-configuration.xml`文件，目前zebra-dao只实现了MySQLDialect。

	<?xml version="1.0" encoding="UTF-8" ?>
	<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN" "http://mybatis.org/dtd/mybatis-3-config.dtd">
	<configuration>
		<plugins>
			<plugin interceptor="com.dianping.zebra.dao.plugin.page.PageInterceptor">
				<property name="dialectClass" value="com.dianping.zebra.dao.dialect.MySQLDialect"/>
			</plugin>
		</plugins>
	</configuration>

如此配置后，所有的分页查询都变成物理分页了。

#### 高级物理分页
zebra-dao支持在一个dao调用中同时获得总条数和数据。举例来说：
在`HeartbeatMapper.xml`中：

    <select id="getAll" parameterType="map" resultType="HeartbeatEntity">
       	SELECT * FROM heartbeat
    </select>

在`HeartbeatMapper.java`中，可以使用`PageModel`定义`page`和`pageNum`，同时使用这个对象就可以获得`recordCount`(总数)和`records`(结果数据)。注意的是，这个功能必须要配置过物理分页才能支持。

	// must return void
	void getAll(PageModel page);

注意这里没有任何返回值，返回的值在`PageModel`对象里面。一旦使用了PageModel的方式，必须是配置了物理分页，并且方法的返回值必须为`void`。

#### 分页功能的异步支持
1.使用`RowBounds`的方式，对于回调和Future都支持。

2.使用`PageModel`的方式，仅仅支持回调的方式，不支持Future的方式。在回调方式使用中，回调方法`onSuccess`中传入的`PageModel`并不是结果，结果在调用dao时传入的model中。

	dao.getAll(model, new AsyncDaoCallback<PageModel>() {
		@Override
		public void onSuccess(PageModel pageModel) {
			//pageModel为null，real result is in the model
			System.out.println(model.getRecordCount());
			System.out.println(model.getRecords().size());
		}

		@Override
		public void onException(Exception e) {
		}
	});















 
    
