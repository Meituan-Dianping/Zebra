package com.dianping.zebra.administrator.dto.jdbcref;

import java.util.Date;

/**
 * @author Created by tong.xin on 18/3/7.
 */
public class JdbcrefDto {
    private String jdbcref;

    private String env;

    private DBConfigInfoDto groupConfig;

    private String owner;

    private Date updateTime;

    public String getJdbcref() {
        return jdbcref;
    }

    public void setJdbcref(String jdbcref) {
        this.jdbcref = jdbcref;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public DBConfigInfoDto getGroupConfig() {
        return groupConfig;
    }

    public void setGroupConfig(DBConfigInfoDto groupConfig) {
        this.groupConfig = groupConfig;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
