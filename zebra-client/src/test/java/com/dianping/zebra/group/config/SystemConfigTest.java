package com.dianping.zebra.group.config;

import com.dianping.zebra.group.config.system.entity.SqlFlowControl;
import com.dianping.zebra.group.config.system.entity.SystemConfig;
import com.dianping.zebra.group.config.system.transform.DefaultSaxParser;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

public class SystemConfigTest {
    @Test
    public void test() throws IOException, SAXException {
        SystemConfig config = new SystemConfig();
        config.setRetryTimes(3);
        SqlFlowControl flowControl = new SqlFlowControl();
        flowControl.setSqlId("12345678");
        flowControl.setApp("app1");
        flowControl.setAllowPercent(10);
        config.addSqlFlowControl(flowControl);

        flowControl= new SqlFlowControl();
        flowControl.setSqlId("12345678");
        flowControl.setApp("app2");
        flowControl.setAllowPercent(20);
        config.addSqlFlowControl(flowControl);

        flowControl= new SqlFlowControl();
        flowControl.setSqlId("12345679");
        flowControl.setApp("app1");
        flowControl.setAllowPercent(20);
        config.addSqlFlowControl(flowControl);

        flowControl= new SqlFlowControl();
        flowControl.setSqlId("12345677");
        flowControl.setApp("app4");
        flowControl.setAllowPercent(20);
        config.addSqlFlowControl(flowControl);

        String configStr = config.toString();
        System.out.println(configStr);
        SystemConfig newConfig = DefaultSaxParser.parse(configStr);
        Assert.assertEquals(config, newConfig);
    }
}
