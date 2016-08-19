package com.dianping.zebra.util;

import com.google.common.collect.Lists;

import groovy.sql.GroovyRowResult;
import groovy.sql.Sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Dozer @ 2015-02
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public final class SqlExecuteHelper {
    private SqlExecuteHelper() {
    }

    public static int executeInsert(Connection conn, String sql) throws SQLException {
        System.out.println(sql);
        Sql client = new Sql(conn);
        Integer result = (Integer) client.executeInsert(sql).get(0).get(0);
        conn.close();
        return result;
    }

    public static int executeUpdate(Connection conn, String sql) throws SQLException {
        System.out.println(sql);
        Sql client = new Sql(conn);
        int result = client.executeUpdate(sql);
        conn.close();
        return result;
    }

    @SuppressWarnings("unchecked")
   public static List<List<Object>> executeQuery(Connection conn, String sql) throws SQLException {
        System.out.println(sql);
        Sql client = new Sql(conn);
        List<GroovyRowResult> rows = client.rows(sql);

        List<List<Object>> result = new ArrayList<List<Object>>();

        for (GroovyRowResult row : rows) {
            result.add(Lists.newArrayList(row.values()));
        }
        conn.close();
        return result;
    }
}
