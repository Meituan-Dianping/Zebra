package com.dianping.zebra.administrator.dto.shard;

/**
 * @author Created by tong.xin on 18/3/7.
 */
public class ShardDto {
    private String ruleName;

    private String ZKName;

    private String owner;

    private String desc;

    private String updateTime;

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getZKName() {
        return ZKName;
    }

    public void setZKName(String ZKName) {
        this.ZKName = ZKName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
