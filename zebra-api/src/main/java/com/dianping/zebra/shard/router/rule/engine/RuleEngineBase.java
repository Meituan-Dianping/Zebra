package com.dianping.zebra.shard.router.rule.engine;

import com.dianping.zebra.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.CRC32;

public class RuleEngineBase {
	public static final int SKIP = -1;

	public long crc32(Object str) throws UnsupportedEncodingException {
		return crc32(str, "utf-8");
	}

	public Date date(Object value) throws ParseException {
		if (value instanceof Date) {
			return (Date) value;
		}
		if (value instanceof String) {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.SIMPLIFIED_CHINESE);
			return format.parse((String) value);
		}

		throw new IllegalArgumentException();
	}

	public String md5(String input) throws NoSuchAlgorithmException {
		return StringUtils.md5(input);
	}

	public long crc32(Object str, String encode) throws UnsupportedEncodingException {
		CRC32 crc32 = new CRC32();
		crc32.update(String.valueOf(str).getBytes(encode));
		return crc32.getValue();
	}
}
