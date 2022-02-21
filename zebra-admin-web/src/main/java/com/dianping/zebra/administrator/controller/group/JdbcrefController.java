package com.dianping.zebra.administrator.controller.group;

import com.dianping.zebra.administrator.zookeeper.ZookeeperService;
import com.dianping.zebra.administrator.controller.AbstractController;
import com.dianping.zebra.administrator.mapper.JdbcrefMapper;
import com.dianping.zebra.administrator.dto.ResultDto;
import com.dianping.zebra.administrator.dto.jdbcref.*;
import com.dianping.zebra.administrator.entity.DsConfigEntity;
import com.dianping.zebra.administrator.entity.GroupConfigEntity;
import com.dianping.zebra.administrator.entity.JdbcrefEntity;
import com.dianping.zebra.administrator.entity.SingleConfigEntity;
import com.dianping.zebra.administrator.exception.ZebraException;
import com.dianping.zebra.administrator.service.JdbcrefService;

import com.dianping.zebra.administrator.service.ZookeeperConfigService;
import com.dianping.zebra.administrator.util.JaxbUtils;
import com.dianping.zebra.administrator.util.JdbcUrlUtils;
import com.dianping.zebra.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.dianping.zebra.administrator.constant.SystemConsts.DS_CONFIG_PATTERN;
import static com.dianping.zebra.administrator.constant.SystemConsts.GROUP_CONFIG_NAME_PATTERN;

/**
 * @author Created by tong.xin on 18/3/7.
 */

@RestController
@RequestMapping(value = "/i/jdbcref")
public class JdbcrefController extends AbstractController {
	@Autowired
	private JdbcrefMapper jdbcrefDao;

	@Autowired
	private JdbcrefService jdbcrefService;

	@Autowired
	private ZookeeperConfigService zkConfigService;

	@RequestMapping(value = "/findJbdcrefByEnv", method = RequestMethod.GET)
	@ResponseBody
	public JdbcrefOverviewDto findJbdcrefByEnv(@RequestParam String env, @RequestParam int page, @RequestParam int size,
	      String jdbcrefFilter) {
		JdbcrefOverviewDto result = new JdbcrefOverviewDto();
		jdbcrefFilter = StringUtils.isNotBlank(jdbcrefFilter) ? jdbcrefFilter.trim().toLowerCase() : null;

		int begin = page <= 0 ? size : (page - 1) * size;
		int end = begin + size;
		List<JdbcrefEntity> entities = jdbcrefDao.findByEnv(env);

		if (entities != null && !entities.isEmpty()) {
			List<JdbcrefDto> dtos = new LinkedList<>();

			for (JdbcrefEntity entity : entities) {
				if (filterDtos(entity.getJdbcref(), jdbcrefFilter)) {
					JdbcrefDto dto = new JdbcrefDto();

					dto.setJdbcref(entity.getJdbcref());
					dto.setEnv(entity.getEnv());
					DBConfigInfoDto dbConfig = jdbcrefService.getGroupConfig(entity.getJdbcref(), env);
					dto.setGroupConfig(dbConfig);
					dto.setOwner(entity.getOwner());
					dto.setUpdateTime(entity.getUpdateTime());
					dtos.add(dto);
				}
			}

			result.setTotal(dtos.size());

			for (int i = begin; i < end && i < dtos.size(); ++i) {
				result.addJdbcrefDto(dtos.get(i));
			}
		} else {
			result.setTotal(0);
		}

		return result;
	}

	private boolean filterDtos(String jdbcref, String jdbcrefFilter) {
		if (jdbcrefFilter != null && StringUtils.isNotBlank(jdbcref)) {
			return jdbcref.contains(jdbcrefFilter);
		}

		return true;
	}

	@RequestMapping(value = "/removeJdbcRef", method = RequestMethod.GET)
	@ResponseBody
	public ResultDto<String> removeJdbcRef(@RequestParam String jdbcref, @RequestParam String env) {
		ResultDto<String> resultDto = new ResultDto<>();
		try {
			jdbcrefService.removeJdbcref(jdbcref, env);
			resultDto.setStatus("success");
			resultDto.setMessage(jdbcref + "删除成功！");
		} catch (Exception e) {
			resultDto.setStatus("fail");
			resultDto.setMessage(jdbcref + "删除失败！");
		}
		return resultDto;
	}

