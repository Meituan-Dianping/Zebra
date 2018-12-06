package com.dianping.zebra.administrator.dto.jdbcref;

import java.util.List;

/**
 * Created by taochen on 2018/11/7.
 */
public class JdbcrefConfigDto {

    private String jdbcref;

    private String env;

    private List<DBAddressDto> dbAddresses;

    private String dbName;

    private String owner;

    private String description;

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

    public List<DBAddressDto> getDbAddresses() {
        return dbAddresses;
    }

    public void setDbAddresses(List<DBAddressDto> dbAddresses) {
        this.dbAddresses = dbAddresses;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
