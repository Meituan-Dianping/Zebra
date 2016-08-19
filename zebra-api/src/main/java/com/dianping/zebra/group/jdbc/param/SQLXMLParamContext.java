/**
 * Project: zebra-client
 *
 * File Created at 2011-6-19
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
package com.dianping.zebra.group.jdbc.param;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLXML;

/**
 * TODO Comment of SQLXMLParamContext
 *
 * @author Leo Liang
 */
public class SQLXMLParamContext extends ParamContext {

	private static final long serialVersionUID = -3225485567882941489L;

	/**
	 * @param index
	 * @param values
	 */
	public SQLXMLParamContext(int index, Object[] values) {
		super(index, values);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.zebra.jdbc.param.ParamContext#setParam(java.sql. PreparedStatement)
	 */
	@Override
	public void setParam(PreparedStatement stmt) throws SQLException {
		stmt.setSQLXML(index, (SQLXML) values[0]);
	}

}
