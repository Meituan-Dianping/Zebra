package com.dianping.zebra.administrator.service.impl;

import com.dianping.zebra.administrator.mapper.ZookeeperConfigMapper;
import com.dianping.zebra.administrator.dto.zookeeperConfig.ZookeeperConfigDto;
import com.dianping.zebra.administrator.entity.ZookeeperConfigEntity;
import com.dianping.zebra.administrator.service.ZookeeperConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by taochen on 2018/11/7.
 */
@Service
public class ZookeeperConfigServiceImpl implements ZookeeperConfigService {

    @Autowired
    private ZookeeperConfigMapper zookeeperConfigDao;

    @Override
    public List<ZookeeperConfigEntity> findZKConfig() {
        List<ZookeeperConfigEntity> zkconfigList = zookeeperConfigDao.findZKConfig();
        return zkconfigList;
    }

    @Override
    public List<String> getZKName() {
        List<String> zkNametList = zookeeperConfigDao.getZKName();
        return zkNametList;
    }

    @Override
    public String getZKHostByName(String name) {

        return zookeeperConfigDao.getHostByName(name);
    }

    @Override
    public void addZKConfig(ZookeeperConfigDto zkConfigDto) {
        ZookeeperConfigEntity zkConfigEntity = new ZookeeperConfigEntity();
        zkConfigEntity.setName(zkConfigDto.getName());
        zkConfigEntity.setHost(zkConfigDto.getHost());
        zkConfigEntity.setDescription(zkConfigDto.getDescription());
        zookeeperConfigDao.addZKConfig(zkConfigEntity);
    }

    @Override
    public void updateConfig(ZookeeperConfigEntity zkEntity) {

    }

    @Override
    public void deleteZKConfig(Integer id) {
        zookeeperConfigDao.deleteZKConfig(id);
    }
}
