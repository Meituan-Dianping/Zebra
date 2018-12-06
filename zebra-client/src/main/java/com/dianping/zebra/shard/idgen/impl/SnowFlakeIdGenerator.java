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
package com.dianping.zebra.shard.idgen.impl;

import com.dianping.zebra.shard.exception.ShardIdGenException;
import com.dianping.zebra.shard.idgen.IdGenerator;
import com.dianping.zebra.shard.idgen.IdRange;
import com.dianping.zebra.shard.idgen.IdTimeService;

public class SnowFlakeIdGenerator implements IdGenerator {

	private static final long EPOCH = 1514736000000L; // default start time 2018-01-01 00:00:00.000

	private static final long SEQUENCE_COUNT = 12L;

	private static final long MACHINE_ID_COUNT = 10L;

	private static final long SEQUENCE_MASK = (1L << SEQUENCE_COUNT) - 1;

	private static final long MACHINE_ID_SHIFT_COUNT = SEQUENCE_COUNT;

	private static final long MAX_MACHINE_ID = (1L << MACHINE_ID_COUNT) - 1;

	private static final long TIMESTAMP_SHIFT_COUNT = SEQUENCE_COUNT + MACHINE_ID_COUNT;

	private long machineId;

	private long lastTimestamp;

	private long currentSequence;

	private IdTimeService timeService;

	@Override
	public void init() {
		if (this.timeService == null) {
			this.timeService = new DefaultIdTimeService();
		}
	}

	@Override
	public synchronized long nextId() {
		long currentMillis = timeService.getCurrentTimeMillis();
		if (currentMillis == lastTimestamp) {
			currentSequence = (++currentSequence) & SEQUENCE_MASK;
			if (currentSequence == 0) {
				currentMillis = waitToNextMillis(currentMillis);
			}
		} else if (currentMillis > lastTimestamp) {
			currentSequence = 0;
		} else {
			throw new ShardIdGenException("Time move backwards, refuse to generate id for "
					+ (lastTimestamp - currentMillis) + " milliseconds!");
		}

		lastTimestamp = currentMillis;

		return ((currentMillis - EPOCH) << TIMESTAMP_SHIFT_COUNT) | (machineId << MACHINE_ID_SHIFT_COUNT)
				| currentSequence;
	}

	protected long waitToNextMillis(long lastTimestamp) {
		long timestamp = timeService.getCurrentTimeMillis();
		while (timestamp <= lastTimestamp) {
			timestamp = timeService.getCurrentTimeMillis();
		}
		return timestamp;
	}

	@Override
	public synchronized long nextId(String nameColumn) {
		throw new ShardIdGenException("SnowFlakeIdGenerator not support nextId with specified column name!");
	}

	@Override
	public IdRange nextBatch(String nameColumn) {
		throw new ShardIdGenException("SnowFlakeIdGenerator not support nextBatch!");
	}

	@Override
	public IdRange nextBatch(String nameColumn, int batchSize) {
		throw new ShardIdGenException("SnowFlakeIdGenerator not support nextBatch!");
	}

	public void setMachineId(long machineId) {
		if (machineId < 0 || machineId > MAX_MACHINE_ID) {
			throw new ShardIdGenException("Machine id cannot be less than 0 or greater than " + MAX_MACHINE_ID);
		}
		this.machineId = machineId;
	}

	public void setTimeService(IdTimeService timeService) {
		this.timeService = timeService;
	}
}