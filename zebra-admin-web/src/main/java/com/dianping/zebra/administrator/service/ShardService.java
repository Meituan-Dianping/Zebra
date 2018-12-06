package com.dianping.zebra.administrator.service;

import com.dianping.zebra.administrator.dto.shard.ShardConfigDto;

/**
 * @author canhuang
 * @date 2018/3/25
 */
public interface ShardService {

    void addRuleName(String ruleName, String zkAddr, String owner, String desc);

    void saveRuleNameConfig(ShardConfigDto shardConfigDto);

    void deleteRuleNameConfig(String ruleName, String env);
}
