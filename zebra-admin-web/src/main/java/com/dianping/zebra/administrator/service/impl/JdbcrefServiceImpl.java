package com.dianping.zebra.administrator.service.impl;

import com.dianping.zebra.administrator.zookeeper.ZookeeperService;
import com.dianping.zebra.administrator.dao.JdbcrefMapper;
import com.dianping.zebra.administrator.dao.ZookeeperConfigMapper;
import com.dianping.zebra.administrator.dto.jdbcref.DBAddressDto;
import com.dianping.zebra.administrator.dto.jdbcref.DBConfigInfoDto;
import com.dianping.zebra.administrator.dto.jdbcref.JdbcrefConfigDto;
import com.dianping.zebra.administrator.entity.DsConfigEntity;
import com.dianping.zebra.administrator.entity.GroupConfigEntity;
import com.dianping.zebra.administrator.entity.JdbcrefEntity;
import com.dianping.zebra.administrator.entity.SingleConfigEntity;
import com.dianping.zebra.administrator.service.JdbcrefService;
import com.dianping.zebra.administrator.util.JaxbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.dianping.zebra.administrator.GlobalConstants.*;

/**
 * @author canhuang
 * @date 2018/3/25
 */
@Service
public class JdbcrefServiceImpl extends BaseServiceImpl implements JdbcrefService {

    @Autowired
    private JdbcrefMapper jdbcrefDao;

    @Autowired
    private ZookeeperConfigMapper zKConfigDao;

    @Override
    public int selectCount(int env) {
        return jdbcrefDao.selectCountByEnv(env);
    }

    @Override
    public List<String> selectAllByEnv(int env) { return jdbcrefDao.selectAllByEnv(env); }

    @Override
    public DBConfigInfoDto getGroupConfig(String jdbcref, String env) {
        String groupConfigKey = String.format(GROUP_CONFIG_NAME_PATTERN, jdbcref);
        String host = zKConfigDao.getHostByName(env);
        byte[] data = ZookeeperService.getConfig(host, groupConfigKey);
        GroupConfigEntity groupConfig = JaxbUtils.jaxbReadXml(GroupConfigEntity.class, data);
        DBConfigInfoDto dbConfigInfoDto = new DBConfigInfoDto();
        if (groupConfig != null) {
            int writeNum = 0;
            int readNum = 0;
            for (SingleConfigEntity singleConfig : groupConfig.getSingleConfigs()) {
                writeNum += singleConfig.getWriteWeight();
                readNum += singleConfig.getReadWeight();
            }
            dbConfigInfoDto.setWriteDBNum(writeNum);
            dbConfigInfoDto.setReadDBNum(readNum);
        }
        return dbConfigInfoDto;
    }

    @Override
    public DsConfigEntity getDsConfig(String dsName, String env) {
        String host = zKConfigDao.getHostByName(env);
        String dsKey = String.format(DS_CONFIG_PATTERN, dsName);
        //String xmlData = ZookeeperService.getConfig(host, dsKey);
        return null;
    }

    @Override
    public void removeJdbcref(String jdbcref, String env) {
        //删除数据库记录
        jdbcrefDao.removeJdbcref(jdbcref, env);

        //删除zk记录
        String groupKey = String.format(GROUP_CONFIG_NAME_PATTERN, jdbcref);
        String host = zKConfigDao.getHostByName(env);
        ZookeeperService.deleteConfig(host, groupKey);
    }

