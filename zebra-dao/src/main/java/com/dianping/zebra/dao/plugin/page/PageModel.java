/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.zebra.dao.plugin.page;

import java.util.List;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * 
 * @author damonzhu
 *
 */
public class PageModel extends RowBounds implements ResultHandler {

	/**
	 * Total records size
	 */
	private int recordCount;

	/**
	 * The number of records of per page
	 */
	private int pageSize;

	/**
	 * Current page
	 */
	private int page = 1;

	/**
	 * Records
	 */
	private List<?> records;

	private String sortField;

	private boolean sortAsc = true;

	public PageModel(int page, int pageSize) {
		super(page > 0 ? (page - 1) * pageSize : 0, pageSize);
		this.page = page;
		this.pageSize = pageSize;
	}

	public int getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public List<?> getRecords() {
		return records;
	}

	public void setRecords(List<?> records) {
		this.records = records;
	}

	public String getSortField() {
		return sortField;
	}

	public void setSortField(String sortField) {
		this.sortField = sortField;
	}

	public boolean isSortAsc() {
		return sortAsc;
	}

	public void setSortAsc(boolean sortAsc) {
		this.sortAsc = sortAsc;
	}

	@Override
	public String toString() {
		return "Paginate [recordCount=" + recordCount + ", pageSize=" + pageSize + ", page=" + page + ", records="
				+ records + ", sortField=" + sortField + ", sortAsc=" + sortAsc + "]";
	}

	@Override
	public void handleResult(ResultContext context) {
		// do nothing
	}
}
