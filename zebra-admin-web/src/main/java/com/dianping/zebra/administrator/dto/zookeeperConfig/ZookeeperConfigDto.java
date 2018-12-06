package com.dianping.zebra.administrator.dto.zookeeperConfig;

/**
 * Created by taochen on 2018/11/16.
 */
public class ZookeeperConfigDto {
    private String host;

    private String name;

    private String description;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
