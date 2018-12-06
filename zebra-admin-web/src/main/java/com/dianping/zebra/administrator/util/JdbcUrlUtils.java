package com.dianping.zebra.administrator.util;

import com.dianping.zebra.administrator.exception.ZebraException;
import com.dianping.zebra.shard.util.LRUCache;
import com.dianping.zebra.util.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author tong.xin on 2018/11/30.
 */
public class JdbcUrlUtils {
	private static Pattern urlPattern = Pattern.compile("\\w+:\\w+://([^:]+:\\d+)/([^\\?]+).*");

	public static JdbcUrlAnalysisResult analysis(String jdbcUrl) throws ZebraException {
		if (StringUtils.isNotBlank(jdbcUrl)) {
			if (jdbcUrl.startsWith("jdbc:")) {
				if (jdbcUrl.startsWith("jdbc:h2:")) {
					// just for test
					JdbcUrlAnalysisResult result = new JdbcUrlAnalysisResult();
					result.setAddress("127.0.0.1");
					result.setPort("3306");
					result.setDatabase("test");

					return result;
				}

				try {
					Matcher m = urlPattern.matcher(jdbcUrl);
					JdbcUrlAnalysisResult result = new JdbcUrlAnalysisResult();
					if (m.matches()) {
						String url = m.group(1);
						String[] urlAndPort = url.split(":");

						result.setAddress(urlAndPort[0]);
						result.setPort(urlAndPort[1]);
						result.setDatabase(m.group(2));
						int startIndex = jdbcUrl.indexOf("socketTimeout");
						if (startIndex != -1) {
							startIndex = startIndex + 14;
							int endIndex = jdbcUrl.indexOf("&", startIndex);
							if (endIndex == -1) {
								int socketTimeout = Integer.parseInt(jdbcUrl.substring(startIndex));
								result.setSocketTimeout(socketTimeout);
							} else {
								int socketTimeout = Integer.parseInt(jdbcUrl.substring(startIndex, endIndex));
								result.setSocketTimeout(socketTimeout);
							}
						}
						return result;
					}
				} catch (Exception e) {
					throw new ZebraException("cannot analysis jdbcurl " + jdbcUrl, e);
				}
			}
		}

		throw new ZebraException("cannot analysis jdbcurl:" + jdbcUrl);
	}

	public static class JdbcUrlAnalysisResult {
		private String address;

		private String port;

		private String database;

		private int socketTimeout;

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public String getPort() {
			return port;
		}

		public void setPort(String port) {
			this.port = port;
		}

		public String getDatabase() {
			return database;
		}

		public void setDatabase(String database) {
			this.database = database;
		}

		public int getSocketTimeout() {
			return socketTimeout;
		}

		public void setSocketTimeout(int socketTimeout) {
			this.socketTimeout = socketTimeout;
		}

		@Override
		public String toString() {
			return "JdbcUrlAnalysisResult{" +
					"address='" + address + '\'' +
					", port='" + port + '\'' +
					", database='" + database + '\'' +
					", socketTimeout=" + socketTimeout +
					'}';
		}
	}
}
