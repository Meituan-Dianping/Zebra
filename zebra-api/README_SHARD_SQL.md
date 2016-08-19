### SQL 支持列表

假设有一张表：User，主维度是：Id，辅维度是：CityId

#### INSERT

##### 插入时必须指定主键，暂不支持自动生成主键

* INSERT INTO User WHERE (Id, Name, CityId) VALUES (1, 'test', 2)

#### UPDATE & DELETE

##### 更新操作最好指定主维度，这样性能高

* UPDATE User SET Name = 'new name' WHERE Id = 1
* DELETE FROM User WHERE Id = 1

##### 这里虽然有主维度，但是这类操作会操作所有的主维度表，性能较差

* UPDATE User SET Name = 'new name' WHERE Id > 1
* UPDATE User SET Name = 'new name' WHERE Id <> 1
* DELETE FROM User WHERE Id > 1
* DELETE FROM User WHERE Id <> 1

##### 使用辅维度，但是也会操作所有主维度，性能较差

  * UPDATE User SET Name = 'new name' WHERE CityId = 2
  * DELETE FROM User WHERE CityId = 2

##### 使用其他字段或者不加条件，都会操作所有主维度

  * UPDATE User SET Name = 'new name' WHERE Name = 'test'
  * DELETE FROM User WHERE Name = 'test'

#### SELECT

##### 指定主维度或者辅维度，都可以快速查询到数据，性能高

  * SELECT Id, Name, CityId WHERE Id = 1
  * SELECT Id, Name, CityId WHERE CityId = 1
  * SELECT Id, Name, CityId WHERE Id = 2 AND CityId = 1
  * SELECT Id, Name, CityId WHERE Id IN (1,2,3,4)
  * SELECT Id, Name, CityId WHERE Id = 1 OR Id = 2

##### 不指定主维度或者辅维度，会查询所有主维度的表，性能差

  * SELECT Id, Name, CityId FROM User
  * SELECT Id, Name, CityId FROM User WHERE Id > 1
  * SELECT Id, Name, CityId FROM User WHERE Id <> 1
  * SELECT Id, Name, CityId FROM User WHERE Name LIKE '%test%'


##### LIMIT, OFFSET, ORDER BY 都支持，性能差

  * SELECT Id, Name, CityId FROM User ORDER BY CityId LIMIT 1 OFFSET 1


##### 支持子查询，但无法识别子查询中的分区字段，性能差

  * SELECT Id, Name, CityId FROM User WHERE Id IN (SELECT Id FROM User WHERE CityId = 1)


##### 支持 GROUP BY, COUNT, MAX, MIN 都支持，AVG 不支持，性能差，另外必须加字段别名

  * SELECT CityId, MAX(Id) as MaxId FROM User GROUP BY CityId
  * SELECT CityId, MIN(Id) as MinId FROM User GROUP BY CityId
  * SELECT CityId, COUNT(Id) as AllId FROM User GROUP BY CityId