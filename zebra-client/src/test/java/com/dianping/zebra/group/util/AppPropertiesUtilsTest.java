package com.dianping.zebra.group.util;

import com.dianping.zebra.util.AppPropertiesUtils;
import org.junit.Assert;
import org.junit.Test;

public class AppPropertiesUtilsTest {

	@Test
	public void test(){
		Assert.assertEquals("zebra_ut", AppPropertiesUtils.getAppName());
	}
}
