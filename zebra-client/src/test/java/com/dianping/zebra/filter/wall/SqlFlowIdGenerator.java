package com.dianping.zebra.filter.wall;

import java.security.NoSuchAlgorithmException;

import com.dianping.zebra.util.StringUtils;
import junit.framework.Assert;
import org.junit.Test;

public class SqlFlowIdGenerator {

	@Test
	public void test() throws NoSuchAlgorithmException {
		String token = String.format("/*%s*/%s", "dianpingm3-m1-write", "SwitchsInfo.getAllSwitchsInfo");
		String resultId = StringUtils.md5(token).substring(0, 8);

		Assert.assertEquals("f14b190b", resultId);
	}
}
