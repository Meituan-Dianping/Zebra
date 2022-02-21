package com.dianping.zebra.administrator.controller.zookeeperConfig;

import com.dianping.zebra.administrator.controller.AbstractController;
import com.dianping.zebra.administrator.dto.ResultDto;
import com.dianping.zebra.administrator.dto.zookeeperConfig.ZookeeperConfigDto;
import com.dianping.zebra.administrator.entity.ZookeeperConfigEntity;
import com.dianping.zebra.administrator.service.ZookeeperConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by taochen on 2018/11/7.
 */

@RestController
@RequestMapping(value = "/i/zkConfig")
public class ZookeeperConfigController extends AbstractController {

    @Autowired
    private ZookeeperConfigService zkConfigService;

    @RequestMapping(value = "/findZKConfig", method = RequestMethod.GET)
    @ResponseBody
    public List<ZookeeperConfigEntity> findZKConfig() {
        List<ZookeeperConfigEntity> zkConfigList = zkConfigService.findZKConfig();
        return zkConfigList;
    }

    @RequestMapping(value = "/getEnv", method = RequestMethod.GET)
    @ResponseBody
    public List<String> getEnv() {
        List<String> zkNameList = zkConfigService.getZKName();
        return zkNameList;
    }

    @RequestMapping(value = "/addZKConfig", method = RequestMethod.POST)
    @ResponseBody
    public ResultDto<String> addZKConfig(@RequestBody ZookeeperConfigDto zookeeperConfigDto) {
        ResultDto<String> resultDto = new ResultDto<>();
        try {
            zkConfigService.addZKConfig(zookeeperConfigDto);
            resultDto.setStatus("success");
        } catch (Exception e) {
            resultDto.setStatus("fail");
            resultDto.setMessage(e.getMessage());
        }
        return resultDto;
    }

    @RequestMapping(value = "/deleteZKConfig", method = RequestMethod.GET)
    @ResponseBody
    public ResultDto<String> deleteZKConfig(@RequestParam Integer id) {
        ResultDto<String> resultDto = new ResultDto<>();
        try {
            zkConfigService.deleteZKConfig(id);
            resultDto.setStatus("success");
        } catch (Exception e) {
            resultDto.setStatus("fail");
        }
        return resultDto;
    }

}
