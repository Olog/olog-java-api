package edu.msu.nscl.olog.api;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.msu.nscl.olog.api.OlogClientImpl.OlogClientBuilder;
import static edu.msu.nscl.olog.api.LogbookBuilder.*;
import static edu.msu.nscl.olog.api.TagBuilder.*;
import static edu.msu.nscl.olog.api.LogBuilder.*;

public class ClientIT {

	private static OlogClient client;

	private static String logOwner;
	private static String logbookOwner;
	private static String tagOwner;
	private static String propertyOwner;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		client = OlogClientBuilder.serviceURL().withHTTPAuthentication(true)
				.create();
		// these should be read from some properties files so that they can be
		// setup for the corresponding intergration testing enviorment.
		logOwner = "me";
		logbookOwner = "me";
		tagOwner = "me";
		propertyOwner = "me";
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * create(set), list and delete a logbook
	 * 
	 */
	@Test
	public void logbookSimpleTest() {
		LogbookBuilder logbook = logbook("testLogBook").owner(logbookOwner);
		try {
			// set a logbook
			// list all logbook
			client.set(logbook);
			assertTrue("failed to set the testLogBook", client.listLogbooks()
					.contains(logbook.build()));
		} catch (Exception e) {
			fail(e.getCause().toString());
		} finally {
			// delete a logbook
			client.deleteLogbook(logbook.build().getName());
			assertFalse("failed to clean up the testLogbook", client
					.listLogbooks().contains(logbook.build()));
		}
	}

	/**
	 * create(set), list and delete a tag
	 */
	@Test
	public void tagsSimpleTest() {
		TagBuilder tag = tag("testTag");
		try {
			// set a tag
			// list all tag
			client.set(tag);
			assertTrue("failed to set the testTag",
					client.listTags().contains(tag.build()));
		} catch (Exception e) {
			fail(e.getCause().toString());
		} finally {
			// delete a tag
			client.deleteLogbook(tag.build().getName());
			assertFalse("failed to clean the testTag", client.listTags()
					.contains(tag.build()));
		}
	}

	/**
	 * create(set), list, delete a single log
	 */
	@Test
	public void logSimpleTest() {
		LogBuilder log = log("testLog").description("some details")
				.level("Info").id(1234L);
		try {
			// set a log
			// list all log
			client.set(log);
			assertTrue("failed to set the testLog",
					client.listLogs().contains(log.build()));
		} catch (Exception e) {
			fail(e.getCause().toString());
		} finally {
			// delete a log
			client.delete(log);
			assertFalse("failed to clean the testLog", client.listLogs()
					.contains(log.build()));
		}

	}

	/**
	 * create(set), list and delete a group of logs
	 */
	@Test
	public void logsSimpleTest() {
		LogBuilder log1 = log("testLog1").description("some details").level(
				"Info");
		LogBuilder log2 = log("testLog2").description("some details").level(
				"Info");
		Collection<LogBuilder> logs = new ArrayList<LogBuilder>();
		logs.add(log1);
		logs.add(log2);

		Collection<Log> result = null;

		try {
			// set a group of channels
			client.set(logs);
			// list all logs
			result = client.listLogs();
			assertTrue("Failed to set the group of logs.",
					result.containsAll(LogUtil.toLogs(logs)));
		} catch (Exception e) {
			fail(e.getCause().toString());
		} finally {
			// delete a group of logs
			for (Log log : result) {
				client.delete(log(log));
			}
			result = client.listLogs();
			for (Log userLog : LogUtil.toLogs(logs)) {
				assertTrue("Failed to clean up the group of test logs", client
						.listLogs().contains(userLog));
			}
		}
	}
}
