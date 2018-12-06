package com.dianping.zebra.sample.dao.shard;

import com.dianping.zebra.sample.entity.shard.FeedEntity;
import org.apache.ibatis.annotations.Param;

/**
 * @author tong.xin on 2018/11/8.
 */
public interface FeedDao {
	void insert(FeedEntity feedEntity);

	FeedEntity searchFeedByUid(@Param("uid") String uid);
}