    @Override
    public void saveJdbcrefConfig(JdbcrefConfigDto jdbcrefConfigDto) throws Exception {
        //写入groupConfig到ZK中
        int dsNameIndex = 1;
        String groupConfigKey = String.format(GROUP_CONFIG_NAME_PATTERN, jdbcrefConfigDto.getJdbcref());
        List<String> dsKeyList = new ArrayList<>();
        String host = zKConfigDao.getHostByName(jdbcrefConfigDto.getEnv());
        GroupConfigEntity groupConfig = new GroupConfigEntity();
        List<SingleConfigEntity> singleConfigs = new ArrayList<>();
        for (DBAddressDto dbAddressDto: jdbcrefConfigDto.getDbAddresses()) {
            SingleConfigEntity singleConfig = new SingleConfigEntity();
            singleConfig.setActive(1);
            singleConfig.setReadWeight(dbAddressDto.getReadWeight());
            singleConfig.setWriteWeight(dbAddressDto.getWriteWeight());
            String dsName = String.format(DS_NAME, jdbcrefConfigDto.getJdbcref(), dsNameIndex);
            dsKeyList.add(String.format(DS_CONFIG_PATTERN, dsName));
            singleConfig.setName(dsName);
            singleConfigs.add(singleConfig);
            dsNameIndex++;
        }
        groupConfig.setSingleConfigs(singleConfigs);
        byte[] GroupData = JaxbUtils.jaxbWriteXml(GroupConfigEntity.class, groupConfig);

        ZookeeperService.createKey(host, groupConfigKey);
        ZookeeperService.setConfig(host, groupConfigKey, GroupData);

        //写入dsConfig到ZK中
        int index = 0;
        for (DBAddressDto dbAddressDto: jdbcrefConfigDto.getDbAddresses()) {
            DsConfigEntity dsConfig = new DsConfigEntity();
            String url = String.format(JDBC_URL, dbAddressDto.getAddress(), jdbcrefConfigDto.getDbName());
            dsConfig.setUrl(url);
            dsConfig.setActive(true);
            dsConfig.setUsername(dbAddressDto.getUserName());
            dsConfig.setPassword(dbAddressDto.getPassword());
            dsConfig.setProperties(PROPERTIES);
            dsConfig.setDriverClass(DRIVER_CLASS);
            byte[] dsData = JaxbUtils.jaxbWriteXml(DsConfigEntity.class, dsConfig);

            ZookeeperService.createKey(host, dsKeyList.get(index));
            ZookeeperService.setConfig(host, dsKeyList.get(index), dsData);
            ++index;
        }

        //写入数据库
        JdbcrefEntity jdbcrefEntity = new JdbcrefEntity();
        jdbcrefEntity.setJdbcref(jdbcrefConfigDto.getJdbcref());
        jdbcrefEntity.setEnv(jdbcrefConfigDto.getEnv());
        jdbcrefEntity.setStatus(0);
        jdbcrefEntity.setOwner(jdbcrefConfigDto.getOwner());
        jdbcrefEntity.setDescription(jdbcrefConfigDto.getDescription());
        jdbcrefDao.addJdbcref(jdbcrefEntity);
    }

    @Override
    public void dsOffLine(String dsName, String env) {
        String host = zKConfigDao.getHostByName(env);
        String dsKey = String.format(DS_CONFIG_PATTERN, dsName);

        byte[] oldDsConfigXml = ZookeeperService.getConfig(host, dsKey);
        DsConfigEntity oldDsConfig = JaxbUtils.jaxbReadXml(DsConfigEntity.class, oldDsConfigXml);
        oldDsConfig.setActive(false);

        byte[] newDsConfigXml = JaxbUtils.jaxbWriteXml(DsConfigEntity.class, oldDsConfig);
        ZookeeperService.setConfig(host, dsKey, newDsConfigXml);

    }

    @Override
    public void dsOnLine(String dsName, String env) {
        String host = zKConfigDao.getHostByName(env);
        String dsKey = String.format(DS_CONFIG_PATTERN, dsName);

        byte[] oldDsConfigXml = ZookeeperService.getConfig(host, dsKey);
        DsConfigEntity oldDsConfig = JaxbUtils.jaxbReadXml(DsConfigEntity.class, oldDsConfigXml);
        oldDsConfig.setActive(true);

        byte[] newDsConfigXml = JaxbUtils.jaxbWriteXml(DsConfigEntity.class, oldDsConfig);
        ZookeeperService.setConfig(host, dsKey, newDsConfigXml);
    }
}
