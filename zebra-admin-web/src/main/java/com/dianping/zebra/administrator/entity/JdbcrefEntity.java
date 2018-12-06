package com.dianping.zebra.administrator.entity;

import java.util.Date;

/**
 * @author Created by tong.xin on 18/3/7.
 */
public class JdbcrefEntity {
    protected int id;

    protected String jdbcref;

    protected int status;

    protected String env;

    protected String owner;

    protected String description;

    protected Date updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getJdbcref() {
        return jdbcref;
    }

    public void setJdbcref(String jdbcref) {
        this.jdbcref = jdbcref;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
