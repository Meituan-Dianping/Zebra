# zebra-dao:An asynchronous and paginated dao 

[Readme in Chinese](https://github.com/ainilife/zebra-dao/blob/master/README_ZH.md)

[![Build Status](https://travis-ci.org/ainilife/zebra-dao.svg?branch=master)](https://travis-ci.org/ainilife/zebra-dao)

## Introduction
`zebra-dao` is an `asynchronous` dao built on top of `mybatis` and `mybatis-spring`，it also supports `page` feature. Now, DianPing corp intenal has already using this dao on the product environment。

## Majar features
1. support asynchronous methods, both `callback` and `future`
2. support paginate feature
3. support all other[`MyBatis`](https://mybatis.github.io/mybatis-3/zh/) features.


## Preparation
Download the source code and compile.

	git clone https://github.com/ainilife/zebra-dao.git
	mvn clean install -DskipTests

Config pom.xml,add the following denpendcy. Also add Spring, Mybatis and Mybatis-Spring dependencies all by yourself. 

	<dependency>
		<groupId>com.dianping.zebra</groupId>
		<artifactId>zebra-dao</artifactId>
		<version>0.1.5</version>
	</dependency>

The only difference between original `mybatis-spring` config and `zebra-dao` is the following beans config.

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
    
## Usage

### `Callback` for Asynchronous API
1.For example, UserMapper.java has both synchronization and asynchronous methods.

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

2.`AsyncDaoCallback` implementation is as follow:

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
3.Note that, asynchronous method should have the same name as the synchronization method. If you have to define a different name for 
asynchronous method, you have to use annotation `TargetMethod ` to define according synchronization method. For example:

	//synchronization invoke
	public UserEntity findUserById(@Param("userId") int userId);
	
	//asynchronous invoke with a different method name
	@TargetMethod(name = "findUserById")
	public void findUserById2(@Param("userId") int userId, AsyncDaoCallback<UserEntity> callback);

### `Future` for Asynchronous API
1.For example,UserMapper.java has a synchronization method `getAll`. You can add a new method `getAll1`, also assign a synchronization method in the annotation `TargetMethod `。

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

2.Using Future to get data, for example:

	@Autowired
	private UserMapper dao;
	
	......

	Future<List<UserEntity>> future = dao.getAll1();
	List<UserEntity> list = future.get();
	
	for(UserEntity user : list){
		System.out.println(user);
	}

## Pagination
#### Logical Pagination
Logical Pagination loads all data from database and pagination in Java Code。In general, Java use JDBC cursor to position the ResultSet.`MyBatis` use this method to pagination.

In `HeartbeatMapper.xml`

    <select id="getAll" parameterType="map" resultType="HeartbeatEntity">
       	SELECT * FROM heartbeat
    </select>

In `HeartbeatMapper.java`, `RowBounds` can define both `offset` and `limit`：

	List<HeartbeatEntity> getAll(RowBounds rb);
	
#### Physical Pagination
Physical Pagination loads paged data from database by using different sql. Different database vendor may has different implementation。`zebra-dao` implements an `Interceptor` of mybatis to achieve physical pagination feature. For example：

1.Modify the Spring bean sqlSessionFactory，add the configLocation

	<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
		<property name="dataSource" ref="dataSource" />
		<property name="configLocation" value="classpath:config/mybatis/mybatis-configuration.xml" />
	</bean>

2.add `mybatis-configuration.xml`. Note that zebra-dao only implements MySQLDialect Now。

	<?xml version="1.0" encoding="UTF-8" ?>
	<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN" "http://mybatis.org/dtd/mybatis-3-config.dtd">
	<configuration>
		<plugins>
			<plugin interceptor="com.dianping.zebra.dao.plugin.page.PageInterceptor">
				<property name="dialectClass" value="com.dianping.zebra.dao.dialect.MySQLDialect"/>
			</plugin>
		</plugins>
	</configuration>

After these configuration, all page query become physical pagination.

#### Advanced Pysical Pagination
zebra-dao supports get both `totalRecord` and `records` in one dao. For example:
In `HeartbeatMapper.xml`

    <select id="getAll" parameterType="map" resultType="HeartbeatEntity">
       	SELECT * FROM heartbeat
    </select>

In `HeartbeatMapper.java`, `PageModel` can define both `page` and `pageSize`. After the invoke, you can get both `records` and `recordCount` in the `PageModel` object. Note that to support this feature, physical pagination configs above is a must.

	// must return void
	void getAll(PageModel page);
	

#### Asynchronous Pagination
1.By using `RowBounds`，both `Callback` and `Future` are supported.

2.By using `PageModel`，only support `Callback` style. Note that in `Callback` invoke, the result int `onSuccess` is not the real result.

	dao.getAll(model, new AsyncDaoCallback<PageModel>() {
		@Override
		public void onSuccess(PageModel pageModel) {
			//pageModel is null，real result is in the `model`
			System.out.println(model.getRecordCount());
			System.out.println(model.getRecords().size());
		}

		@Override
		public void onException(Exception e) {
		}
	});
















 
    
