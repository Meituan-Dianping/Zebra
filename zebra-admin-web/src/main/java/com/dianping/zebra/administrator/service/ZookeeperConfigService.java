package com.dianping.zebra.administrator.service;

import com.dianping.zebra.administrator.dto.zookeeperConfig.ZookeeperConfigDto;
import com.dianping.zebra.administrator.entity.ZookeeperConfigEntity;

import java.util.List;

/**
 * Created by taochen on 2018/11/8.
 */
public interface ZookeeperConfigService {

    List<ZookeeperConfigEntity> findZKConfig();

    List<String> getZKName();

    String getZKHostByName(String name);

    void addZKConfig(ZookeeperConfigDto zkConfigDto);

    void updateConfig(ZookeeperConfigEntity zkEntity);

    void deleteZKConfig(Integer id);
}
