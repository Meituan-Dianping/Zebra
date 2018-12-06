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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class JsonObject {
    private HashMap<String, Object> m_myHashMap;
    public static final Object NULL = new JsonObject.Null((JsonObject.Null) null);

    public JsonObject() {
        this.m_myHashMap = new HashMap<String, Object>();
    }

    public JsonObject(JsonTokener x) throws ParseException {
        this();
        if (x.nextClean() != 123) {
            throw x.syntaxError("A JSONObject must begin with \'{\'");
        } else {
            while (true) {
                char c = x.nextClean();
                switch (c) {
                    case '\u0000':
                        throw x.syntaxError("A JSONObject must end with \'}\'");
                    case '}':
                        return;
                    default:
                        x.back();
                        String key = x.nextValue().toString();
                        c = x.nextClean();
                        if (c == 61) {
                            if (x.next() != 62) {
                                x.back();
                            }
                        } else if (c != 58) {
                            throw x.syntaxError("Expected a \':\' after a key");
                        }

                        this.m_myHashMap.put(key, x.nextValue());
                        switch (x.nextClean()) {
                            case ',':
                            case ';':
                                if (x.nextClean() == 125) {
                                    return;
                                }

                                x.back();
                                break;
                            case '}':
                                return;
                            default:
                                throw x.syntaxError("Expected a \',\' or \'}\'");
                        }
                }
            }
        }
    }

    public JsonObject(String string) throws ParseException {
        this(new JsonTokener(string));
    }

    public Object get(String key) throws NoSuchElementException {
        Object o = this.opt(key);
        if (o == null) {
            throw new NoSuchElementException("JSONObject[" + quote(key) + "] not found.");
        } else {
            return o;
        }
    }

    public boolean getBoolean(String key) throws ClassCastException, NoSuchElementException {
        Object o = this.get(key);
        if (!Boolean.FALSE.equals(o) && (!(o instanceof String) || !("false").equalsIgnoreCase((String) o))) {
            if (!Boolean.TRUE.equals(o) && (!(o instanceof String) || !("true").equalsIgnoreCase((String) o))) {
                throw new ClassCastException("JSONObject[" + quote(key) + "] is not a Boolean.");
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public double getDouble(String key) throws NoSuchElementException, NumberFormatException {
        Object o = this.get(key);
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        } else if (o instanceof String) {
            return (new Double((String) o)).doubleValue();
        } else {
            throw new NumberFormatException("JSONObject[" + quote(key) + "] is not a number.");
        }
    }

    public long getLong(String key) throws NoSuchElementException, NumberFormatException {
        Object o = this.get(key);
        if (o instanceof Number) {
            return ((Number) o).longValue();
        } else if (o instanceof String) {
            return (new Long((String) o)).longValue();
        } else {
            throw new NumberFormatException("JSONObject[" + quote(key) + "] is not a number.");
        }
    }

    public int getInt(String key) throws NoSuchElementException, NumberFormatException {
        Object o = this.get(key);
        return o instanceof Number ? ((Number) o).intValue() : (int) this.getDouble(key);
    }

    public JsonArray getJSONArray(String key) throws NoSuchElementException {
        Object o = this.get(key);
        if (o instanceof JsonArray) {
            return (JsonArray) o;
        } else {
            throw new NoSuchElementException("JSONObject[" + quote(key) + "] is not a JSONArray.");
        }
    }

    public JsonObject getJSONObject(String key) throws NoSuchElementException {
        Object o = this.get(key);
        if (o instanceof JsonObject) {
            return (JsonObject) o;
        } else {
            throw new NoSuchElementException("JSONObject[" + quote(key) + "] is not a JSONObject.");
        }
    }

    public String getString(String key) throws NoSuchElementException {
        return this.get(key).toString();
    }

    public boolean has(String key) {
        return this.m_myHashMap.containsKey(key);
    }

    public Iterator<String> keys() {
        return this.m_myHashMap.keySet().iterator();
    }

    public int length() {
        return this.m_myHashMap.size();
    }

    public static String numberToString(Number n) throws ArithmeticException {
        if ((!(n instanceof Float) || !((Float) n).isInfinite() && !((Float) n).isNaN()) && (!(n instanceof Double) || !((Double) n).isInfinite() && !((Double) n).isNaN())) {
            String s = n.toString();
            if (s.indexOf(46) > 0 && s.indexOf(101) < 0 && s.indexOf(69) < 0) {
                while (s.endsWith("0")) {
                    s = s.substring(0, s.length() - 1);
                }

                if (s.endsWith(".")) {
                    s = s.substring(0, s.length() - 1);
                }
            }

            return s;
        } else {
            throw new ArithmeticException("JSON can only serialize finite numbers.");
        }
    }

    public Object opt(String key) throws NullPointerException {
        if (key == null) {
            throw new NullPointerException("Null key");
        } else {
            return this.m_myHashMap.get(key);
        }
    }

    public JsonObject put(String key, boolean value) {
        this.put(key, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }

    public JsonObject put(String key, double value) {
        this.put(key, new Double(value));
        return this;
    }

    public JsonObject put(String key, int value) {
        this.put(key, new Integer(value));
        return this;
    }

    public JsonObject put(String key, Object value) throws NullPointerException {
        if (key == null) {
            throw new NullPointerException("Null key.");
        } else {
            if (value != null) {
                this.m_myHashMap.put(key, value);
            } else {
                this.remove(key);
            }

            return this;
        }
    }

    public static String quote(String str) {
        int length = str == null ? 4 : str.length() + 4;
        StringBuilder sb = new StringBuilder(length);
        appendQuoted(str, sb);
        return sb.toString();
    }

    protected static StringBuilder appendQuoted(String str, StringBuilder sb) {
        if (str != null && str.length() != 0) {
            sb.append('\"');
            escape(str, false, true, sb);
            sb.append('\"');
            return sb;
        } else {
            sb.append("\"\"");
            return sb;
        }
    }

    protected static int getQuotedSize(String str) {
        return str != null && str.length() != 0 ? (int) (1.3D * (double) str.length()) : 2;
    }

    public static String escape(String str, boolean singleQuote) {
        if (str != null && str.length() != 0) {
            StringBuilder sb = new StringBuilder(str.length());
            escape(str, singleQuote, false, sb);
            return sb.toString();
        } else {
            return str;
        }
    }

    private static void escape(String str, boolean singleQuote, boolean handleSlash, StringBuilder sb) {
        char c = 0;
        int len = str.length();

        for (int i = 0; i < len; ++i) {
            char b = c;
            c = str.charAt(i);
            switch (c) {
                case '\b':
                    sb.append('\\').append('b');
                    break;
                case '\t':
                    sb.append('\\').append('t');
                    break;
                case '\n':
                    sb.append('\\').append('n');
                    break;
                case '\f':
                    sb.append('\\').append('f');
                    break;
                case '\r':
                    sb.append('\\').append('r');
                    break;
                case '\"':
                case '\\':
                    if (!singleQuote) {
                        sb.append('\\');
                    }

                    sb.append(c);
                    break;
                case '\'':
                    if (singleQuote) {
                        sb.append('\\');
                    }

                    sb.append(c);
                    break;
                case '/':
                    if (handleSlash && b == 60) {
                        sb.append('\\');
                    }

                    sb.append(c);
                    break;
                default:
                    if (c < 32) {
                        String t = "000" + Integer.toHexString(c);
                        sb.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }

    }

    public Object remove(String key) {
        return this.m_myHashMap.remove(key);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getSerializedSize());
        return this.append(sb).toString();
    }

    @SuppressWarnings("rawtypes")
    public StringBuilder append(StringBuilder sb) {
        sb.append('{');
        boolean firstEntry = true;
        Iterator<?> var4 = this.m_myHashMap.entrySet().iterator();

        while (var4.hasNext()) {
            Map.Entry entry = (Map.Entry) var4.next();
            if (!firstEntry) {
                sb.append(',');
            } else {
                firstEntry = false;
            }

            appendQuoted((String) entry.getKey(), sb);
            sb.append(':');
            appendValue(entry.getValue(), sb);
        }

        sb.append('}');
        return sb;
    }

    @SuppressWarnings("rawtypes")
    private int getSerializedSize() {
        int value = 2 + this.length() * 2;

        Map.Entry entry;
        for (Iterator<?> var3 = this.m_myHashMap.entrySet().iterator(); var3.hasNext(); value += getQuotedSize((String) entry.getKey()) + getValueSize(entry.getValue())) {
            entry = (Map.Entry) var3.next();
        }

        return (int) (1.2D * (double) value);
    }

    public String toString(int indentFactor) {
        return this.toString(indentFactor, 0);
    }

    String toString(int indentFactor, int indent) {
        int n = this.length();
        if (n == 0) {
            return "{}";
        } else {
            Iterator<String> keys = this.keys();
            StringBuilder sb = new StringBuilder("{");
            int newindent = indent + indentFactor;
            String key;
            if (n == 1) {
                key = (String) keys.next();
                sb.append(quote(key));
                sb.append(": ");
                sb.append(valueToString(this.m_myHashMap.get(key), indentFactor, indent));
            } else {
                while (true) {
                    int i;
                    if (!keys.hasNext()) {
                        if (sb.length() > 1) {
                            sb.append('\n');

                            for (i = 0; i < indent; ++i) {
                                sb.append(' ');
                            }
                        }
                        break;
                    }

                    key = (String) keys.next();
                    if (sb.length() > 1) {
                        sb.append(",\n");
                    } else {
                        sb.append('\n');
                    }

                    for (i = 0; i < newindent; ++i) {
                        sb.append(' ');
                    }

                    sb.append(quote(key));
                    sb.append(": ");
                    sb.append(valueToString(this.m_myHashMap.get(key), indentFactor, newindent));
                }
            }

            sb.append('}');
            return sb.toString();
        }
    }

    protected static StringBuilder appendValue(Object value, StringBuilder sb) {
        return value != null && !value.equals((Object) null) ? (value instanceof String ? appendQuoted((String) value, sb) : (value instanceof Number ? sb.append(numberToString((Number) value)) : (value instanceof Boolean ? sb.append((Boolean) value) : (value instanceof JsonArray ? ((JsonArray) value).append(sb) : (JsonObject.class.isAssignableFrom(value.getClass()) ? ((JsonObject) value).append(sb) : sb.append(value.toString())))))) : sb.append("null");
    }

    protected static int getValueSize(Object value) {
        return value != null && !value.equals((Object) null) ? (value instanceof Number ? 12 : (value instanceof Boolean ? 5 : (value instanceof JsonArray ? ((JsonArray) value).getSerializedSize() : (JsonObject.class.isAssignableFrom(value.getClass()) ? ((JsonObject) value).getSerializedSize() : (!(value instanceof String) ? 32 : getQuotedSize((String) value)))))) : 4;
    }

    static String valueToString(Object value, int indentFactor, int indent) {
        return null;
    }

    private static final class Null {

        public Null(Null aNull) {
        }

        @Override
        protected final Object clone() {
            return this;
        }

        @Override
        public boolean equals(Object object) {
            return object == null || object == this;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public String toString() {
            return "null";
        }
    }
}