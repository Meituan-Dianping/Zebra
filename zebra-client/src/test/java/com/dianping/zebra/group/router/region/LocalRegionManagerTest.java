package com.dianping.zebra.group.router.region;

import com.dianping.zebra.Constants;
import org.junit.Test;

import junit.framework.Assert;

public class LocalRegionManagerTest {

    @Test
    public void test() throws Exception{

        ZebraRegionManager instance = new LocalRegionManager();
        instance.init();

        Assert.assertEquals(false, instance.isInSameIdc("192.168.1.1", "192.168.1.3"));
        Assert.assertEquals(true, instance.isInSameIdc("192.20.1.1", "192.21.1.3"));
        Assert.assertEquals(true, instance.isInSameIdc("192.3.1.1", "192.4.1.3"));
        Assert.assertEquals(true, instance.isInSameIdc("192.4.1.1", "192.4.12.3"));
        Assert.assertEquals(false, instance.isInSameIdc("192.4.1.1", "192.13.1.3"));
        Assert.assertEquals(true, instance.isInSameIdc("192.4.1.1", "192.3.1.3"));
        Assert.assertEquals(true, instance.isInSameIdc("192.11.1.1", "192.10.1.3"));
        Assert.assertEquals(true, instance.isInSameIdc("192.21.1.1", "192.20.1.3"));
    }

    @Test
    public void testInitFromFile() {
        LocalRegionManager manager = new LocalRegionManager();
        manager.init();

        Assert.assertEquals(AbstractZebraRegionManager.NO_CENTER, manager.findCenter("122.75.255.255"));
        Assert.assertEquals(AbstractZebraRegionManager.NO_CENTER, manager.findCenter("111.67.1.213"));
        Assert.assertEquals(AbstractZebraRegionManager.NO_CENTER, manager.findCenter("192.25.1.1"));
        Assert.assertEquals(AbstractZebraRegionManager.NO_CENTER, manager.findCenter("192.9.1.255"));
        Assert.assertEquals(AbstractZebraRegionManager.NO_CENTER, manager.findCenter("188.16.255.1"));
        Assert.assertEquals(AbstractZebraRegionManager.NO_CENTER, manager.findCenter("192.124.255.1"));
        Assert.assertEquals("center1", manager.findCenter("192.1.255.255"));
        Assert.assertEquals("center1", manager.findCenter("192.1.1.1"));
        Assert.assertEquals("center1", manager.findCenter("192.2.255.255"));
        Assert.assertEquals("center1", manager.findCenter("192.2.1.1"));
        Assert.assertEquals("center1", manager.findCenter("192.3.1.1"));
        Assert.assertEquals("center1", manager.findCenter("192.3.255.255"));
        Assert.assertEquals("center1", manager.findCenter("192.5.1.1"));

        Assert.assertEquals("center2", manager.findCenter("192.6.1.1"));
        Assert.assertEquals("center2", manager.findCenter("192.6.255.1"));

        Assert.assertTrue(manager.isInSameCenter("192.3.255.255", "192.3.1.1"));
        Assert.assertTrue(manager.isInSameCenter("192.3.1.1", "192.4.1.1"));
        Assert.assertTrue(manager.isInSameCenter("192.4.1.1", "192.3.33.1"));
        Assert.assertTrue(manager.isInSameCenter("192.4.1.1", "192.3.22.5"));
        Assert.assertTrue(manager.isInSameCenter("192.10.1.1", "192.11.1.1"));


        Assert.assertTrue(manager.isInSameCenter("192.5.1.1", "192.5.45.2"));
        Assert.assertTrue(manager.isInSameCenter("192.10.11.1", "192.20.1.11"));
        Assert.assertTrue(manager.isInSameCenter("192.4.1.1", "192.5.1.255"));

        Assert.assertFalse(manager.isInSameCenter("192.3.1.1", "192.11.15.1"));
        Assert.assertFalse(manager.isInSameCenter("192.20.1.1", "192.1.1.1"));
        Assert.assertFalse(manager.isInSameCenter("192.6.1.1", "192.21.1.255"));

        manager.setLocalAddress("192.3.1.5");
        Assert.assertTrue(manager.isInLocalCenter("192.3.255.255"));
        Assert.assertTrue(manager.isInLocalCenter("192.1.255.255"));
        Assert.assertTrue(manager.isInLocalCenter("192.2.255.255"));
        Assert.assertTrue(manager.isInLocalCenter("192.4.255.255"));
        Assert.assertTrue(manager.isInLocalCenter("192.2.25.255"));
        Assert.assertTrue(manager.isInLocalCenter("192.5.255.255"));
        Assert.assertFalse(manager.isInLocalCenter("192.6.255.255"));
        Assert.assertFalse(manager.isInLocalCenter("192.10.255.255"));
        Assert.assertFalse(manager.isInLocalCenter("192.11.255.255"));
        Assert.assertFalse(manager.isInLocalCenter("192.20.255.255"));

        manager.setLocalAddress("192.10.1.5");
        Assert.assertTrue(manager.isInLocalCenter("192.10.255.255"));
        Assert.assertTrue(manager.isInLocalCenter("192.11.255.255"));
        Assert.assertTrue(manager.isInLocalCenter("192.20.255.255"));
        Assert.assertFalse(manager.isInLocalCenter("192.1.255.255"));
        Assert.assertFalse(manager.isInLocalCenter("192.2.25.25"));
        Assert.assertFalse(manager.isInLocalCenter("192.3.25.25"));
        Assert.assertFalse(manager.isInLocalCenter("192.4.15.255"));
        Assert.assertFalse(manager.isInLocalCenter("192.1.5.255"));
        Assert.assertFalse(manager.isInLocalCenter("192.2.155.255"));
        Assert.assertFalse(manager.isInLocalCenter("192.3.35.255"));

        manager.setLocalAddress("192.6.1.5");
        Assert.assertTrue(manager.isInLocalCenter("192.6.255.255"));
        Assert.assertFalse(manager.isInLocalCenter("192.4.255.255"));
        Assert.assertFalse(manager.isInLocalCenter("192.11.5.255"));
        Assert.assertFalse(manager.isInLocalCenter("192.10.255.255"));
        Assert.assertFalse(manager.isInLocalCenter("192.11.255.255"));
        Assert.assertFalse(manager.isInLocalCenter("192.20.255.255"));
        Assert.assertFalse(manager.isInLocalCenter("192.21.255.255"));
        Assert.assertFalse(manager.isInLocalCenter("192.1.255.255"));
        Assert.assertFalse(manager.isInLocalCenter("192.2.255.255"));
        Assert.assertFalse(manager.isInLocalCenter("192.3.255.255"));
        Assert.assertFalse(manager.isInLocalCenter("192.20.25.25"));
    }

}
