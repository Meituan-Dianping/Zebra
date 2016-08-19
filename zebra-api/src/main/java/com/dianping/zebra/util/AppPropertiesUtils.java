package com.dianping.zebra.util;

import com.dianping.zebra.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class AppPropertiesUtils {

	public static String getAppName() {
		URL appProperties = AppPropertiesUtils.class.getResource("/META-INF/app.properties");

		if (appProperties != null) {
			InputStream in = null;
			try {
				in = appProperties.openStream();
				Properties properties = new Properties();
				properties.load(in);

				String appName = (String) properties.get("app.name");

				if (appName != null) {
					return appName;
				} else {
					return Constants.PHOENIX_APP_NO_NAME;
				}
			} catch (IOException ignore) {
				return Constants.PHOENIX_APP_NO_NAME;
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (Exception e) {
						// ignore it
					}
				}
			}
		} else {
			return Constants.PHOENIX_APP_NO_NAME;
		}
	}
}
