package com.dianping.zebra.administrator.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by taochen on 2018/11/13.
 */
@XmlRootElement(name = "tableShardConfigs")
@XmlAccessorType(XmlAccessType.FIELD)
public class ShardConfig {

    @XmlElement(name = "tableShardConfig")
    private List<TableShardConfig> tableShardConfigs;

    public List<TableShardConfig> getTableShardConfigs() {
        return tableShardConfigs;
    }

    public void setTableShardConfigs(List<TableShardConfig> tableShardConfigs) {
        this.tableShardConfigs = tableShardConfigs;
    }
}
