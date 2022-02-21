package com.dianping.zebra.administrator.mapper;

import com.dianping.zebra.administrator.entity.ShardEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Created by tong.xin on 18/3/7.
 */
public interface ShardMapper {
    void create(ShardEntity entity);

    List<ShardEntity> findByEnv(@Param("env") String env);

	void createRule(ShardEntity shardEntity);

	void deleteRuleName(@Param("ruleName") String ruleName, @Param("env") String env);
}
