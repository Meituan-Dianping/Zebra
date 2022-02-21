package com.dianping.zebra.administrator.mapper;

import com.dianping.zebra.administrator.entity.JdbcrefEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Created by tong.xin on 18/3/7.
 */
public interface JdbcrefMapper {

    void create(JdbcrefEntity entity);

    List<JdbcrefEntity> findByEnv(@Param("env") String enc);

    String findOwnByJdbcref(@Param("env") int env, @Param("jdbcref") String jdbcref);

    List<JdbcrefEntity> findByJdbcrefs(@Param("list") List<String> jdbcrefs, @Param("env") int env);

    int selectCountByEnv(@Param("env") int env);

    List<String> selectAllByEnv(@Param("env") int env);

    void removeJdbcref(@Param("jdbcref") String jdbcref, @Param("env") String env);

    void addJdbcref(JdbcrefEntity jdbcrefEntity);
}
