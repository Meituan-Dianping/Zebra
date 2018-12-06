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
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * TODO Comment of BlobParamContext
 *
 * @author Leo Liang
 */
public class BlobParamContext extends ParamContext {

	private static final long serialVersionUID = 4357790552909332479L;

	/**
	 * @param index
	 * @param values
	 */
	public BlobParamContext(int index, Object[] values) {
		super(index, values);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.zebra.jdbc.param.ParamContext#setParam(java.sql. PreparedStatement)
	 */
	@Override
	public void setParam(PreparedStatement stmt) throws SQLException {
		if (values.length == 1 && values[0] instanceof Blob) {
			stmt.setBlob(index, (Blob) values[0]);
		} else if (values.length == 1 && values[0] instanceof InputStream) {
			stmt.setBlob(index, (InputStream) values[0]);
		} else if (values.length == 2) {
			stmt.setBlob(index, (InputStream) values[0], (Integer) values[1]);
		}
	}
}
