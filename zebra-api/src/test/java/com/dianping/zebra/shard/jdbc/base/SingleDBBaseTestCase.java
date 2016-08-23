/**
 * Project: zebra-client
 * 
 * File Created at 2011-6-29
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.dbunit.DBTestCase;
import org.dbunit.DatabaseUnitException;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.statement.IBatchStatement;
import org.dbunit.database.statement.IStatementFactory;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.operation.CompositeOperation;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Leo Liang
 * 
 */
public abstract class SingleDBBaseTestCase extends DBTestCase {

	private List<CreateTableScriptEntry> createdTableList = new ArrayList<CreateTableScriptEntry>();
	
	protected ApplicationContext context;

	protected abstract String getDataSetFilePath();

	protected abstract String getCreateTableScriptPath();

	protected abstract String getDBUrl();

	protected abstract String[] getSpringConfigLocations();

	protected String getDriverName() {
		return "org.h2.Driver";
	}

	public SingleDBBaseTestCase() {
		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, getDriverName());
		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, getDBUrl());
	}

	@Override
	protected void setUpDatabaseConfig(DatabaseConfig config) {
		config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
	}

	protected IDataSet getDataSet() throws Exception {
		return new FlatXmlDataSetBuilder()
				.build(SingleDBBaseTestCase.class.getClassLoader().getResourceAsStream(getDataSetFilePath()));
	}

	protected DatabaseOperation getSetUpOperation() throws Exception {
		parseCreateTableScriptFile();

		initSpringContext();

		return new CompositeOperation(new DatabaseOperation[] { new DatabaseOperation() {

			@Override
			public void execute(IDatabaseConnection connection, IDataSet dataSet)
					throws DatabaseUnitException, SQLException {

				DatabaseConfig databaseConfig = connection.getConfig();
				IStatementFactory statementFactory = (IStatementFactory) databaseConfig
						.getProperty(DatabaseConfig.PROPERTY_STATEMENT_FACTORY);
				IBatchStatement statement = statementFactory.createBatchStatement(connection);
				try {
					int count = 0;
					for (CreateTableScriptEntry entry : createdTableList) {
						statement.addBatch(entry.getCreateTableScript());
						count++;
					}

					if (count > 0) {
						statement.executeBatch();
						statement.clearBatch();
					}
				} finally {
					statement.close();
				}
			}
		}, DatabaseOperation.CLEAN_INSERT });
	}

	protected DatabaseOperation getTearDownOperation() throws Exception {
		return new DatabaseOperation() {

			@Override
			public void execute(IDatabaseConnection connection, IDataSet dataSet)
					throws DatabaseUnitException, SQLException {

				DatabaseConfig databaseConfig = connection.getConfig();
				IStatementFactory statementFactory = (IStatementFactory) databaseConfig
						.getProperty(DatabaseConfig.PROPERTY_STATEMENT_FACTORY);
				IBatchStatement statement = statementFactory.createBatchStatement(connection);

				try {
					int count = 0;
					for (CreateTableScriptEntry entry : createdTableList) {
						statement.addBatch("drop table " + entry.getTableName());
						count++;
					}

					if (count > 0) {
						statement.executeBatch();
						statement.clearBatch();
					}
				} finally {
					statement.close();
				}

			}
		};
	}

	protected void initSpringContext() throws Exception {
		if (context == null) {
			context = new ClassPathXmlApplicationContext(getSpringConfigLocations());
		}
	}

	protected void parseCreateTableScriptFile() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document configDoc = builder
				.parse(SingleDBBaseTestCase.class.getClassLoader().getResourceAsStream(getCreateTableScriptPath()));
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		NodeList tableScriptList = (NodeList) xpath.compile("/tables/table").evaluate(configDoc,
				XPathConstants.NODESET);
		for (int i = 0; i < tableScriptList.getLength(); i++) {
			CreateTableScriptEntry entry = new CreateTableScriptEntry();
			Element ele = (Element) tableScriptList.item(i);
			entry.setTableName(ele.getAttribute("name"));
			entry.setCreateTableScript(ele.getTextContent());
			createdTableList.add(entry);
		}
	}

	
}
