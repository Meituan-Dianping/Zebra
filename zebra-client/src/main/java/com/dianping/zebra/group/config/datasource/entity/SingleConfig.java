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
package com.dianping.zebra.group.config.datasource.entity;

import com.dianping.zebra.exception.ZebraException;
import com.dianping.zebra.util.StringUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "singleConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class SingleConfig {
	private String name;

	private int writeWeight = -1;

	private int readWeight = -1;

	public SingleConfig() {

	}

	public SingleConfig(String name, int writeWeight, int readWeight) {
		this.name = name;
		this.writeWeight = writeWeight;
		this.readWeight = readWeight;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getWriteWeight() {
		return writeWeight;
	}

	public void setWriteWeight(int writeWeight) {
		this.writeWeight = writeWeight;
	}

	public int getReadWeight() {
		return readWeight;
	}

	public void setReadWeight(int readWeight) {
		this.readWeight = readWeight;
	}

	public void checkConfig() {
		if(StringUtils.isBlank(this.name)) {
			throw new ZebraException("incomplete singleDataSourceConfig name:" + this.name);
		}
	}
}
