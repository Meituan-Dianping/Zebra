/**
 * Project: ${zebra-client.aid}
 * 
 * File Created at 2011-7-5
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.zebra.shard.jdbc;

import com.dianping.zebra.shard.jdbc.base.BaseTestCase;

/**
 * TODO Comment of ResultSetTest
 * 
 * @author Leo Liang
 * 
 */
public class ResultSetTest extends BaseTestCase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.zebra.jdbc.ZebraBaseTestCase#getSupportedOps()
	 */
	@Override
	protected String[] getSupportedOps() {
		return new String[] { "setRouterTarget", "addResultSet", "getStatement","getShardStatement",
				"setStatement", "close", "findColumn", "getArray", "getAsciiStream", "getBigDecimal",
				"getBinaryStream", "getBlob", "getBoolean", "getByte", "getBytes", "getCharacterStream", "getClob",
				"getConcurrency", "getCursorName", "getDate", "getDouble", "getFetchDirection", "getFetchSize",
				"getFloat", "getHoldability", "getInt", "getLong", "getLong", "getMetaData", "getNCharacterStream",
				"getNClob", "getNString", "getObject", "getRef", "getRow", "getRowId", "getSQLXML", "getShort",
				"getStatement", "getString", "getTime", "getTimestamp", "getType", "getURL", "getUnicodeStream",
				"isClosed", "next", "setFetchDirection", "setFetchSize", "wasNull", "init", "setDataPool" };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.zebra.jdbc.ZebraBaseTestCase#getTestObj()
	 */
	@Override
	protected Object getTestObj() {
		return new ShardResultSet();
	}

}
