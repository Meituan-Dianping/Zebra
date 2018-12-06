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

public class JsonTokener {
    private int myIndex = 0;
    private String mySource;

    public JsonTokener(String s) {
        this.mySource = s;
    }

    public void back() {
        if (this.myIndex > 0) {
            --this.myIndex;
        }

    }

    public boolean more() {
        return this.myIndex < this.mySource.length();
    }

    public char next() {
        if (this.more()) {
            char c = this.mySource.charAt(this.myIndex);
            ++this.myIndex;
            return c;
        } else {
            return '\u0000';
        }
    }

    public char next(char c) throws ParseException {
        char n = this.next();
        if (n != c) {
            throw this.syntaxError("Expected \'" + c + "\' and instead saw \'" + n + "\'.");
        } else {
            return n;
        }
    }

    public String next(int n) throws ParseException {
        int i = this.myIndex;
        int j = i + n;
        if (j >= this.mySource.length()) {
            throw this.syntaxError("Substring bounds error");
        } else {
            this.myIndex += n;
            return this.mySource.substring(i, j);
        }
    }

    public char nextClean() throws ParseException {
        label52:
        while (true) {
            char c = this.next();
            if (c == 47) {
                switch (this.next()) {
                    case '*':
                        while (true) {
                            c = this.next();
                            if (c == 0) {
                                throw this.syntaxError("Unclosed comment.");
                            }

                            if (c == 42) {
                                if (this.next() == 47) {
                                    continue label52;
                                }

                                this.back();
                            }
                        }
                    case '/':
                        while (true) {
                            c = this.next();
                            if (c == 10 || c == 13 || c == 0) {
                                continue label52;
                            }
                        }
                    default:
                        this.back();
                        return '/';
                }
            } else if (c == 35) {
                while (true) {
                    c = this.next();
                    if (c == 10 || c == 13 || c == 0) {
                        break;
                    }
                }
            } else if (c == 0 || c > 32) {
                return c;
            }
        }
    }

    public String nextString(char quote) throws ParseException {
        StringBuffer sb = new StringBuffer();

        while (true) {
            char c = this.next();
            switch (c) {
                case '\u0000':
                case '\n':
                case '\r':
                    throw this.syntaxError("Unterminated string");
                case '\\':
                    c = this.next();
                    switch (c) {
                        case 'b':
                            sb.append('\b');
                            continue;
                        case 'f':
                            sb.append('\f');
                            continue;
                        case 'n':
                            sb.append('\n');
                            continue;
                        case 'r':
                            sb.append('\r');
                            continue;
                        case 't':
                            sb.append('\t');
                            continue;
                        case 'u':
                            sb.append((char) Integer.parseInt(this.next((int) 4), 16));
                            continue;
                        case 'x':
                            sb.append((char) Integer.parseInt(this.next((int) 2), 16));
                            continue;
                        default:
                            sb.append(c);
                            continue;
                    }
                default:
                    if (c == quote) {
                        return sb.toString();
                    }

                    sb.append(c);
            }
        }
    }

    public String nextTo(char d) {
        StringBuffer sb = new StringBuffer();

        while (true) {
            char c = this.next();
            if (c == d || c == 0 || c == 10 || c == 13) {
                if (c != 0) {
                    this.back();
                }

                return sb.toString().trim();
            }

            sb.append(c);
        }
    }

    public String nextTo(String delimiters) {
        StringBuffer sb = new StringBuffer();

        while (true) {
            char c = this.next();
            if (delimiters.indexOf(c) >= 0 || c == 0 || c == 10 || c == 13) {
                if (c != 0) {
                    this.back();
                }

                return sb.toString().trim();
            }

            sb.append(c);
        }
    }

    public Object nextValue() throws ParseException {
        char c = this.nextClean();
        switch (c) {
            case '\"':
            case '\'':
                return this.nextString(c);
            case '[':
                this.back();
                return new JsonArray(this);
            case '{':
                this.back();
                return new JsonObject(this);
            default:
                StringBuffer sb = new StringBuffer();

                char b;
                for (b = c; c >= 32 && ",:]}/\\\"[{;=#".indexOf(c) < 0; c = this.next()) {
                    sb.append(c);
                }

                this.back();
                String s = sb.toString().trim();
                if ("".equals(s)) {
                    throw this.syntaxError("Missing value.");
                } else if ("true".equalsIgnoreCase(s)) {
                    return Boolean.TRUE;
                } else if ("false".equalsIgnoreCase(s)) {
                    return Boolean.FALSE;
                } else if ("null".equalsIgnoreCase(s)) {
                    return JsonObject.NULL;
                } else {
                    if (b >= 48 && b <= 57 || b == 46 || b == 45 || b == 43) {
                        if (b == 48) {
                            if (s.length() > 2 && (s.charAt(1) == 120 || s.charAt(1) == 88)) {
                                try {
                                    return new Integer(Integer.parseInt(s.substring(2), 16));
                                } catch (Exception var9) {
                                    ;
                                }
                            } else {
                                try {
                                    return new Integer(Integer.parseInt(s, 8));
                                } catch (Exception var8) {
                                    ;
                                }
                            }
                        }

                        try {
                            return new Integer(s);
                        } catch (Exception var7) {
                            try {
                                return new Double(s);
                            } catch (Exception var6) {
                                ;
                            }
                        }
                    }

                    return s;
                }
        }
    }

    public ParseException syntaxError(String message) {
        return new ParseException(message + this.toString(), this.myIndex);
    }

    @Override
    public String toString() {
        return " at character " + this.myIndex + " of " + this.mySource;
    }
}
