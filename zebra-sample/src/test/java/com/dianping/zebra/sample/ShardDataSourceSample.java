package com.dianping.zebra.sample;

import com.dianping.zebra.sample.dao.group.UserDao;
import com.dianping.zebra.sample.dao.shard.FeedDao;
import com.dianping.zebra.sample.entity.group.UserEntity;
import com.dianping.zebra.sample.entity.shard.FeedEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author tong.xin on 2018/10/29.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:spring/shard/shard-local-config.xml"})
public class ShardDataSourceSample {

	@Autowired
	private FeedDao feedDao;

	@Test
	public void test(){
		String uid = "123456";
		FeedEntity feed = new FeedEntity();
		feed.setUid(uid);

		feedDao.insert(feed);

		FeedEntity searchFeed = feedDao.searchFeedByUid(uid);

		System.out.println(searchFeed.getUid());
		System.out.println(searchFeed.getUpdateTime());
	}
}
