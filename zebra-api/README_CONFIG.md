## zebra配置文档 

### 配置设计
#### 配置格式
Lion中有两个地方需要配置，`ds`项目中负责配置单个数据源，`groupds`项目负责配置组合数据源。

#### 单数据源配置`ds`（所有配置可以在Lion中的`ds`项目下查询到）
配置key的命名规范是：ds.`name`.jdbc.`parameter`，其中`name`是标识了唯一一套数据库连接和账号，而`parameter`则是代表参数。
对于一套数据库账号，需要配置以下参数
1. `ds.test-m1-read.jdbc.url`：配置jdbcUrl
2. `ds.test-m1-read.jdbc.user`：配置用户名
3. `ds.test-m1-read.jdbc.password`：配置密码
4. `ds.test-m1-read.jdbc.driverClass`：配置driverClass
5. `ds.test-m1-read.jdbc.active`：配置是否可用。默认值为true。
6. `ds.test-m1-read.jdbc.properties`(`optional`)：如果业务想自己配置c3p0相关参数，这个参数可以不用配置；一般情况下，还是给出默认配置即可。
7. `ds.test-m1-read.jdbc.warmupTime`(`optional`)：该配置表明需要业务数据库在多久的时间内完成连接，以避免连接风暴。该配置以毫秒为单位，默认值是0。

* 值得注意的是，其中数据库名字也是有规范的。比如test-m1-read，其中`m1`表示它是主库中的第一台，如果有多台主库，则配置成`m2`、`m3`...如果从库则配置成`s1`、`s2`...
* `read`表明这个是只读账号，如果是可写账号，则配置成`write`。三个部分以`-`符号分隔。

* 对于每一台数据库来说，都要配置两套账号：`只读账号`、`可写账号`。因此可能的配置如下：
`ds.test-m1-read.jdbc.url = url`
`ds.test-m1-read.jdbc.user = dpUser_r`
`ds.test-m1-read.jdbc.password = password_r`
`.....(省略其他配置).....`
`ds.test-m1-write.jdbc.url = url`
`ds.test-m1-write.jdbc.user = dpUser`
`ds.test-m1-write.jdbc.password = password`
`.....(省略其他配置).....`

* 如果还有其他从库，那么对于每一台从库，也需要配置两套账号，因此可以能的配置如下：
`ds.test-s1-read.jdbc.url = url`
`ds.test-s1-read.jdbc.user = dpUser_r`
`ds.test-s1-read.jdbc.password = password_r`
`.....(省略其他配置).....`
`ds.test-s1-write.jdbc.url = url`
`ds.test-s1-write.jdbc.user = dpUser`
`ds.test-s1-write.jdbc.password = password`
`.....(省略其他配置).....`

* 对于properties参数，这里可以给出默认值，DBA拷贝粘贴一下即可：
initialPoolSize=10&maxPoolSize=20&minPoolSize=10&idleConnectionTestPeriod=60&acquireRetryAttempts=50&acquireRetryDelay=300&maxStatements=0&numHelperThreads=6&maxAdministrativeTaskTime=5&preferredTestQuery=SELECT 1&checkoutTimeout=3000

####  动态数据源配置`groupds`(所有配置可以在Lion中的groupds项目下查到)
配置key的命名规范是：groupds.`name`.mapping，其中`name`就是业务使用的`jdbcRef`。

value的规范是：(test-s1-read:1,test-m1-read:1),(test-m1-write)
第一个括号配置的是`读库`，多个读库以`,`符号分隔，`数字`表示分流的`权重`。
第二个括号配置的是`写库`，需要将集群中所有的instance的写账号配置在这里。

####  组合数据源配置`groupds`特殊配置
groupds.`name`.single.mapping：配置该规则后，一个不使用`dpdl`的数据源也将自动替换成`GroupDataSource`。

groupds.`name`.mapping.`app_name`：配置该规则后，一个应用会优先使用独立配置，而不使用统一配置。

groupds.`name`.single.mapping.`app_name`：以上两条可以规则可以组合使用


####  groupds隔离原则
1. 对于三级甚至重要性比较低的两级业务，如果是同一个库，可以使用同一个`name`给业务方当做`jdbcRef`使用。
2. 对于一级业务或者比较重要的二级业务，虽然是同一个库，但建议给不同的名字。

* 这样做目的是DBA在对某个业务库进行运维操作时，不会影响到别的业务，或者不被别的业务影响。做到业务隔离。