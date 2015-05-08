package org.h2.test;

import java.io.File;
import java.sql.SQLException;

import org.h2.store.fs.FileSystem;
import org.h2.test.unit.SelfDestructor;
import org.h2.test.utils.OutputCatcher;

import rubah.runtime.state.Options;
import rubah.test.ClientTest;
import rubah.test.ClientTestGenerator;
import rubah.test.ServerStartedPredicate;
import rubah.test.TestOptions;
import rubah.test.TestRunner;
import rubah.tools.Updater.Type;

public class TestUpdate extends TestAll {
	@SuppressWarnings("unchecked")
	private static final Class<? extends TestBase>[] tests = new Class[]{
        // db
		// Does not generate any update opportunity in this setting
//        org.h2.test.db.TestScriptSimple.class,
//        org.h2.test.db.TestBackup.class,
//        org.h2.test.db.TestBigDb.class,
//        org.h2.test.db.TestCluster.class,
//        org.h2.test.db.TestCsv.class,
//        org.h2.test.db.TestEncryptedDb.class,
//        org.h2.test.db.TestLargeBlob.class,
//        org.h2.test.db.TestLinkedTable.class,
//        org.h2.test.db.TestListener.class,
//        org.h2.test.db.TestTwoPhaseCommit.class,
//        org.h2.test.db.TestSpeed.class,
//        org.h2.test.db.TestLogFile.class,
//        org.h2.test.db.TestMemoryUsage.class,

        // Generates update opportunities
        org.h2.test.db.TestAlter.class,
        org.h2.test.db.TestAutoRecompile.class,
        org.h2.test.db.TestBigResult.class,
        org.h2.test.db.TestCases.class,
        org.h2.test.db.TestBeginPrepared.class,
        org.h2.test.db.TestCheckpoint.class,
        org.h2.test.db.TestCompatibility.class,
        org.h2.test.db.TestDeadlock.class,
        org.h2.test.db.TestExclusive.class,
        org.h2.test.db.TestFullText.class,
        org.h2.test.db.TestFunctionOverload.class,
        org.h2.test.db.TestRights.class,
        org.h2.test.db.TestLob.class,
        org.h2.test.db.TestSQLInjection.class,
        org.h2.test.db.TestSessionsLocks.class,
        org.h2.test.rowlock.TestRowLocks.class,

        // Failing, requires more work to figure out why
//        org.h2.test.db.TestFunctions.class,
//        org.h2.test.db.TestIndex.class,
//        org.h2.test.db.TestMultiConn.class,
//        org.h2.test.db.TestMultiThread.class,
//        TestScript.class,
        // Not checked yet
//        org.h2.test.db.TestMultiDimension.class,
//        org.h2.test.db.TestMultiThreadedKernel.class,
//        org.h2.test.db.TestOpenClose.class,
//        org.h2.test.db.TestOptimizations.class,
//        org.h2.test.db.TestOutOfMemory.class,
//        org.h2.test.db.TestPowerOff.class,
//        org.h2.test.db.TestReadOnly.class,
//        org.h2.test.db.TestRunscript.class,
//        org.h2.test.db.TestSequence.class,
//        org.h2.test.db.TestSpaceReuse.class,
//        org.h2.test.db.TestTempTables.class,
//        org.h2.test.db.TestTransaction.class,
//        org.h2.test.db.TestTriggersConstraints.class,
//        org.h2.test.db.TestView.class,

        // jaqu
//        AliasMapTest.class,
//        SamplesTest.class,
//        UpdateTest.class,

        // jdbc
//        TestBatchUpdates.class,
//        TestCallableStatement.class,
//        TestCancel.class,
//        TestDatabaseEventListener.class,
//        TestDriver.class,
//        TestManyJdbcObjects.class,
//        TestMetaData.class,
//        TestNativeSQL.class,
//        TestPreparedStatement.class,
//        TestResultSet.class,
//        TestStatement.class,
//        TestTransactionIsolation.class,
//        TestUpdatableResultSet.class,
//        TestZloty.class,

        // jdbcx
//        TestConnectionPool.class,
//        TestDataSource.class,
//        TestXA.class,
//        TestXASimple.class,

        // server
//        TestAutoServer.class,
//        TestNestedLoop.class,
//        TestWeb.class,

        // mvcc & row level locking
//        TestMvcc1.class,
//        TestMvcc2.class,
//        TestMvcc3.class,
//        TestMvccMultiThreaded.class,

        // synth
//        TestBtreeIndex.class,
//        TestCrashAPI.class,
//        TestFuzzOptimizations.class,
//        TestRandomSQL.class,
//        TestKillRestart.class,
//        TestKillRestartMulti.class,
//        TestMultiThreaded.class,
	};

    /**
     * Run all tests.
     *
     * @param args the command line arguments
     */
    public static void main(String... args) throws Exception {
        OutputCatcher catcher = OutputCatcher.start();
        run(args);
        catcher.stop();
        catcher.writeTo("Test Output", "docs/html/testOutput.html");
    }

