package com.dianping.zebra.administrator.dto.shard;

import com.dianping.zebra.administrator.entity.DimensionConfig;
import com.dianping.zebra.shard.config.TableShardDimensionConfig;

import java.util.List;

/**
 * Created by taochen on 2018/11/13.
 */
public class TableShardConfigDto {
    private String tableName;

    private List<TableShardDimensionConfig> dimensionConfigs;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<TableShardDimensionConfig> getDimensionConfigs() {
        return dimensionConfigs;
    }

    public void setDimensionConfigs(List<TableShardDimensionConfig> dimensionConfigs) {
        this.dimensionConfigs = dimensionConfigs;
    }
}
