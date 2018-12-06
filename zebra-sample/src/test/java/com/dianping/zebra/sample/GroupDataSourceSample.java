package com.dianping.zebra.sample;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dianping.zebra.sample.dao.group.UserDao;
import com.dianping.zebra.sample.entity.group.UserEntity;

/**
 * @author tong.xin on 2018/10/29.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:spring/group/group-db-config.xml"})
public class GroupDataSourceSample {

	@Autowired
	private UserDao userDao;

	@Test
	public void test(){
		UserEntity user = new UserEntity();
		user.setName("test_man");
		user.setMis("test_group");
		user.setEmail("test_man@meituan.com");

		userDao.insert(user);
		UserEntity searchUser = userDao.searchUserById(user.getId());

		System.out.println(searchUser.getName());
		System.out.println(searchUser.getMis());
		System.out.println(searchUser.getEmail());
	}
}
