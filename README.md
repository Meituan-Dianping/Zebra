## zebra

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
