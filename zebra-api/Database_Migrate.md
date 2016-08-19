## 为什么你需要DAL来进行迁库或者拆库？
和老的修改Lion配置相比，使用DAL来进行迁库或者拆库有以下优势：
1. 业务实时切换，无需重启，没有宕机时间
2. 优雅切换，理论上无错误
3. 简化切换操作，只需修改几条Lion配置，整个切换流程耗时短

## DAL如何帮助你来进行迁库或者拆库？

以下均以`tuangou2010`为例

### 迁库
1. 保证访问`tuangou2010`这个库的业务均已升级了DAL（如需了解如何升级DAL，请参看[README.md](/arch/zebra/blob/master/README.md)）
2. DBA操作修改Lion，将jdbcRef = `tuangou2010`的这个dal配置从原来的库，修改指向新的库，完成切换


### 拆库
假如想从`tuangou2010`库中拆除一个做团单的库，库名叫`tuangoudeal`，可能的步骤如下：

1. DBA创建新的jdbcRef = `tuangoudeal`，使这个Ref使用`tuangou2010`的值，这样保证`tuangoudeal`和`tuangou2010`访问相同的库
2. 开发修改相应业务，新建一个DAL的`datasource`，使它的jdbcRef = `tuangoudeal`，有关访问tuangoudeal中表的sql均使用该`datasource`
3. 开发将所有相应业务都上线
4. DBA进行拆分库和老库之间数据同步
5. DBA操作修改Lion，将jdbcRef = `tuangoudeal`的这个dal配置从原来的库，修改指向新的库，完成切换

### 重要
以上过程是一个大概的过程，每个库在进行实际操作时，可能会有稍许的差别，但总的流程不变。

## 案例分享
1. 2014年09月	dpreview库迁库
2. 2014年11月	社区库拆库出活动库  [详细方案](http://wiki.dp/pages/viewpage.action?pageId=8978623)
3. 2014年12月	社区库迁库
4. 2014年12月	dianpinguc库迁库

        时间        读库QPS     写库QPS     出错个数
        << 1分钟    200         80          1000左右              
    
5. 2014年12月   tpd_deal库迁库

        时间        读库QPS     写库QPS     出错个数
        << 1分钟    150         20          0
        
6. 2015年01月	dianping库拆库到dpshop
7. 2015年01月   tuangou2010库拆库到tgdeal
8. 2015年03月  mopay库和DianPingHui库迁库
9. 2015年04月  dpopenplatform库迁库
