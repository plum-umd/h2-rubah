/*
 * Copyright 2004-2009 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.test.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.h2.test.TestBase;

import rubah.test.Test;

/**
 * Various test cases.
 */
public class TestBeginPrepared extends TestBase {

    /**
     * Run just this test.
     *
     * @param a ignored
     */
    public static void main(String... a) throws Exception {
        TestBase.createCaller().init().test();
    }

    @Override
	public void test() throws Exception {
        deleteDb("prepared");
        Connection conn = getConnection("prepared");
        conn.createStatement().executeUpdate("create table test (ID  int PRIMARY KEY)");
        conn.createStatement().executeUpdate("insert into test values(1)");
        PreparedStatement prepared = conn.prepareStatement("BEGIN TRANSACTION");
        Test.allowUpdates();
        prepared.execute();
        ResultSet rs = conn.createStatement().executeQuery("select ID from test");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        Test.disallowUpdates();
        prepared.close();
        conn.close();
        deleteDb("prepared");
    }
}
