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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Dozer @ 2015-02
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class JDBCUtils {
    // ERROR 1290 (HY000): The MySQL server is running with the --read-only option so it cannot execute this statement
    private final static int READ_ONLY_ERROR_CODE = 1290;

    private final static String READ_ONLY_ERROR_MESSAGE = "read-only";

    public static void throwSQLExceptionIfNeeded(List<SQLException> exceptions) throws SQLException {
        if (exceptions != null && !exceptions.isEmpty()) {
            StringWriter buffer = new StringWriter();
            PrintWriter out = null;
            try {
                out = new PrintWriter(buffer);

                for (SQLException exception : exceptions) {
                    exception.printStackTrace(out);
                }
            } finally {
                if (out != null) {
                    out.close();
                }
            }

            throw new SQLException(buffer.toString());
        }
    }

    public static void throwWrappedSQLException(SQLException e) throws SQLException {
        if (isReadOnlyException(e)) {
            throw new SQLException("Write dataSource is currently in the maintaining stage. ", e);
        } else {
            throw e;
        }
    }

    public static boolean isReadOnlyException(SQLException e) {
        return e.getErrorCode() == READ_ONLY_ERROR_CODE && e.getMessage().contains(READ_ONLY_ERROR_MESSAGE);
    }

    public static void closeAll(Statement statement, Connection connection) {
        closeAll(null, statement, connection);
    }

    public static void closeAll(ResultSet resultSet, Statement statement, Connection connection) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException ignore) {
            }
        }

        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ignore) {
            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignore) {
            }
        }
    }
}
