package com.dianping.zebra.group.filter.wall;

import java.security.NoSuchAlgorithmException;

import com.dianping.zebra.util.StringUtils;

public class SqlFlowIdGenerator {

	public static void main(String[] args) throws NoSuchAlgorithmException {
		String token = String.format("/*%s*/%s", "dianpingm3-m1-write", "SwitchsInfo.getAllSwitchsInfo");
		String resultId = StringUtils.md5(token).substring(0, 8);

		System.out.println(resultId);
   }
}
