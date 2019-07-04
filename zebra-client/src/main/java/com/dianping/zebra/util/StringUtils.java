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
package com.dianping.zebra.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class StringUtils {
	private static final String EMPTY = "";

	private static final int PAD_LIMIT = 8192;

	private static final String platformEncoding = System.getProperty("file.encoding");

	private static final ConcurrentHashMap<String, Charset> charsetsByAlias = new ConcurrentHashMap<String, Charset>();

	public static boolean endsWithIgnoreCase(String s, String suffix) {
		return s.toLowerCase().trim().endsWith(suffix.toLowerCase());
	}

	public static boolean equals(String str1, String str2) {
		return str1 == null ? str2 == null : str1.equals(str2);
	}

	public static boolean isBlank(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if ((Character.isWhitespace(str.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	public static boolean isNotBlank(String str) {
		return !StringUtils.isBlank(str);
	}

	public static boolean isNumeric(String str) {
		if (str == null) {
			return false;
		}
		int sz = str.length();
		for (int i = 0; i < sz; i++) {
			if (Character.isDigit(str.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}

	public static <T extends Object> String joinCollectionToString(Collection<T> list, String str) {
		StringBuffer sb = new StringBuffer(100);
		for (Object entity : list) {
			if (sb.length() > 0) {
				sb.append(str);
			}
			sb.append(String.valueOf(entity));
		}
		return sb.toString();
	}

	public static String joinMapToString(Map<String, String> map) {
		StringBuffer sb = new StringBuffer(100);
		for (Map.Entry<String, String> entity : map.entrySet()) {
			try {
				String temp = String.format("%s=%s", URLEncoder.encode(entity.getKey(), "utf-8"),
				      URLEncoder.encode(entity.getValue(), "utf-8"));
				if (sb.length() > 0) {
					sb.append("&");
				}
				sb.append(temp);
			} catch (UnsupportedEncodingException e) {
				continue;
			}
		}
		return sb.toString();
	}

	public static String leftPad(String str, int size, char padChar) {
		if (str == null) {
			return null;
		}
		int pads = size - str.length();
		if (pads <= 0) {
			return str; // returns original String when possible
		}
		if (pads > PAD_LIMIT) {
			return leftPad(str, size, String.valueOf(padChar));
		}
		return padding(pads, padChar).concat(str);
	}

	public static String leftPad(String str, int size, String padStr) {
		if (str == null) {
			return null;
		}
		if (isEmpty(padStr)) {
			padStr = " ";
		}
		int padLen = padStr.length();
		int strLen = str.length();
		int pads = size - strLen;
		if (pads <= 0) {
			return str; // returns original String when possible
		}
		if (padLen == 1 && pads <= PAD_LIMIT) {
			return leftPad(str, size, padStr.charAt(0));
		}

		if (pads == padLen) {
			return padStr.concat(str);
		} else if (pads < padLen) {
			return padStr.substring(0, pads).concat(str);
		} else {
			char[] padding = new char[pads];
			char[] padChars = padStr.toCharArray();
			for (int i = 0; i < pads; i++) {
				padding[i] = padChars[i % padLen];
			}
			return new String(padding).concat(str);
		}
	}

	private static String padding(int repeat, char padChar) throws IndexOutOfBoundsException {
		if (repeat < 0) {
			throw new IndexOutOfBoundsException("Cannot pad a negative amount: " + repeat);
		}
		final char[] buf = new char[repeat];
		for (int i = 0; i < buf.length; i++) {
			buf[i] = padChar;
		}
		return new String(buf);
	}

	public static String repeat(String str, int repeat) {
		StringBuffer buffer = new StringBuffer(repeat * str.length());
		for (int i = 0; i < repeat; i++) {
			buffer.append(str);
		}
		return buffer.toString();
	}

	public static String md5(String input) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] messageDigest = md.digest(input.getBytes());
		BigInteger number = new BigInteger(1, messageDigest);
		String hashtext = number.toString(16);
		while (hashtext.length() < 32) {
			hashtext = "0" + hashtext;
		}
		return hashtext;
	}

	public static String sha1(String input) throws NoSuchAlgorithmException {
		MessageDigest mDigest = MessageDigest.getInstance("SHA-1");
		byte[] result = mDigest.digest(input.getBytes());
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < result.length; i++) {
			sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

	public static void splitStringToMap(Map<String, String> map, String input) {
		if (StringUtils.isBlank(input) || map == null) {
			return;
		}

		for (String keyValue : input.split("&")) {
			String[] keyValueArray = keyValue.split("=");
			if (keyValueArray.length != 2) {
				continue;
			}
			try {
				String key = URLDecoder.decode(keyValueArray[0], "utf-8");
				String value = URLDecoder.decode(keyValueArray[1], "utf-8");

				map.put(key, value);

			} catch (UnsupportedEncodingException e) {
				continue;
			}
		}
	}

	/**
	 * <pre>
	 * Determines whether or not the string 'searchIn' contains the string
	 * 'searchFor', dis-regarding case starting at 'startAt' Shorthand for a
	 * String.regionMatch(...)
	 * 
	 * From mysql connector-j
	 * </pre>
	 *
	 * @param searchIn
	 *           the string to search in
	 * @param startAt
	 *           the position to start at
	 * @param searchFor
	 *           the string to search for
	 * @return whether searchIn starts with searchFor, ignoring case
	 */
	public static boolean startsWithIgnoreCase(String searchIn, int startAt, String searchFor) {
		return searchIn.regionMatches(true, startAt, searchFor, 0, searchFor.length());
	}

	/**
	 * <pre>
	 * Determines whether or not the string 'searchIn' contains the string
	 * 'searchFor', dis-regarding case. Shorthand for a String.regionMatch(...)
	 * 
	 * From mysql connector-j
	 * </pre>
	 *
	 * @param searchIn
	 *           the string to search in
	 * @param searchFor
	 *           the string to search for
	 * @return whether searchIn starts with searchFor, ignoring case
	 */
	public static boolean startsWithIgnoreCase(String searchIn, String searchFor) {
		return startsWithIgnoreCase(searchIn, 0, searchFor);
	}

	/**
	 * <pre>
	 * Determines whether or not the sting 'searchIn' contains the string
	 * 'searchFor', disregarding case and leading whitespace
	 * 
	 * From mysql connector-j
	 * </pre>
	 *
	 * @param searchIn
	 *           the string to search in
	 * @param searchFor
	 *           the string to search for
	 * @return true if the string starts with 'searchFor' ignoring whitespace
	 */
	public static boolean startsWithIgnoreCaseAndWs(String searchIn, String searchFor) {
		return startsWithIgnoreCaseAndWs(searchIn, searchFor, 0);
	}

	/**
	 * <pre>
	 * Determines whether or not the sting 'searchIn' contains the string
	 * 'searchFor', disregarding case and leading whitespace
	 * 
	 * From mysql connector-j
	 * </pre>
	 *
	 * @param searchIn
	 *           the string to search in
	 * @param searchFor
	 *           the string to search for
	 * @param beginPos
	 *           where to start searching
	 * @return true if the string starts with 'searchFor' ignoring whitespace
	 */

	public static boolean startsWithIgnoreCaseAndWs(String searchIn, String searchFor, int beginPos) {
		if (searchIn == null) {
			return searchFor == null;
		}

		int inLength = searchIn.length();

		for (; beginPos < inLength; beginPos++) {
			if (!Character.isWhitespace(searchIn.charAt(beginPos))) {
				break;
			}
		}

		return startsWithIgnoreCase(searchIn, beginPos, searchFor);
	}

	/**
	 * <pre>
	 * Returns the given string, with comments removed
	 * 
	 * From mysql connector-j
	 * </pre>
	 *
	 * @param src
	 *           the source string
	 * @param stringOpens
	 *           characters which delimit the "open" of a string
	 * @param stringCloses
	 *           characters which delimit the "close" of a string, in counterpart order to <code>stringOpens</code>
	 * @param slashStarComments
	 *           strip slash-star type "C" style comments
	 * @param slashSlashComments
	 *           strip slash-slash C++ style comments to end-of-line
	 * @param hashComments
	 *           strip #-style comments to end-of-line
	 * @param dashDashComments
	 *           strip "--" style comments to end-of-line
	 * @return the input string with all comment-delimited data removed
	 */
	public static String stripComments(String src, String stringOpens, String stringCloses, boolean slashStarComments,
	      boolean slashSlashComments, boolean hashComments, boolean dashDashComments) {
		if (src == null) {
			return null;
		}

		StringBuffer buf = new StringBuffer(src.length());

		// It's just more natural to deal with this as a stream
		// when parsing..This code is currently only called when
		// parsing the kind of metadata that developers are strongly
		// recommended to cache anyways, so we're not worried
		// about the _1_ extra object allocation if it cleans
		// up the code

		StringReader sourceReader = new StringReader(src);

		int contextMarker = Character.MIN_VALUE;
		boolean escaped = false;
		int markerTypeFound = -1;

		int ind = 0;

		int currentChar = 0;

		try {
			while ((currentChar = sourceReader.read()) != -1) {

				if (markerTypeFound != -1 && currentChar == stringCloses.charAt(markerTypeFound) && !escaped) {
					contextMarker = Character.MIN_VALUE;
					markerTypeFound = -1;
				} else if ((ind = stringOpens.indexOf(currentChar)) != -1 && !escaped
				      && contextMarker == Character.MIN_VALUE) {
					markerTypeFound = ind;
					contextMarker = currentChar;
				}

				if (contextMarker == Character.MIN_VALUE && currentChar == '/' && (slashSlashComments || slashStarComments)) {
					currentChar = sourceReader.read();
					if (currentChar == '*' && slashStarComments) {
						int prevChar = 0;
						while ((currentChar = sourceReader.read()) != '/' || prevChar != '*') {
							if (currentChar == '\r') {

								currentChar = sourceReader.read();
								if (currentChar == '\n') {
									currentChar = sourceReader.read();
								}
							} else {
								if (currentChar == '\n') {

									currentChar = sourceReader.read();
								}
							}
							if (currentChar < 0) {
								break;
							}
							prevChar = currentChar;
						}
						continue;
					} else if (currentChar == '/' && slashSlashComments) {
						do {
							currentChar = sourceReader.read();
						} while (currentChar != '\n' && currentChar != '\r' && currentChar >= 0);
					}
				} else if (contextMarker == Character.MIN_VALUE && currentChar == '#' && hashComments) {
					// Slurp up everything until the newline
					do {
						currentChar = sourceReader.read();
					} while (currentChar != '\n' && currentChar != '\r' && currentChar >= 0);
				} else if (contextMarker == Character.MIN_VALUE && currentChar == '-' && dashDashComments) {
					currentChar = sourceReader.read();

					if (currentChar == -1 || currentChar != '-') {
						buf.append('-');

						if (currentChar != -1) {
							buf.append((char) currentChar);
						}

						continue;
					}

					// Slurp up everything until the newline

					do {
						currentChar = sourceReader.read();
					} while (currentChar != '\n' && currentChar != '\r' && currentChar >= 0);
				}

				if (currentChar != -1) {
					buf.append((char) currentChar);
				}
			}
		} catch (IOException ioEx) {
			// we'll never see this from a StringReader
		}

		return buf.toString();
	}

	public static String substring(String str, int start) {
		if (str == null) {
			return null;
		}

		// handle negatives, which means last n characters
		if (start < 0) {
			start = str.length() + start; // remember start is negative
		}

		if (start < 0) {
			start = 0;
		}
		if (start > str.length()) {
			return EMPTY;
		}

		return str.substring(start);
	}

	public static String substringAfter(String str, String separator) {
		if (isEmpty(str)) {
			return str;
		}
		if (separator == null) {
			return EMPTY;
		}
		int pos = str.indexOf(separator);
		if (pos == -1) {
			return EMPTY;
		}
		return str.substring(pos + separator.length());
	}

	public static String substringAfterLast(String str, String separator) {
		if (isEmpty(str)) {
			return str;
		}
		if (isEmpty(separator)) {
			return EMPTY;
		}
		int pos = str.lastIndexOf(separator);
		if (pos == -1 || pos == (str.length() - separator.length())) {
			return EMPTY;
		}
		return str.substring(pos + separator.length());
	}

	public static String substringBefore(String str, String separator) {
		if (isEmpty(str) || separator == null) {
			return str;
		}
		if (separator.length() == 0) {
			return EMPTY;
		}
		int pos = str.indexOf(separator);
		if (pos == -1) {
			return str;
		}
		return str.substring(0, pos);
	}

	public static String substringBeforeLast(String str, String separator) {
		if (isEmpty(str) || isEmpty(separator)) {
			return str;
		}
		int pos = str.lastIndexOf(separator);
		if (pos == -1) {
			return str;
		}
		return str.substring(0, pos);
	}

	public static String substringBetween(String str, String open, String close) {
		if (str == null || open == null || close == null) {
			return null;
		}
		int start = str.indexOf(open);
		if (start != -1) {
			int end = str.indexOf(close, start + open.length());
			if (end != -1) {
				return str.substring(start + open.length(), end);
			}
		}
		return null;
	}

	public static String trimToEmpty(String str) {
		return str == null ? EMPTY : str.trim();
	}

	public static String upperFirstChar(String str) {
		if (str == null || str.length() == 0) {
			return str;
		}
		char[] charArray = str.toCharArray();
		charArray[0] = str.substring(0, 1).toUpperCase().charAt(0);
		return String.valueOf(charArray);
	}

	public static byte[] getBytes(String value) {
		try {
			Charset cs = findCharset(platformEncoding);
			ByteBuffer buf = cs.encode(CharBuffer.wrap(value.toCharArray(), 0, value.length()));

			// can't simply .array() this to get the bytes especially with variable-length charsets the buffer is sometimes larger than
			// the actual encoded data
			int encodedLen = buf.limit();
			byte[] asBytes = new byte[encodedLen];
			buf.get(asBytes, 0, encodedLen);

			return asBytes;
		} catch (UnsupportedEncodingException e) {
		}

		return null;
	}

	static Charset findCharset(String alias) throws UnsupportedEncodingException {
		try {
			Charset cs = charsetsByAlias.get(alias);

			if (cs == null) {
				cs = Charset.forName(alias);
				charsetsByAlias.putIfAbsent(alias, cs);
			}

			return cs;

			// We re-throw these runtimes for compatibility with java.io
		} catch (UnsupportedCharsetException uce) {
			throw new UnsupportedEncodingException(alias);
		} catch (IllegalCharsetNameException icne) {
			throw new UnsupportedEncodingException(alias);
		} catch (IllegalArgumentException iae) {
			throw new UnsupportedEncodingException(alias);
		}
	}

	public static String toAsciiString(byte[] bytes) {
		int length = bytes.length;
		char[] charArray = new char[length];
		int readpoint = 0;

		for (int i = 0; i < length; i++) {
			charArray[i] = (char) bytes[readpoint];
			readpoint++;
		}

		return new String(charArray);
	}
}
