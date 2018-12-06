package com.dianping.zebra.administrator.service;

import com.dianping.zebra.administrator.dto.jdbcref.DBConfigInfoDto;
import com.dianping.zebra.administrator.dto.jdbcref.JdbcrefConfigDto;
import com.dianping.zebra.administrator.entity.DsConfigEntity;

import java.util.List;

/**
 * @author canhuang
 * @date 2018/3/25
 */
public interface JdbcrefService {

    int selectCount(int env);

    List<String> selectAllByEnv(int env);

    DBConfigInfoDto getGroupConfig(String jdbcref, String env);

    DsConfigEntity getDsConfig(String dsName, String env);

    void removeJdbcref(String jdbcref, String env);

    void saveJdbcrefConfig(JdbcrefConfigDto jdbcrefConfigDto) throws Exception;

    void dsOffLine(String dsName, String env);

    void dsOnLine(String dsName, String env);
}
