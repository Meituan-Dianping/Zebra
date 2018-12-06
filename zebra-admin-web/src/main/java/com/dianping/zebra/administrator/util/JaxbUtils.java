package com.dianping.zebra.administrator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;

/**
 * Created by taochen on 2018/11/10.
 */
public class JaxbUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(JaxbUtils.class);

    //xml to java
    public static <T> T jaxbReadXml(Class<T> clazz, byte[] data) {
        InputStream inputStream = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            inputStream = new ByteArrayInputStream(data);
            return (T) unmarshaller.unmarshal(inputStream);
        } catch (Exception e) {
            LOGGER.error("xml to java fail!", e);
        }
        return null;
    }

    //java to xml
    public static <T> byte[] jaxbWriteXml(Class<T> clazz, T object) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Marshaller marshaller = jaxbContext.createMarshaller();
            //编码格式
            marshaller.setProperty(Marshaller.JAXB_ENCODING,"UTF-8");
            //是否格式化生成的xml串
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            //是否省略xml头信息
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, false);

            marshaller.marshal(object, byteArrayOutputStream);

            byte[] data = byteArrayOutputStream.toByteArray();

            return data;
        } catch (Exception e) {
            LOGGER.error("java to xml fail!", e);
        }
        return null;
    }
}
