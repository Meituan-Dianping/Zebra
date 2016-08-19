/**
 * Project: ${zebra-client.aid}
 *
 * File Created at 2011-6-7 $Id$
 *
 * Copyright 2010 dianping.com. All rights reserved.
 *
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.zebra.shard.router;

import java.util.List;

import com.dianping.zebra.shard.exception.ShardRouterException;
import com.dianping.zebra.shard.router.rule.RouterRule;
import com.dianping.zebra.shard.exception.ShardParseException;

/**
 * Sharding Router
 *
 * @author hao.zhu
 */
public interface ShardRouter {

	RouterResult router(String sql, List<Object> params) throws ShardRouterException, ShardParseException;

	boolean validate(String sql) throws ShardParseException, ShardRouterException;

	RouterRule getRouterRule();
}