	@RequestMapping(value = "/editJdbcrefConfg", method = RequestMethod.POST)
	@ResponseBody
	public ResultDto<String> editJdbcrefConfg(JdbcrefConfigDto jdbcrefConfigDto) {
		ResultDto<String> resultDto = new ResultDto<>();
		return resultDto;
	}

	@RequestMapping(value = "/findJdbcrefDetil", method = RequestMethod.GET)
	@ResponseBody
	public List<JdbcrefDetilDto> findJdbcrefDetil(@RequestParam String jdbcref, @RequestParam String env)
			throws ZebraException {
		List<JdbcrefDetilDto> result = new LinkedList<>();
		String groupkey = String.format(GROUP_CONFIG_NAME_PATTERN, jdbcref);
		String host = zkConfigService.getZKHostByName(env);
		byte[] groupValue = ZookeeperService.getConfig(host, groupkey);
        GroupConfigEntity groupConfig = JaxbUtils.jaxbReadXml(GroupConfigEntity.class, groupValue);
        if (groupConfig != null) {
            for (SingleConfigEntity singleConfig : groupConfig.getSingleConfigs()) {
                String dsName = singleConfig.getName();
                String dskey = String.format(DS_CONFIG_PATTERN, dsName);
                byte[] dsValue = ZookeeperService.getConfig(host, dskey);
                DsConfigEntity dsConfig = JaxbUtils.jaxbReadXml(DsConfigEntity.class, dsValue);
                JdbcrefDetilDto jdbcrefDetilDto = new JdbcrefDetilDto();
                JdbcUrlUtils.JdbcUrlAnalysisResult analysis = JdbcUrlUtils.analysis(dsConfig.getUrl());
                jdbcrefDetilDto.setIp(analysis.getAddress());
                jdbcrefDetilDto.setPort(analysis.getPort());
                jdbcrefDetilDto.setJdbcUrl(dsConfig.getUrl());
                jdbcrefDetilDto.setUsername(dsConfig.getUsername());
                jdbcrefDetilDto.setPassword(dsConfig.getPassword());
                jdbcrefDetilDto.setProperties(dsConfig.getProperties());
                jdbcrefDetilDto.setActive(dsConfig.isActive());
                jdbcrefDetilDto.setDsName(singleConfig.getName());
                if (singleConfig.getReadWeight() != 0) {
                    jdbcrefDetilDto.setWeight(singleConfig.getReadWeight());
                } else jdbcrefDetilDto.setWeight(0);
                result.add(jdbcrefDetilDto);
            }
        }
		return result;
	}

	@RequestMapping(value = "/saveJdbcrefConfig", method = RequestMethod.POST)
	@ResponseBody
	public ResultDto<String> saveJdbcrefConfig(@RequestBody JdbcrefConfigDto jdbcrefConfigDto) {
		ResultDto<String> resultDto = new ResultDto<>();
		try {
			jdbcrefService.saveJdbcrefConfig(jdbcrefConfigDto);
			resultDto.setStatus("success");
			resultDto.setMessage(jdbcrefConfigDto.getJdbcref() + "注册成功！");
		} catch (Exception e) {
			resultDto.setStatus("fail");
			resultDto.setMessage(jdbcrefConfigDto.getJdbcref() + "注册失败！");
		}
        return resultDto;
    }

    @RequestMapping(value = "/dsOffLine", method = RequestMethod.GET)
    @ResponseBody
    public void dsOffLine(@RequestParam String dsName, @RequestParam String env) {
        try {
            jdbcrefService.dsOffLine(dsName, env);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/dsOnLine", method = RequestMethod.GET)
    @ResponseBody
    public void dsOnLine(@RequestParam String dsName, @RequestParam String env) {
        try {
            jdbcrefService.dsOnLine(dsName, env);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
