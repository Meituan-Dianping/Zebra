package com.dianping.zebra.single;

import com.dianping.zebra.single.jdbc.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        // jdbc
        SingleDataSourceTest.class,
        C3p0SingleDataSourceFieldTest.class,
        DruidSingleDataSourceTest.class,
        TomcatJdbcSingleDataSourceFieldTest.class,
        //dbcp2和dbcp不能同时测试，需要切换jdk版本
        Dbcp2SingleDataSourceFieldTest.class,
        DbcpSingleDataSourceFieldTest.class,


})
public class AllTests {

}