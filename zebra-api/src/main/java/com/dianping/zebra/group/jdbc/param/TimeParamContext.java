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
import java.sql.Time;
import java.util.Calendar;

/**
 * TODO Comment of TimeParamContext
 *
 * @author Leo Liang
 */
public class TimeParamContext extends ParamContext {

	private static final long serialVersionUID = -8378628871695126103L;

	/**
	 * @param index
	 * @param values
	 */
	public TimeParamContext(int index, Object[] values) {
		super(index, values);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.zebra.jdbc.param.ParamContext#setParam(java.sql. PreparedStatement)
	 */
	@Override
	public void setParam(PreparedStatement stmt) throws SQLException {
		if (values.length == 1) {
			stmt.setTime(index, (Time) values[0]);
		} else if (values.length == 2) {
			stmt.setTime(index, (Time) values[0], (Calendar) values[1]);
		}
	}

}
