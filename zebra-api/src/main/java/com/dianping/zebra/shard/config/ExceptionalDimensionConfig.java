/**
 * Project: com.dianping.zebra.zebra-client-0.1.0
 * 
 * File Created at 2011-6-14
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
package com.dianping.zebra.shard.config;

import java.io.Serializable;

public class ExceptionalDimensionConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5187189052650156734L;

	private String condition;
	
	private String db;
	
	private String table;

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}
	
}
