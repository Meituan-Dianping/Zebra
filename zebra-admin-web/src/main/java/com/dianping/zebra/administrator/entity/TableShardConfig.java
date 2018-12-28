package com.dianping.zebra.administrator.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by taochen on 2018/11/13.
 */
@XmlRootElement(name = "tableShardConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class TableShardConfig {
    private String tableName;

    @XmlElement(name = "dimensionConfigs")
    private DimensionConfigs dimensionConfigs;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public DimensionConfigs getDimensionConfigs() {
        return dimensionConfigs;
    }

    public void setDimensionConfigs(DimensionConfigs dimensionConfigs) {
        this.dimensionConfigs = dimensionConfigs;
    }
}
