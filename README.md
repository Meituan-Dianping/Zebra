## zebra

[![Build Status](https://travis-ci.org/dianping/zebra.svg?branch=master)](https://travis-ci.org/dianping/zebra)

## 简介
`Zebra`是点评内部使用的`数据库访问层`中间件，它具有以下的功能点：

1. 配置集中管理，动态刷新     
2. 支持读写分离、分库分表
3. 丰富的监控信息在`CAT`上展现

其中的三个组件的功能分别是：

 - zebra-api : 最主要的访问层中间件
 - zebra-ds-monitor-client：基于CAT的监控(可选)
 - zebra-dao：基于MyBatis的异步化的DAO组件(可选)

## 编译

    1. git clone https://github.com/dianping/zebra.git
    2. git checkout mvn-repo
    3. 拷贝里面的mvn依赖到本地仓库（第2和3步骤主要是为了使用zebra-ds-monitor-client中的CAT监控）
    4. mvn clean install -DskipTests

## 使用

 - zebra-api : [文档](https://github.com/dianping/zebra/zebra-api/blob/master/README.md)
 - zebra-ds-monitor-client
 - zebra-dao：[文档](https://github.com/dianping/zebra/zebra-dao/blob/master/README_ZH.md)

## Copyright and License

Copyright 2016 DianPing, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
