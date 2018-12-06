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

import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.exception.ZebraException;
import com.dianping.zebra.log.Logger;
import com.dianping.zebra.log.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;

public class JaxbUtils {
	private static Logger LOGGER = LoggerFactory.getLogger(JaxbUtils.class);

	public static <T> T fromXml(String xmlString, Class<T> clazz) {
		if (StringUtils.isBlank(xmlString)) {
			throw new ZebraException("xmlString won't be blank");
		}

		try {
			StringReader reader = new StringReader(xmlString);
			JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
			return (T) jaxbContext.createUnmarshaller().unmarshal(reader);
		} catch (JAXBException e) {
			LOGGER.error("convert xml to Java failed", e);
			throw new ZebraConfigException("convert xml to Java failed", e);
		}
	}

	public static <T> byte[] toXml(Class<T> clazz, T object) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
			Marshaller marshaller = jaxbContext.createMarshaller();
			// 编码格式
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			// 是否格式化生成的xml串
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			// 是否省略xml头信息
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, false);
			marshaller.marshal(object, outputStream);

			return outputStream.toByteArray();
		} catch (Exception e) {
			LOGGER.error("convert Java to xml fail!", e);
			throw new ZebraConfigException("convert xml to Java failed", e);
		}
	}
}
