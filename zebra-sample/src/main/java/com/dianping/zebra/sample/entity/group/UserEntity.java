package com.dianping.zebra.sample.entity.group;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @author tong.xin on 2018/10/29.
 */
public class UserEntity {
	protected int id;

	protected String name;

	protected String mis;

	protected String email;

	protected Timestamp updateTime;

	protected Date createTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMis() {
		return mis;
	}

	public void setMis(String mis) {
		this.mis = mis;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
