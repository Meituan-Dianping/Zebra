package com.dianping.zebra.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public final class FileUtils {

	public static File toFile(URL url) {
		if (url == null || !url.getProtocol().equals("file")) {
			return null;
		} else {
			String filename = url.getFile().replace('/', File.separatorChar);
			int pos = 0;
			while ((pos = filename.indexOf('%', pos)) >= 0) {
				if (pos + 2 < filename.length()) {
					String hexStr = filename.substring(pos + 1, pos + 3);
					char ch = (char) Integer.parseInt(hexStr, 16);
					filename = filename.substring(0, pos) + ch + filename.substring(pos + 3);
				}
			}
			return new File(filename);
		}
	}

	public static void closeQuietly(InputStream input) {
		try {
			if (input != null) {
				input.close();
			}
		} catch (IOException ioe) {
			// ignore
		}
	}
}
