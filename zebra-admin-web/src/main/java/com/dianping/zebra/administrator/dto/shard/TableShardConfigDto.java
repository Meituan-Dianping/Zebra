package com.dianping.zebra.administrator.dto.shard;

import com.dianping.zebra.administrator.entity.DimensionConfig;

import java.util.List;

/**
 * Created by taochen on 2018/11/13.
 */
public class TableShardConfigDto {
    private String tableName;

    private boolean global;

    private List<DimensionConfig> dimensionConfigs;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public List<DimensionConfig> getDimensionConfigs() {
        return dimensionConfigs;
    }

    public void setDimensionConfigs(List<DimensionConfig> dimensionConfigs) {
        this.dimensionConfigs = dimensionConfigs;
    }
}
