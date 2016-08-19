package com.dianping.zebra.group.router;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.system.entity.DataCenter;
import com.dianping.zebra.util.NetworkUtils;

public class DataCenterAwareRouter implements DataSourceRouter {

	private WeightDataSourceRouter sameDataCenterWeightedRouter;

	private WeightDataSourceRouter otherDataCenterWeightedRouter;

	public DataCenterAwareRouter(Map<String, DataSourceConfig> dataSourceConfigs, Map<String, DataCenter> dataCenters) {
		this(dataSourceConfigs, dataCenters, NetworkUtils.IpHelper.INSTANCE.getLocalHostAddress());
	}

	// for test purpose
	protected DataCenterAwareRouter(Map<String, DataSourceConfig> dataSourceConfigs,
			Map<String, DataCenter> dataCenters, String localIpAddress) {
		String[] dataCenterIps = null;
		for (Entry<String, DataCenter> entry : dataCenters.entrySet()) {
			DataCenter dc = entry.getValue();
			String[] ipPrefixs = dc.getIpPrefix().split(",");

			if (ipPrefixs != null) {
				for (String ipPrefix : ipPrefixs) {
					if (localIpAddress.startsWith(ipPrefix)) {
						dataCenterIps = ipPrefixs;
						break;
					}
				}
			}
		}

		Map<String, DataSourceConfig> sameDataCenterDataSourceConfigs = new HashMap<String, DataSourceConfig>();
		Map<String, DataSourceConfig> otherDataCenterDataSourceConfigs = new HashMap<String, DataSourceConfig>();

		if (dataCenterIps != null) {
			for (Entry<String, DataSourceConfig> entry : dataSourceConfigs.entrySet()) {
				if (entry.getValue().getWeight() > 0) {

					String jdbcUrl = entry.getValue().getJdbcUrl();

					boolean inSameDataCenter = false;
					for (String ipPrefix : dataCenterIps) {
						if (jdbcUrl.contains(ipPrefix)) {
							inSameDataCenter = true;
							break;
						}
					}

					if (inSameDataCenter) {
						sameDataCenterDataSourceConfigs.put(entry.getKey(), entry.getValue());
					} else {
						otherDataCenterDataSourceConfigs.put(entry.getKey(), entry.getValue());
					}
				}
			}
		} else {
			sameDataCenterDataSourceConfigs.putAll(dataSourceConfigs);
		}

		this.sameDataCenterWeightedRouter = new WeightDataSourceRouter(sameDataCenterDataSourceConfigs);
		this.otherDataCenterWeightedRouter = new WeightDataSourceRouter(otherDataCenterDataSourceConfigs);
	}

	@Override
	public String getName() {
		return "data-center-aware";
	}

	@Override
	public RouterTarget select(RouterContext routerContext) {
		RouterTarget routerTarget = sameDataCenterWeightedRouter.select(routerContext);

		if (routerTarget == null) {
			routerTarget = otherDataCenterWeightedRouter.select(routerContext);
		}

		return routerTarget;
	}
}