    private static void run(String... args) throws Exception {
        SelfDestructor.startCountdown(6 * 60);
        long time = System.currentTimeMillis();
        TestUpdate test = new TestUpdate();
        System.setProperty("h2.maxMemoryRowsDistinct", "128");
        System.setProperty("h2.check2", "true");

        test.memory = true;
        test.pageStore = false;
        test.runTests();
//        TestPerformance.main("-init", "-db", "1");
        System.out.println(TestBase.formatTime(System.currentTimeMillis() - time) + " total");
    }

    private static final String JVM 		= "/data/jdk/jdk1.7.0_75/bin/java";
    private static final String JVM_OPT 	= "-Xmx4G -XX:-UseSplitVerifier -DprintConversionsToFile=conversions.txt";
    private static final String RUBAH 		= "/data/repos/rubah/rubah/";
    private static final String BOOT_CP 	= "-Xbootclasspath/p:/data/repos/rubah/bench/run/bootstrap7.jar:" + RUBAH + "/target/rubah-jar-with-dependencies.jar";
    private static final String H2	 		= "/data/repos/rubah/h2/";
    private static final String CP 			= "-cp " + H2 + "/h2/bin/h2-1.2.121.jar:" + H2 + "/h2/bin-test";
    private static final String RUBAH_ARGS	= H2 + "/h2/bin/h2-1.2.121.jar " + H2 + "/h2/bin/h2-1.2.121.desc org.h2.tools.Server -tcp -trace";

    private static final String CMD 	= JVM + " " + JVM_OPT + " " + BOOT_CP + " " + CP +  " rubah.Rubah " + RUBAH_ARGS ;

    private static final File logFile = new File("server.log");

    /**
     * Run the tests with a number of different settings.
     */
    private void runTests() throws SQLException {
        smallLog = big = ssl = false;
        diskResult = deleteIndex = traceSystemOut = diskUndo = false;
        traceTest = stopOnError = false;
        traceLevelFile = throttle = 0;
        logMode = 1;
        cipher = null;
        mvcc = false;
        memory = true;
        networked = true;

        TestOptions options = new TestOptions()
        		.setClientTestGenerator(new TestGenerator(tests, this))
//        		.usnsetStartServer()
        		.setStartServer(
        				CMD,
        				new PingH2Server(this),
        				logFile)
        		.setRefreshServer(false)
        		.setUpdate(Type.v0v0, new Options().setStopAndGo(true))
//        		.setUpdate(
//        				Type.v0v1,
//        				new Options()
//        					.setJar(new File(H2 + "/h2/bin/h2-1.2.122.jar"))
//        					.setMigrationStrategy(new FullyLazyMonolithic())
//        					.setUpdateClass(new JarUpdateClass("UpdateClass_1_2_121_to_1_2_122"))
//        					.setUpdateDescriptor(new File(H2 + "/h2/bin/h2-1.2.122.desc"))
//        					.setLazy(false)
//        				.setStopAndGo(false))
        		.setFirstUpdate(0)
        		;

        new TestRunner(options).runTests();

    }


    public void runTest(TestBase test) throws Throwable {
    }

    private static class TestGenerator implements ClientTestGenerator {
    	private final Class<? extends TestBase>[] tests;
    	private final TestAll conf;
    	private int current = -1;



		public TestGenerator(Class<? extends TestBase>[] tests, TestAll conf) {
			this.tests = tests;
			this.conf = conf;
		}

		@Override
		public ClientTest getCurrent() {
			if (this.current < 0 || this.current >= tests.length)
				throw new IllegalStateException();

			try {
				return new SingleTest(tests[current].newInstance(), conf);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new Error(e);
			}
		}

		@Override
		public boolean hasNext() {
			this.current++;

			return this.current < tests.length;
		}
    }

    private static class SingleTest implements ClientTest {
    	private final TestBase test;
    	private final TestAll  conf;

		public SingleTest(TestBase test, TestAll conf) {
			this.test = test;
			this.conf = conf;
		}

		@Override
		public void run() throws Throwable {
            try {
            	this.test.init(this.conf);
            	this.test.start = System.currentTimeMillis();
            	this.test.test();
            } finally {
            	FileSystem.getInstance("memFS:").deleteRecursive("memFS:", false);
            	FileSystem.getInstance("memLZF:").deleteRecursive("memLZF:", false);
            }
		}

		@Override
		public int timeout() {
			return 60 * 1000;
		}

		@Override
		public int firstUpdate() {
			return 0;
		}

		@Override
		public String toString() {
			return this.test.getClass().getName();
		}
    }

    private static class PingH2Server implements ServerStartedPredicate {

    	private final TestAll conf;

    	public PingH2Server(TestAll conf) {
			this.conf = conf;
		}

		private TestBase test = new TestBase() {
			@Override
			public void test() throws Exception {
				// Empty
			}
		};

		@Override
		public boolean hasServerStarted() {
			try {
				this.test.init(conf);
				this.test.getConnection("ping").close();;
			} catch (Exception e) {
				return false;
			}

			return true;
		}

		@Override
		public int timeout() {
			return 60 * 1000;
		}

    }

}
