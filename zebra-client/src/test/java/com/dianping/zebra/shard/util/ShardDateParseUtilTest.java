package com.dianping.zebra.shard.util;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;


/**
 * Created by wxl on 17/4/13.
 */

public class ShardDateParseUtilTest {
    @Test
    public void testParseDate() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ShardDateParseUtil.ShardDate sd = ShardDateParseUtil.parseToYMD("yyyy-MM-dd", format.parse("2017-02-28"));
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2017, 1, 27), sd);

        sd = ShardDateParseUtil.parseToYMD("yyyy-MM-dd", format.parse("2017-03-01"));
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2017, 2, 0), sd);

        sd = ShardDateParseUtil.parseToYMD("yyyy-MM-dd", format.parse("2016-02-29"));
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2016, 1, 28), sd);

        sd = ShardDateParseUtil.parseToYMD("yyyy-MM-dd", format.parse("2017-03-31"));
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2017, 2, 30), sd);

        sd = ShardDateParseUtil.parseToYMD("yyyy-MM-dd", format.parse("2017-12-31"));
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2017, 11, 30), sd);

        sd = ShardDateParseUtil.parseToYMD("yyyy-MM-dd", format.parse("2017-01-01"));
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2017, 0, 0), sd);

    }

    @Test
    public void testParseStr() {
        ShardDateParseUtil.ShardDate sd = ShardDateParseUtil.parseToYMD("yyyy-MM-dd", "2017-02-28");
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2017, 1, 27), sd);

        sd = ShardDateParseUtil.parseToYMD("yyyy-MM-dd", "2017-03-01");
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2017, 2, 0), sd);

        sd = ShardDateParseUtil.parseToYMD("yyyy-MM-dd", "2016-02-29");
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2016, 1, 28), sd);

        sd = ShardDateParseUtil.parseToYMD("yyyy-MM-dd", "2017-03-31");
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2017, 2, 30), sd);

        sd = ShardDateParseUtil.parseToYMD("yyyy-MM-dd", "2017-12-31");
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2017, 11, 30), sd);

        sd = ShardDateParseUtil.parseToYMD("yyyy-MM-dd", "2017-01-01");
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2017, 0, 0), sd);
    }

//    @Test
//    public void testTime() throws ParseException {
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        Date date = format.parse("2017-02-28");
//        ShardDateParseUtil.ShardDate sd = null;
//        int loop = 1000000;
////        ShardDateParseUtil.ShardDate sd = ShardDateParseUtil.parseToYMD(format.parse("2017-02-28"));
//
//        long s = System.currentTimeMillis();
//        for(int i = 0; i < loop; ++i) {
//            sd = ShardDateParseUtil.parseToYMD("2017-02-28", "yyyy-MM-dd");
//        }
//        long e = System.currentTimeMillis();
//        System.out.println("t = "+(e-s)+"ms");
//
//        s = System.currentTimeMillis();
//        for(int i = 0; i < loop; ++i) {
//            sd = ShardDateParseUtil.parseToYMD(new Date());
//        }
//        e = System.currentTimeMillis();
//        System.out.println("t = "+(e-s)+"ms");
//    }

    @Test
    public void testAddDay() {
        ShardDateParseUtil.ShardDate sd = ShardDateParseUtil.addDay("yyyy-MM-dd", "2016-02-28", 1);
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2016, 1, 28), sd);

        sd = ShardDateParseUtil.addDay("yyyy-MM-dd", "2017-02-28", 1);
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2017, 2, 0), sd);

        sd = ShardDateParseUtil.addDay("yyyy-MM-dd", "2016-12-31", 1);
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2017, 0, 0), sd);

        sd = ShardDateParseUtil.addDay("yyyy-MM-dd", "2016-09-30", 1);
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2016, 9, 0), sd);

        sd = ShardDateParseUtil.addDay("yyyy-MM-dd", "2016-03-01", -1);
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2016, 1, 28), sd);

        sd = ShardDateParseUtil.addDay("yyyy-MM-dd", "2017-03-01", -1);
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2017, 1, 27), sd);

        sd = ShardDateParseUtil.addDay("yyyy-MM-dd", "2016-01-01",  -1);
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2015, 11, 30), sd);

        sd = ShardDateParseUtil.addDay("yyyy-MM-dd", "2016-12-01", -1);
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2016, 10, 29), sd);

    }

    @Test
    public void testAddMonth() {
        ShardDateParseUtil.ShardDate sd = ShardDateParseUtil.addMonth("yyyy-MM-dd", "2016-02-28", 1);
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2016, 2, 27), sd);

        sd = ShardDateParseUtil.addMonth("yyyy-MM-dd", "2016-03-31", 1);
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2016, 3, 29), sd);

        sd = ShardDateParseUtil.addMonth("yyyy-MM-dd", "2016-03-01", -1);
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2016, 1, 0), sd);

        sd = ShardDateParseUtil.addMonth("yyyy-MM-dd", "2016-01-01", -1);
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2015, 11, 0), sd);

        sd = ShardDateParseUtil.addMonth("yyyy-MM-dd", "2016-01-31", -1);
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2015, 11, 30), sd);

        sd = ShardDateParseUtil.addMonth("yyyy-MM-dd", "2016-12-31", 1);
        Assert.assertEquals(new ShardDateParseUtil.ShardDate(2017, 0, 30), sd);
    }
}
