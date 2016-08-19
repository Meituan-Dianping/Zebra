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
