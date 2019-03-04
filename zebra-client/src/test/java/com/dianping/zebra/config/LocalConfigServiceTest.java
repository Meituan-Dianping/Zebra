package com.dianping.zebra.config;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dianping.zebra.Constants;

public class LocalConfigServiceTest {

	private String resourceId = "zebra.system";

	private ConfigService localConfigService;

	private Properties props;

	@Before
	public void setup() throws IOException {
		props = loadProperties(resourceId);
		Map<String, Object> configs = ServiceConfigBuilder.newInstance()
		      .putValue(Constants.CONFIG_SERVICE_NAME_KEY, resourceId).build();
		localConfigService = ConfigServiceFactory.getConfigService(Constants.CONFIG_MANAGER_TYPE_LOCAL, configs);
	}

	@Test
	public void testGetKey() {
		Assert.assertEquals("3", localConfigService.getProperty("zebra_ut." + getKey(Constants.ELEMENT_RETRY_TIMES)));
	}

	@Test
	public void testChangeKeyAndGet() throws IOException, InterruptedException {
		TimeUnit.SECONDS.sleep(3);
		Properties prop = loadProperties(resourceId);
		saveProperties(resourceId, prop);

		// System.in.read();
		TimeUnit.SECONDS.sleep(5);

	}

	private String getKey(String key) {
		return resourceId + "." + key;
	}

	@After
	public void recover() throws IOException {
		saveProperties(resourceId, props);
	}

	private Properties loadProperties(String resourceId) throws IOException {
		String resourceFileName = resourceId + ".properties";
		Properties prop = new Properties();
		InputStream inputStream = null;
		try {
			inputStream = getClass().getClassLoader().getResourceAsStream(resourceFileName);
			prop.load(inputStream);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}

		return prop;
	}

	private void saveProperties(String resourceId, Properties properties) throws IOException {
		URL url = getClass().getClassLoader().getResource(resourceId + ".properties");

		FileWriter writer = null;
		try {
			writer = new FileWriter(url.getPath());
			properties.store(writer, "New Properties");
		} finally {
			try {
				writer.close();
			} catch (IOException ignore) {
			}
		}
	}
}
