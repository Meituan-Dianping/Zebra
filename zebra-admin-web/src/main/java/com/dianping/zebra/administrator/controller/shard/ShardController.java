package com.dianping.zebra.administrator.controller.shard;

import com.dianping.zebra.administrator.constant.SystemConsts;
import com.dianping.zebra.administrator.zookeeper.ZookeeperService;
import com.dianping.zebra.administrator.controller.AbstractController;
import com.dianping.zebra.administrator.mapper.ShardMapper;
import com.dianping.zebra.administrator.dto.ResultDto;
import com.dianping.zebra.administrator.dto.shard.ShardConfigDto;
import com.dianping.zebra.administrator.dto.shard.ShardDto;
import com.dianping.zebra.administrator.dto.shard.ShardOverviewDto;
import com.dianping.zebra.administrator.dto.shard.TableShardConfigDto;
import com.dianping.zebra.administrator.entity.ShardEntity;
import com.dianping.zebra.administrator.service.ShardService;
import com.dianping.zebra.administrator.service.ZookeeperConfigService;
import com.dianping.zebra.administrator.util.JaxbUtils;
import com.dianping.zebra.shard.config.RouterRuleConfig;
import com.dianping.zebra.shard.config.TableShardRuleConfig;
import com.dianping.zebra.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Created by tong.xin on 18/3/7.
 */

@RestController
@RequestMapping(value = "/i/shard")
public class ShardController extends AbstractController {
	@Autowired
	private ShardMapper shardDao;

	@Autowired
	private ShardService shardService;

	@Autowired
    private ZookeeperConfigService zkconfigService;

	@RequestMapping(value = "/findRuleNameByEnv", method = RequestMethod.GET)
	public ShardOverviewDto findRuleNameByEnv(@RequestParam String env, @RequestParam int page,
	      @RequestParam int size, String shardFilter) {
		ShardOverviewDto result = new ShardOverviewDto();
		shardFilter = StringUtils.isNotBlank(shardFilter) ? shardFilter.trim().toLowerCase() : null;

		int begin = page <= 0 ? size : (page - 1) * size;
		int end = begin + size;
		List<ShardEntity> entities = shardDao.findByEnv(env);

		if (entities != null && !entities.isEmpty()) {
			List<ShardDto> dtos = new LinkedList<>();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			for (ShardEntity entity : entities) {
				if (filterDtos(entity.getRuleName(), shardFilter)) {
					ShardDto dto = new ShardDto();

					dto.setRuleName(entity.getRuleName());
					dto.setZKName(entity.getEnv());
					dto.setOwner(entity.getOwner());
					dto.setDesc(entity.getDescription());
					dto.setUpdateTime(df.format(entity.getUpdateTime()));

					dtos.add(dto);
				}
			}

			result.setTotal(dtos.size());

			for (int i = begin; i < end && i < dtos.size(); ++i) {
				result.addShardDto(dtos.get(i));
			}
		} else {
			result.setTotal(0);
		}

		return result;
	}

	private boolean filterDtos(String ruleName, String shardFilter) {
		if (shardFilter != null && StringUtils.isNotBlank(ruleName)) {
			return ruleName.contains(shardFilter);
		}

		return true;
	}

	@RequestMapping(value = "/findRuleName", method = RequestMethod.GET)
	@ResponseBody
	public ShardConfigDto findRuleName(@RequestParam String env, @RequestParam String ruleName) throws Exception {
		ShardConfigDto result = new ShardConfigDto();
		RouterRuleConfig shardConfig = null;
		String key = String.format(SystemConsts.SHARD_CONFIG_NAME_PATTERN, ruleName);
		String host = zkconfigService.getZKHostByName(env);
		byte[] data = ZookeeperService.getConfig(host, key);
		if (data != null) {
			shardConfig = JaxbUtils.jaxbReadXml(RouterRuleConfig.class, data);

			List<TableShardConfigDto> tsConfigDtoList = new ArrayList<>();
			for (TableShardRuleConfig tsConfig : shardConfig.getTableShardConfigs()) {
				TableShardConfigDto tsConfigDto = new TableShardConfigDto();
				tsConfigDto.setTableName(tsConfig.getTableName());
				tsConfigDto.setDimensionConfigs(tsConfig.getDimensionConfigs());
				tsConfigDtoList.add(tsConfigDto);
			}
			result.setTableShardConfigs(tsConfigDtoList);
		}

		return result;
	}

	@RequestMapping(value = "/addRuleName", method = RequestMethod.GET)
	@ResponseBody
	public ResultDto<String> addRuleName(@RequestParam String ruleName, @RequestParam String env,
	      @RequestParam String owner, @RequestParam String desc) {
		ResultDto<String> resultDto = new ResultDto<>();
		try {
			shardService.addRuleName(ruleName, env, owner, desc);
			resultDto.setStatus("success");
			resultDto.setMessage(ruleName + "注册成功！");
		} catch (Exception e) {
			resultDto.setStatus("fail");
			resultDto.setMessage(ruleName + "注册失败！");
		}
		return resultDto;
	}

	@RequestMapping(value = "/saveRuleNameConfig", method = RequestMethod.POST)
	@ResponseBody
	public ResultDto<String> saveRuleNameConfig(@RequestBody ShardConfigDto shardConfigDto) {
		ResultDto<String> resultDto = new ResultDto<>();
		try {
		    shardService.saveRuleNameConfig(shardConfigDto);
		    resultDto.setStatus("success");
        } catch (Exception e) {
		    resultDto.setStatus("fail");
        }
        return resultDto;
	}

	@RequestMapping(value = "/deleteRuleNameConfig", method = RequestMethod.GET)
	@ResponseBody
	public ResultDto<String> deleteRuleNameConfig(@RequestParam String ruleName, @RequestParam String env) {
		ResultDto<String> resultDto = new ResultDto<>();
		try {
			shardService.deleteRuleNameConfig(ruleName, env);
            resultDto.setStatus("success");
            resultDto.setMessage(ruleName + "删除成功！");
		} catch (Exception e) {
		    resultDto.setStatus("fail");
		    resultDto.setMessage(ruleName + "删除失败！");
        }
        return resultDto;
	}

}
