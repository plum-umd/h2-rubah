/*
 * Copyright 2004-2008 H2 Group. Licensed under the H2 License, Version 1.0
 * (license2)
 * Initial Developer: H2 Group
 */
package org.h2.test.synth.thread;

import java.sql.SQLException;
import java.util.Random;

import org.h2.test.TestBase;

/**
 * The is an abstract operation for {@link TestMulti}.
 */
abstract class TestMultiThread extends Thread {

    TestMulti base;
    Random random = new Random();

    TestMultiThread(TestMulti base) throws SQLException {
        this.base = base;
    }

    abstract void first() throws SQLException;

    abstract void operation() throws SQLException;

    abstract void begin() throws SQLException;

    abstract void end() throws SQLException;

    abstract void finalTest() throws Exception;

    public void run() {
        try {
            while (!base.stop) {
                operation();
            }
            end();
        } catch (Throwable e) {
            TestBase.logError("error", e);
        }
    }

}