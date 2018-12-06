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

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * TODO Comment of UnicodeStreamParamContext
 *
 * @author Leo Liang
 */
public class UnicodeStreamParamContext extends ParamContext {

	private static final long serialVersionUID = 6354070943960944242L;

	/**
	 * @param index
	 * @param values
	 */
	public UnicodeStreamParamContext(int index, Object[] values) {
		super(index, values);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.zebra.jdbc.param.ParamContext#setParam(java.sql. PreparedStatement)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void setParam(PreparedStatement stmt) throws SQLException {
		stmt.setUnicodeStream(index, (InputStream) values[0], (Integer) values[1]);
	}

}
