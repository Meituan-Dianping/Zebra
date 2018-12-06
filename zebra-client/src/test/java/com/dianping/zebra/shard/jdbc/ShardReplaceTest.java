package com.dianping.zebra.shard.jdbc;

import com.dianping.zebra.shard.jdbc.base.MultiDBBaseTestCase;
import org.junit.Ignore;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by wxl on 17/5/24.
 */

public class ShardReplaceTest extends MultiDBBaseTestCase {
    @Override
    protected String getDBBaseUrl() {
        return "jdbc:h2:mem:";
    }

    @Override
    protected String getCreateScriptConfigFile() {
        return "mockdb-config/testReplace/createtable-multidb-replacetest.xml";
    }

    @Override
    protected String getDataFile() {
        return "mockdb-config/testReplace/data-multidb-replacetest.xml";
    }

    @Override
    protected String[] getSpringConfigLocations() {
        return new String[] {"mockdb-config/testReplace/ctx-multidb-replacetest.xml"};
    }

    public DataSource getDataSource() {
        return (DataSource) context.getBean("zebraDS");
    }





    @Test
    @Ignore
    public void testReplace() throws SQLException {
        ShardDataSource ds = new ShardDataSource();
        ds.setRuleName("zebratestencryptionc");
        ds.setParallelExecuteTimeOut(100000);
        ds.init();

        Connection conn = ds.getConnection();

        Statement st = conn.createStatement();
        st.execute("replace into `MonthTable` (`Uid`,`Name`) VALUES (4, 'replace test-1')", Statement.RETURN_GENERATED_KEYS);
        st.execute("replace into `MonthTable` (`Uid`,`Name`) VALUES (4, 'replace test-1') (8, 'replace test-2')", Statement.RETURN_GENERATED_KEYS);
        st.execute("replace into `MonthTable` (`Uid`,`Name`) SELECT `Uid`,`Name` FROM MonthTable WHERE `Uid` = 8", Statement.RETURN_GENERATED_KEYS);
        st.execute("replace into `MonthTable` set `Uid` = 17, `Name` = 'xxxx'", Statement.RETURN_GENERATED_KEYS);
        getGenerateKeys(st);
        st.close();

        conn.close();
        ds.close();
    }

    private void getGenerateKeys(Statement st) throws SQLException {
        ResultSet rs = st.getGeneratedKeys();
        if(rs != null) {
            while (rs.next()) {
                System.out.println("####################--GK: "+rs.getInt(1));
            }
        }
        if(rs != null) {
            rs.close();
        }
    }
}
