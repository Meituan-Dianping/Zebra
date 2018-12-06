package com.dianping.zebra.shard.resultset;

import java.io.*;
import java.sql.SQLException;

import com.dianping.zebra.util.StringUtils;

public class Clob implements java.sql.Clob {

	private String charData;

	public Clob(String charDataInit) {
		this.charData = charDataInit;
	}

	@Override
	public long length() throws SQLException {
		if (this.charData != null) {
			return this.charData.length();
		}

		return 0;
	}

	@Override
	public String getSubString(long startPos, int length) throws SQLException {
		if (startPos < 1) {
			throw new SQLException("CLOB start position can not be < 1");
		}

		int adjustedStartPos = (int) startPos - 1;
		int adjustedEndIndex = adjustedStartPos + length;

		if (this.charData != null) {
			if (adjustedEndIndex > this.charData.length()) {
				throw new SQLException("CLOB start position + length can not be > length of CLOB");
			}

			return this.charData.substring(adjustedStartPos, adjustedEndIndex);
		}

		return null;
	}

	@Override
	public Reader getCharacterStream() throws SQLException {
		if (this.charData != null) {
			return new StringReader(this.charData);
		}

		return null;
	}

	@Override
	public InputStream getAsciiStream() throws SQLException {
		if (this.charData != null) {
			return new ByteArrayInputStream(StringUtils.getBytes(this.charData));
		}

		return null;
	}

	@Override
	public long position(String stringToFind, long startPos) throws SQLException {
		if (startPos < 1) {
			throw new SQLException("Illegal starting position for search, '" + startPos + "'");
		}

		if (this.charData != null) {
			if ((startPos - 1) > this.charData.length()) {
				throw new SQLException("Starting position for search is past end of CLOB");
			}

			int pos = this.charData.indexOf(stringToFind, (int) (startPos - 1));

			return (pos == -1) ? (-1) : (pos + 1);
		}

		return -1;
	}

	@Override
	public long position(java.sql.Clob searchstr, long start) throws SQLException {
		return position(searchstr.getSubString(0L, (int) searchstr.length()), start);
	}

	@Override
	public int setString(long pos, String str) throws SQLException {
		if (pos < 1) {
			throw new SQLException("Starting position can not be < 1");
		}

		if (str == null) {
			throw new SQLException("String to set can not be NULL");
		}

		StringBuffer charBuf = new StringBuffer(this.charData);

		pos--;

		int strLength = str.length();

		charBuf.replace((int) pos, (int) (pos + strLength), str);

		this.charData = charBuf.toString();

		return strLength;
	}

	@Override
	public int setString(long pos, String str, int offset, int len) throws SQLException {
		if (pos < 1) {
			throw new SQLException("Starting position can not be < 1");
		}

		if (str == null) {
			throw new SQLException("String to set can not be NULL");
		}

		StringBuffer charBuf = new StringBuffer(this.charData);

		pos--;

		String replaceString = str.substring(offset, len);

		charBuf.replace((int) pos, (int) (pos + replaceString.length()), replaceString);

		this.charData = charBuf.toString();

		return len;
	}

	@Override
	public OutputStream setAsciiStream(long indexToWriteAt) throws SQLException {
		if (indexToWriteAt < 1) {
			throw new SQLException("indexToWriteAt must be >= 1");
		}

		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();

		if (indexToWriteAt > 0) {
			bytesOut.write(StringUtils.getBytes(this.charData), 0, (int) (indexToWriteAt - 1));
		}

		return bytesOut;
	}

	@Override
	public Writer setCharacterStream(long indexToWriteAt) throws SQLException {
		if (indexToWriteAt < 1) {
			throw new SQLException("indexToWriteAt must be >= 1");
		}

		// Don't call write() if nothing to write...
		//
		CharArrayWriter writer = new CharArrayWriter();

		if (indexToWriteAt > 1) {
			writer.write(this.charData, 0, (int) (indexToWriteAt - 1));
		}

		return writer;
	}

	@Override
	public void truncate(long length) throws SQLException {
		if (length > this.charData.length()) {
			throw new SQLException("Cannot truncate CLOB of length" + this.charData.length()
			      + "to length of" + length + ".");
		}

		this.charData = this.charData.substring(0, (int) length);
	}

	@Override
	public void free() throws SQLException {
		this.charData = null;
	}

	@Override
	public Reader getCharacterStream(long pos, long length) throws SQLException {
		return new StringReader(getSubString(pos, (int) length));
	}
}
