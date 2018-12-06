package com.dianping.zebra.administrator.dto.jdbcref;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Created by tong.xin on 18/3/7.
 */
public class JdbcrefOverviewDto {
    List<JdbcrefDto> jdbcrefList = new LinkedList<>();;

    private int total;

    public List<JdbcrefDto> getJdbcrefList() {
        return jdbcrefList;
    }

    public void addJdbcrefDto(JdbcrefDto jdbcrefDto) {
        this.jdbcrefList.add(jdbcrefDto);
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
