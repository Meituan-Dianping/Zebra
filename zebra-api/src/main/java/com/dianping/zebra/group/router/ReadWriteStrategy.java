/**
 * Project: zebra-client
 *
 * File Created at Mar 10, 2014
 *
 */
package com.dianping.zebra.group.router;

import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;

/**
 * 
 * @author hao.zhu
 *
 */
public interface ReadWriteStrategy {
	boolean shouldReadFromMaster();

	void setGroupDataSourceConfig(GroupDataSourceConfig config);
}
