# Zebra

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

## 简介

Zebra 是一个基于 JDBC API 协议上开发出的**高可用、高性能**的数据库访问层解决方案，也是美团点评官方大规模使用的数据库访问层中间件。

Zebra 具有以下的功能：

 - 配置集中管理，动态刷新
 - 支持读写分离、分库分表
 - 丰富的监控信息在 CAT 上展现
 - 异步化数据库请求，多数据源支持

## 核心价值

 - 简化了读写分离、分库分表的开发工作，使得业务方在分库分库、读写分离的情况下，依然可以像操作单个库那样去操作，屏蔽底层实现的复杂性，对业务透明。
 - 提供了从读写分离到分库分表全生命周期的技术支持。
 - 完善的监控体系帮助开发掌控数据库请求的整个链路，快速定位问题。
 - 基于 mybatis 改造的 zebra-dao 扩展功能

## 开源模块

 - zebra-client（核心）：除了监控外，几乎zebra所有核心功能，如**读写分离、分库分表、就近路由、流量控制**  
 - zebra-cat-client（可选）：提供端到端的监控，将监控信息上报到 [CAT监控平台](https://github.com/dianping/cat)    
 - zebra-dao （可选）：对 mybatis 的轻量级封装，兼容 mybatis 原有的功能，并额外提供了**异步化接口、分页插件、多数据源**等功能  
 - zebra-admin-web：zebra 配置管理平台，用于管理 zk 和保存在 zk 中的 zebra 配置
 - zebra-sample：zebra 客户端使用的 demo

## 快速开始

 - [快速开始](https://github.com/Meituan-Dianping/Zebra/wiki/QuickStart)

## 架构设计

 - [Zebra客户端设计](https://github.com/Meituan-Dianping/Zebra/wiki/Zebra%E6%80%BB%E4%BD%93%E8%AE%BE%E8%AE%A1)

## License

 - [Apache2.0 License](https://github.com/Meituan-Dianping/Zebra/blob/master/LICENSE)

## 谁在使用

 ![](https://raw.githubusercontent.com/wiki/Meituan-Dianping/Zebra/image/white.jpeg) 

接入Zebra 的公司欢迎在此[接入公司](https://github.com/Meituan-Dianping/Zebra/issues/18)留下联系方式, 谢谢。

## 联系我们

 - Mail: [zebra@sankuai.com](zebra@sankuai.com)
 - Issues : [https://github.com/Meituan-Dianping/Zebra/issues](https://github.com/Meituan-Dianping/Zebra/issues)
 - WeChat : [Zebra微信交流群](https://github.com/Meituan-Dianping/Zebra/wiki/WeChat%E4%BA%A4%E6%B5%81%E7%BE%A4)