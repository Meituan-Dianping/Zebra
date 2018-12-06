package com.dianping.zebra.shard.router.rule;

import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.shard.router.rule.tableset.DefaultTableSetsManager;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * Created by wxl on 17/4/10.
 */

public class DefaultTableSetsManagerTest {
    @Test
    public void testTableRule() {
        Map<String, Set<String>> resultMap = new HashMap<String, Set<String>>();
        Set<String> s0 = new LinkedHashSet<String>();
        Set<String> s1 = new LinkedHashSet<String>();
        Set<String> s3 = new LinkedHashSet<String>();
        resultMap.put("database0", s0);
        resultMap.put("database1", s1);

        DefaultTableSetsManager dtsm = new DefaultTableSetsManager("TestTable", "database[0-1]", "everydb:[0,3]");
        Map<String, Set<String>> tableSets = dtsm.getAllTableSets();
        s0.clear(); s0.add("TestTable0"); s0.add("TestTable1"); s0.add("TestTable2"); s0.add("TestTable3");
        s1.clear(); s1.add("TestTable0"); s1.add("TestTable1"); s1.add("TestTable2"); s1.add("TestTable3");
        Assert.assertEquals(resultMap, tableSets);

        dtsm = new DefaultTableSetsManager("TestTable", "database[0-1]", "alldb:[0,3]");
        tableSets = dtsm.getAllTableSets();
        s0.clear(); s0.add("TestTable0"); s0.add("TestTable1");
        s1.clear(); s1.add("TestTable2"); s1.add("TestTable3");
        Assert.assertEquals(resultMap, tableSets);


        dtsm = new DefaultTableSetsManager("TestTable", "database[0-1]", "database0:[_2017]");
        tableSets = dtsm.getAllTableSets();
        s0.clear(); s0.add("TestTable_2017");
        s1.clear();
        Assert.assertEquals(resultMap, tableSets);

        dtsm = new DefaultTableSetsManager("TestTable", "database[0-1]", "database0:[$,_2017] & alldb:[0,3]");
        tableSets = dtsm.getAllTableSets();
        s0.clear(); s0.add("TestTable"); s0.add("TestTable_2017"); s0.add("TestTable0"); s0.add("TestTable1");
        s1.clear(); s1.add("TestTable2"); s1.add("TestTable3");
        Assert.assertEquals(resultMap, tableSets);

        dtsm = new DefaultTableSetsManager("TestTable", "database[0-1]", "database0:[$,_2017] & alldb:[0,3] & database1:[_201701,_201702]");
        tableSets = dtsm.getAllTableSets();
        s0.clear(); s0.add("TestTable"); s0.add("TestTable_2017"); s0.add("TestTable0"); s0.add("TestTable1");
        s1.clear(); s1.add("TestTable2"); s1.add("TestTable3"); s1.add("TestTable_201701"); s1.add("TestTable_201702");
        Assert.assertEquals(resultMap, tableSets);

        dtsm = new DefaultTableSetsManager("TestTable", "database[0-1]", "database1:[$,_2017] & everydb:[0,1]");
        tableSets = dtsm.getAllTableSets();
        s0.clear(); s0.add("TestTable0"); s0.add("TestTable1");
        s1.clear(); s1.add("TestTable"); s1.add("TestTable_2017"); s1.add("TestTable0"); s1.add("TestTable1");
        Assert.assertEquals(resultMap, tableSets);

        dtsm = new DefaultTableSetsManager("TestTable", "database[0-1]", "alldb:[0,3]&database0:[$]");
        tableSets = dtsm.getAllTableSets();
        s0.clear(); s0.add("TestTable0"); s0.add("TestTable1"); s0.add("TestTable");
        s1.clear(); s1.add("TestTable2"); s1.add("TestTable3");
        Assert.assertEquals(resultMap, tableSets);

        dtsm = new DefaultTableSetsManager("TestTable", "database[0-1],default", "database0:[_1, _3, $] & database1:[_2, _4] & default:[$]");
        tableSets = dtsm.getAllTableSets();
        s0.clear(); s0.add("TestTable_1"); s0.add("TestTable_3"); s0.add("TestTable");
        s1.clear(); s1.add("TestTable_2"); s1.add("TestTable_4");
        resultMap.put("default", s3);
        s3.clear(); s3.add("TestTable");
        Assert.assertEquals(resultMap, tableSets);
    }

    @Test
    public void testTableRule2() {
        Map<String, Set<String>> resultMap = new HashMap<String, Set<String>>();
        Set<String> s0 = new LinkedHashSet<String>();
        Set<String> s1 = new LinkedHashSet<String>();
        resultMap.put("database0", s0);
        resultMap.put("database1", s1);

        DefaultTableSetsManager dtsm = new DefaultTableSetsManager("TestTable", "database[0-1]", "database0:{0,3}&database1:[0,3]");
        Map<String, Set<String>> tableSets = dtsm.getAllTableSets();
        s0.clear(); s0.add("TestTable0"); s0.add("TestTable1"); s0.add("TestTable2"); s0.add("TestTable3");
        s1.clear(); s1.add("TestTable0"); s1.add("TestTable3");
        Assert.assertEquals(resultMap, tableSets);

        dtsm = new DefaultTableSetsManager("TestTable", "database[0-1]", "database0:{_bak0,_bak3}&database1:[0,1]");
        tableSets = dtsm.getAllTableSets();
        s0.clear(); s0.add("TestTable_bak0"); s0.add("TestTable_bak1"); s0.add("TestTable_bak2"); s0.add("TestTable_bak3");
        s1.clear(); s1.add("TestTable0"); s1.add("TestTable1");
        Assert.assertEquals(resultMap, tableSets);

        dtsm = new DefaultTableSetsManager("TestTable", "database[0-1]", "database0:{_bak0,_bak10}&database1:[0,1]", true);
        tableSets = dtsm.getAllTableSets();
        s0.clear(); s0.add("TestTable_bak00"); s0.add("TestTable_bak01"); s0.add("TestTable_bak02"); s0.add("TestTable_bak03"); s0.add("TestTable_bak04"); s0.add("TestTable_bak05"); s0.add("TestTable_bak06");s0.add("TestTable_bak07");s0.add("TestTable_bak08");s0.add("TestTable_bak09");s0.add("TestTable_bak10");
        s1.clear(); s1.add("TestTable0"); s1.add("TestTable1");
        Assert.assertEquals(resultMap, tableSets);
    }

    @Test
    public void testException() {
        try {
            new DefaultTableSetsManager("TestTable", "database[0-1]", "database0:[$,_2017] & alldb:[0,3] & database2:[_201701,_201702]");
        } catch (ZebraConfigException e) {
            Assert.assertEquals("Cannot find corresponding jdbcRef from db list!", e.getMessage());
        }

        try {
            new DefaultTableSetsManager("TestTable", "database[0-1]", "database0:[$,_2017] & alldbxx:[_0,_1]");
        } catch (ZebraConfigException e) {
            Assert.assertEquals("TbSuffix property can only be 'alldb' or 'everydb'.", e.getMessage());
        }
    }
}
