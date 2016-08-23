/**
 * Project: ${zebra-client.aid}
 * 
 * File Created at 2011-7-6
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.zebra.shard.jdbc.base;

import org.junit.After;
import org.junit.Before;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Leo Liang
 * 
 */
public abstract class MultiDBBaseTestCase {

	private List<CreateTableScriptEntry> createdTableList = new ArrayList<CreateTableScriptEntry>();
	
	protected static ApplicationContext context;

	protected abstract String getDBBaseUrl();

	protected abstract String getCreateScriptConfigFile();

	protected abstract String getDataFile();

	protected abstract String[] getSpringConfigLocations();

	protected String getDriverClassName() {
		return "org.h2.Driver";
	}

	@Before
	public void setUp() throws Exception {
		parseCreateScriptConfigFile();
		createTables();
		List<DBDataEntry> datas = parseDataFile();
		loadDatas(datas);
		initSpringContext();
	}

	protected void initSpringContext() throws Exception {
		if (context == null) {
			context = new ClassPathXmlApplicationContext(getSpringConfigLocations());
		}
	}

	@After
	public void tearDown() throws Exception {
		Class.forName(getDriverClassName());
		for (CreateTableScriptEntry entry : createdTableList) {
			Connection conn = null;
			Statement stmt = null;
			try {
				conn = DriverManager.getConnection(getDBBaseUrl() + entry.getDbName() + ";MVCC=TRUE;DB_CLOSE_DELAY=-1");
				stmt = conn.createStatement();
				int count = 0;
				for (Map.Entry<String, String> table : entry.getTableNameScriptMapping().entrySet()) {
					stmt.addBatch(" drop table " + table.getKey());
					count++;
				}

				if (count > 0) {
					stmt.executeBatch();
					stmt.clearBatch();
				}
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (Exception e) {

					}
				}
				if (conn != null) {
					try {
						conn.close();
					} catch (Exception e) {

					}
				}
			}
		}

	}

	private List<DBDataEntry> parseDataFile() throws Exception {
		List<DBDataEntry> datas = new ArrayList<DBDataEntry>();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document configDoc = builder
				.parse(MultiDBBaseTestCase.class.getClassLoader().getResourceAsStream(getDataFile()));
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		NodeList databaseList = (NodeList) xpath.compile("/dataset/database").evaluate(configDoc,
				XPathConstants.NODESET);
		for (int i = 0; i < databaseList.getLength(); i++) {
			DBDataEntry entry = new DBDataEntry();
			Element ele = (Element) databaseList.item(i);
			entry.setDbName(ele.getAttribute("name"));
			NodeList scriptNodeList = ele.getChildNodes();
			List<String> scripts = new ArrayList<String>();
			for (int j = 0; j < scriptNodeList.getLength(); j++) {
				scripts.add(scriptNodeList.item(j).getTextContent());
			}
			entry.setScripts(scripts);
			datas.add(entry);
		}

		return datas;

	}

	private void loadDatas(List<DBDataEntry> datas) throws Exception {
		Class.forName(getDriverClassName());
		for (DBDataEntry entry : datas) {
			Connection conn = null;
			Statement stmt = null;
			try {
				conn = DriverManager.getConnection(getDBBaseUrl() + entry.getDbName() + ";MVCC=TRUE;DB_CLOSE_DELAY=-1");
				stmt = conn.createStatement();
				int count = 0;
				for (String script : entry.getScripts()) {
					stmt.addBatch(script);
					count++;
				}

				if (count > 0) {
					stmt.executeBatch();
					stmt.clearBatch();
				}
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (Exception e) {

					}
				}
				if (conn != null) {
					try {
						conn.close();
					} catch (Exception e) {

					}
				}
			}
		}

	}

	private void createTables() throws Exception {
		Class.forName(getDriverClassName());
		for (CreateTableScriptEntry entry : createdTableList) {
			Connection conn = null;
			Statement stmt = null;
			try {
				conn = DriverManager.getConnection(getDBBaseUrl() + entry.getDbName() + ";MVCC=TRUE;DB_CLOSE_DELAY=-1");
				stmt = conn.createStatement();
				int count = 0;
				for (Map.Entry<String, String> table : entry.getTableNameScriptMapping().entrySet()) {
					stmt.addBatch(table.getValue());
					count++;
				}

				if (count > 0) {
					stmt.executeBatch();
					stmt.clearBatch();
				}
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (Exception e) {

					}
				}
				if (conn != null) {
					try {
						conn.close();
					} catch (Exception e) {

					}
				}
			}
		}
	}

	private void parseCreateScriptConfigFile() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document configDoc = builder
				.parse(MultiDBBaseTestCase.class.getClassLoader().getResourceAsStream(getCreateScriptConfigFile()));
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		NodeList databaseList = (NodeList) xpath.compile("/databases/database").evaluate(configDoc,
				XPathConstants.NODESET);
		for (int i = 0; i < databaseList.getLength(); i++) {
			CreateTableScriptEntry entry = new CreateTableScriptEntry();
			Element ele = (Element) databaseList.item(i);
			entry.setDbName(ele.getAttribute("name"));
			NodeList tableList = (NodeList) xpath.compile("tables/table").evaluate(ele, XPathConstants.NODESET);
			Map<String, String> map = new HashMap<String, String>();
			for (int j = 0; j < tableList.getLength(); j++) {
				Element tableEle = (Element) tableList.item(j);
				map.put(tableEle.getAttribute("name"), tableEle.getTextContent());
			}
			entry.setTableNameScriptMapping(map);
			createdTableList.add(entry);
		}
	}

	private static class CreateTableScriptEntry {
		private String dbName;
		private Map<String, String> tableNameScriptMapping;

		/**
		 * @return the dbName
		 */
		public String getDbName() {
			return dbName;
		}

		/**
		 * @param dbName
		 *            the dbName to set
		 */
		public void setDbName(String dbName) {
			this.dbName = dbName;
		}

		/**
		 * @return the tableNameScriptMapping
		 */
		public Map<String, String> getTableNameScriptMapping() {
			return tableNameScriptMapping;
		}

		/**
		 * @param tableNameScriptMapping
		 *            the tableNameScriptMapping to set
		 */
		public void setTableNameScriptMapping(Map<String, String> tableNameScriptMapping) {
			this.tableNameScriptMapping = tableNameScriptMapping;
		}

	}

	private static class DBDataEntry {
		private String dbName;
		private List<String> scripts;

		/**
		 * @return the dbName
		 */
		public String getDbName() {
			return dbName;
		}

		/**
		 * @param dbName
		 *            the dbName to set
		 */
		public void setDbName(String dbName) {
			this.dbName = dbName;
		}

		/**
		 * @return the scripts
		 */
		public List<String> getScripts() {
			return scripts;
		}

		/**
		 * @param scripts
		 *            the scripts to set
		 */
		public void setScripts(List<String> scripts) {
			this.scripts = scripts;
		}

	}
}
