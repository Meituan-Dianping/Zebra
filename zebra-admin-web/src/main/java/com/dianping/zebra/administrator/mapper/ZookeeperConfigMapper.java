package com.dianping.zebra.administrator.mapper;

import com.dianping.zebra.administrator.entity.ZookeeperConfigEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by taochen on 2018/11/8.
 */
public interface ZookeeperConfigMapper {

    List<ZookeeperConfigEntity> findZKConfig();

    List<String> getZKName();

    void addZKConfig(ZookeeperConfigEntity zkEntity);

    void deleteZKConfig(@Param("id") Integer id);

    void updateZKConfig(ZookeeperConfigEntity zkEntity);

    String getHostByName(@Param("ZKName") String ZKName);

}
