package com.dianping.zebra.administrator.dto.shard;


import java.util.List;

/**
 * Created by taochen on 2018/11/13.
 */

public class ShardConfigDto {

    private String ruleName;

    private String env;

    private List<TableShardConfigDto> tableShardConfigs;

    public List<TableShardConfigDto> getTableShardConfigs() {
        return tableShardConfigs;
    }

    public void setTableShardConfigs(List<TableShardConfigDto> tableShardConfigs) {
        this.tableShardConfigs = tableShardConfigs;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }
}
