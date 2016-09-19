## zebra-api 分库分表

### 简介
在数据库单表记录行数达到很大的量的时候，需要对这个单表进行水平拆分。zebra-api就是数据库水平拆分的解决方案。

1. 对业务透明，业务写的SQL只知道逻辑的库名和逻辑的表名
2. 对大部分SQL都支持

目前，暂不支持路由配置动态变更，因为分库分表的路由逻辑一定是一开始设计好的，并且容量够大的，所以无需动态变更。

### 原理
单表水平拆分时，需要指定对某一个维度来进行拆分。那么什么是`维度`？举个例子，例如订单库中的订单表中，每一条订单记录必定是某个用户下的订单，那么订单表可以按照`用户`这个`维度`来进行划分。拆分之后，`物理上`订单库会在多个数据库集群中，每个集群中都可能会有多个订单表。但是，zebra使得业务在`逻辑上`看到的是`单库单表`。那么对于以下这条语句，zebra的执行顺序是什么呢？

		select * from Order where userId = "xxxxx"

1.	zebra会对上面的SQL进行解析，按照`分库分表的配置`配置的`维度`解析出该SQL需要按照分库分表的维度`(userId)`和值`xxxxx`。
2. 按照`分库分表的配置`配置的`路由策略`，得出该SQL会最终路由到哪个物理数据库(假设是OrderDB0)和物理表(假设是Order10)。
3. 改写SQL语句，替换表名。此处省略其余各种复杂细节。。

		select * from Order10 where userId = "xxxxx"
4. 将该SQL语句放到真正的数据库上`OrderDB0`上执行。
5. 将以上执行返回的结果，进行各种整理(此处省略其余各种复杂细节)返回。

### 注意事项
1. 分库分表后，业务的SQL必须要带上`维度`，因为如果没有`维度`，那么如果是Insert语句，则会直接报错，因为无法知道该行语句将要往哪个数据库上执行。如果是Select、Update或者Delete语句，那么会将该SQL对所有的库和表进行执行，试想一下，如果表被拆成了1024张表，那么，这个不带维度的SQL可能会执行1024次，`性能极差`。
2. 分库分表后，业务的SQL最好要只有`一个`维度。因为如果有多个维度，就涉及到将主维度的数据同步到其他维度中去。这里会有一定的延迟。
3. 分库分表后，不能写诸如join，group by，limit等复杂的SQL。一来，性能差；二来，zebra未必支持。

### 接入之前的准备工作
1. 开发需要排查业务所有的SQL，找出`唯一的那个维度`，如果不唯一，尽可能的优化业务，使得维度尽量的少。
2. 和DBA确定需要分的表，以及需要分多少张表。确认好之后，让DBA在各个环境进行建库建表。
3. 指定分库分表配置。每个接入的分库分表规则都有一个名字，一般以需要分库的库名小写来起，比如`welife`，该规则可以在lion上的`shardds.welife.shard`看到。规则的json格式如下：

		{
			"tableShardConfigs":[
				{
					"tableName":"welife_users",
					"dimensionConfigs":[
						{
							"dbRule":"crc32(#bid#)%10",
							"dbIndexes":"welife[0-9]",
							"tbRule":"(crc32(#bid#)/10).toLong()%10",
							"tbSuffix":"everydb:[0,9]",
							"isMaster":true
						}],
					"generatedPK":"uid"
				}
			]
		}

这里具体解释一下这个配置的意思:

1.	tableName: 需要分库分表的表名，该例中需要水平拆分的表是welife_users。
2.	dimensionConfigs: 指定这个表的维度规则，可以有多个维度规则，该例中仅有单维度。这是推荐的用法。
3.	dbRule: 指定库名的路由规则表达式。zebra会解析出SQL中`#bid#`维度和值，并将该值带入该表达式计算出最终落到的数据库的index。
4.	dbIndexes: 指定所有拆分的库的GroupDataSource的jdbcRef。zebra会将第3步计算出的index带入该表达式，得出最终落到的数据库的jdbcRef值。`dbIndexes`可以有以下几种`等价`的写法，它们的index都是从0开始按照写的顺序呢进行排列。

		welife[0-4]			
		welife0,welife1,welife2,welife3,welife4
		welife0,welife[1-3],welife4
		
5.	tbRule: 指定是分表的路由规则表达式。zebra会解析出SQL中`#bid#`维度和值，并将该值带入该表达式计算出最终落到的数据库中表名的后缀index。表达式内置了`crc32`和`md5`两个函数，同时可以支持多值组成的单维度，例如下面的表达式，zebra会解析SQL，得到`#bid#`和`#uid#`两个值带入表达式得出index。

		(crc32(md5(#bid# + "_" + #uid#))).toLong()%10

6.	tbSuffix: 是表的后缀命名规则，分`everydb`和`alldb`。everydb指任何库上的表名都相同。例如`everydb:[0,9]`，意味着分着的10个库上，每个库上的表名均为welife_users0到welife_users9。而`alldb`是指10表名在十个库上都不一样，同样的例子如果使用`alldb`的方式应该配成`alldb:[0,99]`。此时在库`welife0`上的表名为`welife_users[0-9]`，在`welife1`上的表名为`welife_users[10-19]`......在`welife9`上的表名为`welife_users[89-99]`。`建议使用alldb的方式`，这样清晰，方便定位。
7.	generatedPK: 表中唯一识别一行的主键字段，这里是uid。
8.	isMaster: 表明该维度是否是主维度，也就说说一个分表可以有多个维度，但只有主维度支持写，其他辅助维度只能进行读。点评内部是通过binlog的方式进行复制的方式，将主维度的数据自动的复制到辅助维度上去。
