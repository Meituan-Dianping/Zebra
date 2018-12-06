package com.dianping.zebra.administrator.dto.shard;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Created by tong.xin on 18/3/7.
 */
public class ShardOverviewDto {
    List<ShardDto> shardList = new LinkedList<>();;

    private int total;

    public List<ShardDto> getShardList() {
        return shardList;
    }

    public void addShardDto(ShardDto shardDto) {
        this.shardList.add(shardDto);
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
