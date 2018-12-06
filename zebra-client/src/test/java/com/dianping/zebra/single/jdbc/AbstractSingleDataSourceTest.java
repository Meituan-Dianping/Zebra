package com.dianping.zebra.single.jdbc;

import com.google.common.base.Joiner;
import org.junit.Assert;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public abstract class AbstractSingleDataSourceTest {

    protected abstract List<String> getNotSupportedMethod();

    private static Class<?>[] validParameterTypes = {boolean.class, Boolean.class, int.class, Integer.class,
            long.class, Long.class, float.class, Float.class, double.class, Double.class, Number.class, String.class};

    private boolean isValidParameter(Class<?> type) {
        for (Class<?> t : validParameterTypes) {
            if (t.equals(type)) {
                return true;
            }
        }

        return false;
    }

    protected void assertResult(DataSource ds, String sql, String expectedValue) throws SQLException {
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery(sql);

        if (rs.next()) {
            Assert.assertEquals(expectedValue, rs.getString(1));
        }

        if (rs != null) {
            rs.close();
        }

        if (stmt != null) {
            stmt.close();
        }

        if (conn != null) {
            conn.close();
        }
    }

    protected void assertField(Class<?> compareFrom, Class<?> compareTo, List<String> ignoreList) {
        ArrayList<String> errors = new ArrayList<String>();
        Map<String, Method> currentMethods = new HashMap<String, Method>();
        HashSet<String> allMethodNames = new HashSet<String>();

        if (ignoreList == null || ignoreList.size() == 0) {
            ignoreList = this.getNotSupportedMethod();
        }
        for (Method m : compareFrom.getMethods()) {
            currentMethods.put(m.getName(), m);
        }

        Map<String, Method> realPoolMethods = new HashMap<String, Method>();
        for (Method m : compareTo.getMethods()) {
            if (ignoreList.contains(m.getName())) {
                continue;
            }
            if (realPoolMethods.get(m.getName()) == null) {
                realPoolMethods.put(m.getName(), m);
            } else {
                if (realPoolMethods.get(m.getName()).getParameterTypes().length >= 1
                        && !isValidParameter(realPoolMethods.get(m.getName()).getParameterTypes()[0])) {
                    realPoolMethods.put(m.getName(), m);
                }
            }
            if (m.getName().startsWith("set") && !ignoreList.contains(m.getName()) && m.getParameterTypes().length == 1
                    && isValidParameter(m.getParameterTypes()[0])) {
                allMethodNames.add(m.getName().substring("set".length()));
            }
        }

        for (String name : allMethodNames) {
            name = "set" + String.valueOf(name.charAt(0)).toUpperCase() + name.substring(1);
            Method realPoolMethod = realPoolMethods.get(name);

            if (realPoolMethod == null) {
                continue;
            }

            Method currentMethod = currentMethods.get(name);

            try {
                Assert.assertNotNull(name + " not exist!", currentMethod);
                Assert.assertEquals(name + " too many argument", currentMethod.getParameterTypes().length, 1);
                if (currentMethod.getName().equals("setConnectionInitSqls")
                        || currentMethod.getName().equals("setDisconnectionSqlCodes")) {
                    Assert.assertEquals(name + " arguments type not match", currentMethod.getParameterTypes()[0],
                            String.class);
                    Assert.assertEquals(name + " arguments type not match", realPoolMethod.getParameterTypes()[0],
                            Collection.class);
                } else {
                    Assert.assertEquals(name + " arguments type not match", currentMethod.getParameterTypes()[0],
                            realPoolMethod.getParameterTypes()[0]);
                }
            } catch (AssertionError e) {
                errors.add(e.toString());
            }
        }
        Assert.assertEquals(Joiner.on("\n").join(errors), 0, errors.size());
    }

    @SuppressWarnings("unchecked")
    protected <T> void assertParameterValue(Class<T> cls, Object obj, String methodName, Object exceptValue)
            throws Exception {
        String realMethodName = "get" + String.valueOf(methodName.charAt(0)).toUpperCase() + methodName.substring(1);
        System.out.println(String.format("Check value for %s[%s]", methodName, exceptValue));
        try {
            Assert.assertEquals(exceptValue,
                    ((T) obj).getClass().getMethod(realMethodName, new Class<?>[0]).invoke(obj, new Object[0]));
        } catch (Exception e) {
            throw e;
        }
        return;
    }
}
