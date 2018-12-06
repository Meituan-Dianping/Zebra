package com.dianping.zebra.sample.dao.group;

import com.dianping.zebra.sample.entity.group.UserEntity;
import org.apache.ibatis.annotations.Param;

/**
 * @author tong.xin on 2018/10/29.
 */
public interface UserDao {
	void insert(UserEntity user);

	UserEntity searchUserById(@Param("id") int id);
}
