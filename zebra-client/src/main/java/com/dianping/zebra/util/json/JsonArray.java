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
package com.dianping.zebra.util.json;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class JsonArray {
    private ArrayList<Object> m_myArrayList;

    public JsonArray() {
        this.m_myArrayList = new ArrayList<Object>();
    }

    public JsonArray(JsonTokener x) throws ParseException {
        this();
        if (x.nextClean() != 91) {
            throw x.syntaxError("A JSONArray must start with \'[\'");
        } else if (x.nextClean() != 93) {
            x.back();

            while (true) {
                if (x.nextClean() == 44) {
                    x.back();
                    this.m_myArrayList.add((Object) null);
                } else {
                    x.back();
                    this.m_myArrayList.add(x.nextValue());
                }

                switch (x.nextClean()) {
                    case ',':
                    case ';':
                        if (!x.more()) {
                            throw x.syntaxError("Expected a \']\'");
                        }

                        if (x.nextClean() == 93) {
                            return;
                        }

                        x.back();
                        break;
                    case ']':
                        return;
                    default:
                        throw x.syntaxError("Expected a \',\' or \']\'");
                }
            }
        }
    }

    public JsonArray(String string) throws ParseException {
        this(new JsonTokener(string));
    }

    public JsonArray(Collection<Object> collection) {
        this.m_myArrayList = new ArrayList<Object>(collection);
    }

    public Object get(int index) throws NoSuchElementException {
        Object o = this.opt(index);
        if (o == null) {
            throw new NoSuchElementException("JSONArray[" + index + "] not found.");
        } else {
            return o;
        }
    }

    public ArrayList<Object> getArrayList() {
        return this.m_myArrayList;
    }

    public boolean getBoolean(int index) throws ClassCastException, NoSuchElementException {
        Object o = this.get(index);
        if (!Boolean.FALSE.equals(o) && (!(o instanceof String) || !("false").equalsIgnoreCase((String)o))) {
            if (!Boolean.TRUE.equals(o) && (!(o instanceof String) || !("true").equalsIgnoreCase((String)o))) {
                throw new ClassCastException("JSONArray[" + index + "] not a Boolean.");
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public double getDouble(int index) throws NoSuchElementException, NumberFormatException {
        Object o = this.get(index);
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        } else if (o instanceof String) {
            return (new Double((String) o)).doubleValue();
        } else {
            throw new NumberFormatException("JSONObject[" + index + "] is not a number.");
        }
    }

    public int getInt(int index) throws NoSuchElementException, NumberFormatException {
        Object o = this.get(index);
        return o instanceof Number ? ((Number) o).intValue() : (int) this.getDouble(index);
    }

    public JsonArray getJSONArray(int index) throws NoSuchElementException {
        Object o = this.get(index);
        if (o instanceof JsonArray) {
            return (JsonArray) o;
        } else {
            throw new NoSuchElementException("JSONArray[" + index + "] is not a JSONArray.");
        }
    }

    public JsonObject getJSONObject(int index) throws NoSuchElementException {
        Object o = this.get(index);
        if (o instanceof JsonObject) {
            return (JsonObject) o;
        } else {
            throw new NoSuchElementException("JSONArray[" + index + "] is not a JSONObject.");
        }
    }

    public String getString(int index) throws NoSuchElementException {
        return this.get(index).toString();
    }

    private StringBuilder appendJoin(String separator, StringBuilder sb) {
        int len = this.length();

        for (int i = 0; i < len; ++i) {
            if (i > 0) {
                sb.append(separator);
            }

            JsonObject.appendValue(this.m_myArrayList.get(i), sb);
        }

        return sb;
    }

    public int length() {
        return this.m_myArrayList.size();
    }

    public Object opt(int index) {
        return index >= 0 && index < this.length() ? this.m_myArrayList.get(index) : null;
    }

    public JsonArray put(boolean value) {
        this.put(value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }

    public JsonArray put(double value) {
        this.put(new Double(value));
        return this;
    }

    public JsonArray put(int value) {
        this.put(new Integer(value));
        return this;
    }

    public JsonArray put(Object value) {
        this.m_myArrayList.add(value);
        return this;
    }

    public JsonArray put(int index, boolean value) {
        this.put(index, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }

    public JsonArray put(int index, double value) {
        this.put(index, new Double(value));
        return this;
    }

    public JsonArray put(int index, int value) {
        this.put(index, new Integer(value));
        return this;
    }

    public JsonArray put(int index, Object value) throws NoSuchElementException, NullPointerException {
        if (index < 0) {
            throw new NoSuchElementException("JSONArray[" + index + "] not found.");
        } else if (value == null) {
            throw new NullPointerException();
        } else {
            if (index < this.length()) {
                this.m_myArrayList.set(index, value);
            } else {
                while (index != this.length()) {
                    this.put((Object) null);
                }

                this.put(value);
            }

            return this;
        }
    }

    public JsonObject toJSONObject(JsonArray names) {
        if (names != null && names.length() != 0 && this.length() != 0) {
            JsonObject jo = new JsonObject();

            for (int i = 0; i < names.length(); ++i) {
                jo.put(names.getString(i), this.opt(i));
            }

            return jo;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getSerializedSize());
        return this.append(sb).toString();
    }

    StringBuilder append(StringBuilder sb) {
        sb.append('[');
        this.appendJoin(",", sb);
        sb.append(']');
        return sb;
    }

    int getSerializedSize() {
        int arraySize = this.length();
        int value = 2 + arraySize;

        Object obj;
        for (Iterator<Object> var4 = this.m_myArrayList.iterator(); var4.hasNext(); value += JsonObject.getValueSize(obj)) {
            obj = var4.next();
        }

        return (int) (1.2D * (double) value);
    }

    public String toString(int indentFactor) {
        return this.toString(indentFactor, 0);
    }

    String toString(int indentFactor, int indent) {
        int len = this.length();
        if (len == 0) {
            return "[]";
        } else {
            StringBuilder sb = new StringBuilder("[");
            if (len == 1) {
                sb.append(JsonObject.valueToString(this.m_myArrayList.get(0), indentFactor, indent));
            } else {
                int newindent = indent + indentFactor;
                sb.append('\n');

                int i;
                for (i = 0; i < len; ++i) {
                    if (i > 0) {
                        sb.append(",\n");
                    }

                    for (int j = 0; j < newindent; ++j) {
                        sb.append(' ');
                    }

                    sb.append(JsonObject.valueToString(this.m_myArrayList.get(i), indentFactor, newindent));
                }

                sb.append('\n');

                for (i = 0; i < indent; ++i) {
                    sb.append(' ');
                }
            }

            sb.append(']');
            return sb.toString();
        }
    }
}
