package com.dianping.zebra.sample.entity.shard;

import java.sql.Timestamp;

/**
 * @author tong.xin on 2018/11/8.
 */
public class FeedEntity {
	private int id;

	private String uid;

	private Timestamp updateTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
}
