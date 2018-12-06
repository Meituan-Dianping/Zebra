package com.dianping.zebra.administrator.dto.jdbcref;

import com.dianping.zebra.administrator.entity.SingleConfigEntity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by taochen on 2018/11/10.
 */
public class GroupConfigDto {

    private List<SingleConfigEntity> singleConfigs = new LinkedList<>();

    public void addSingleConfig(SingleConfigEntity config) {
        singleConfigs.add(config);
    }

    public void addAllSingleConfig(List<SingleConfigEntity> configs) {
        singleConfigs.addAll(configs);
    }

    public List<SingleConfigEntity> getSingleConfigs() {
        return singleConfigs;
    }
}
