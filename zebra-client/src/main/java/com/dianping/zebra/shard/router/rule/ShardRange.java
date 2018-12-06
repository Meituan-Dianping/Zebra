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
package com.dianping.zebra.shard.router.rule;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;

public class ShardRange {
	public final static int OP_NONE = -1;

	public final static int OP_Equal = 0;

	public final static int OP_Greater = 1;

	public final static int OP_GreaterOrEqual = 2;

	public final static int OP_Less = 3;

	public final static int OP_LessOrEqual = 4;

	public final static int OP_InList = 5;

	public final static int OP_BetweenAnd = 6;

	private int firstType;

	private Object firstParameter;

	private int secondType;

	private Object secondParameter;

	public ShardRange(Object firstParameter) {
		this.firstParameter = firstParameter;
	}

	public ShardRange(int firstType, Object firstParameter) {
		this.firstType = firstType;
		this.firstParameter = firstParameter;
	}

	// a </<= x </<=
	public ShardRange(int firstType, Object firstParameter, int secondType, Object secondParameter) {
		if ((firstType == 1 || firstType == 2) && (secondType == 3 || secondType == 4)) {
			this.firstType = firstType;
			this.firstParameter = firstParameter;
			this.secondType = secondType;
			this.secondParameter = secondParameter;
		} else if ((firstType == 3 || firstType == 4) && (secondType == 1 || secondType == 2)) {
			this.firstType = secondType;
			this.firstParameter = secondParameter;
			this.secondType = firstType;
			this.secondParameter = firstParameter;
		} else {
			// TODO
		}
	}

	public ShardRange(SQLBinaryOperator operator, Object firstParameter) {
		this.firstType = convertOperator(operator);
		this.firstParameter = firstParameter;
	}

	private static int convertOperator(SQLBinaryOperator operator) {
		if (operator == SQLBinaryOperator.GreaterThan) {
			return ShardRange.OP_Greater;
		} else if (operator == SQLBinaryOperator.GreaterThanOrEqual) {
			return ShardRange.OP_GreaterOrEqual;
		} else if (operator == SQLBinaryOperator.LessThan) {
			return ShardRange.OP_Less;
		} else if (operator == SQLBinaryOperator.LessThanOrEqual) {
			return ShardRange.OP_LessOrEqual;
		} else if (operator == SQLBinaryOperator.Equality) {
			return ShardRange.OP_Equal;
		} else {
			return ShardRange.OP_NONE;
		}
	}

	public boolean isEqual() {
		return this.firstType == ShardRange.OP_Equal;
	}

	public boolean isIn() {
		return this.firstType == ShardRange.OP_InList;
	}

	public boolean isGreater() {
		return this.firstType == ShardRange.OP_Greater;
	}

	public boolean isGreaterOrEqual() {
		return this.firstType == ShardRange.OP_GreaterOrEqual;
	}

	public boolean isLess() {
		return this.firstType == ShardRange.OP_Less;
	}

	public boolean isLessOrEqual() {
		return this.firstType == ShardRange.OP_LessOrEqual;
	}

	public boolean isBetweenAnd() {
		return this.firstType == ShardRange.OP_BetweenAnd;
	}

	public boolean onlyHasFirstParameter() {
		return (firstParameter != null && secondParameter == null);
	}

	public int getFirstType() {
		return firstType;
	}

	public void setFirstType(int firstType) {
		this.firstType = firstType;
	}

	public Object getFirstParameter() {
		return firstParameter;
	}

	public void setFirstParameter(Object firstParameter) {
		this.firstParameter = firstParameter;
	}

	public int getSecondType() {
		return secondType;
	}

	public void setSecondType(int secondType) {
		this.secondType = secondType;
	}

	public Object getSecondParameter() {
		return secondParameter;
	}

	public void setSecondParameter(Object secondParameter) {
		this.secondParameter = secondParameter;
	}
}
