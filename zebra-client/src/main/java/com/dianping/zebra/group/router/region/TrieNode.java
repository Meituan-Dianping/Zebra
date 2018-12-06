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
package com.dianping.zebra.group.router.region;

import java.util.*;

public class TrieNode<K, V> {
	private K key;

	private SortedMap<K, TrieNode<K, V>> children;

	private Comparator<K> keyComparator;

	private V value;

	private Map<String, String> attributes;

	public TrieNode() {
		this(null, null);
	}

	public TrieNode(K key) {
		this(key, null);
	}

	public TrieNode(K key, Comparator<K> keyComparator) {
		this.key = key;
		this.keyComparator = keyComparator;
	}

	public void addChild(TrieNode<K, V> child) {
		if (this.children == null) {
			this.children = new TreeMap<K, TrieNode<K, V>>(keyComparator);
		}
		children.put(child.getKey(), child);
	}

	public SortedMap<K, TrieNode<K, V>> getChildren() {
		return children;
	}

	public TrieNode<K, V> getChild(K key) {
		if (this.children == null) {
			return null;
		}
		return children.get(key);
	}

	public K getKey() {
		return this.key;
	}

	public V getValue() {
		return this.value;
	}

	public void setValue(V value) {
		this.value = value;
	}

	public String getAttribute(String key) {
		if (key == null) {
			throw new NullPointerException("attribute key is null");
		}
		return attributes == null ? null : attributes.get(key);
	}

	public void setAttribute(String key, String value) {
		if (key == null) {
			throw new NullPointerException("attribute key is null");
		}
		if (value == null) {
			throw new NullPointerException("attribute value is null");
		}
		if (attributes == null) {
			attributes = new HashMap<String, String>();
		}
		attributes.put(key, value);
	}
}