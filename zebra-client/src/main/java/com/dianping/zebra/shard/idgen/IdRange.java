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
package com.dianping.zebra.shard.idgen;

import java.util.concurrent.atomic.AtomicLong;

public class IdRange {

	private final long minValue;

	private final long maxValue;

	private AtomicLong currentValue;

	private volatile boolean overflow = false;

	// [minValue, maxValue)
	public IdRange(long minValue, long maxValue) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.currentValue = new AtomicLong(minValue);
	}

	public long next() {
		if (this.overflow) {
			return -1;
		}
		long id = this.currentValue.getAndIncrement();
		if (id >= this.maxValue) {
			this.overflow = true;
			return -1;
		}
		return id;
	}

	public int size() {
		return (int) (this.maxValue - this.minValue);
	}

	public int remain() {
		int r = (int) (this.maxValue - this.currentValue.get());
		return r >= 0 ? r : 0;
	}

	public boolean hasNext() {
		return ((this.maxValue - this.currentValue.get()) > 0);
	}

	public boolean isOverflow() {
		return overflow;
	}

	public long getMinValue() {
		return this.minValue;
	}

	public long getMaxValue() {
		return this.maxValue;
	}
}
