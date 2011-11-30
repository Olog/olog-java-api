package edu.msu.nscl.olog.api;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.msu.nscl.olog.api.OlogClientImpl.OlogClientBuilder;

import static edu.msu.nscl.olog.api.LogBuilder.*;
import static edu.msu.nscl.olog.api.TagBuilder.*;
import static edu.msu.nscl.olog.api.LogbookBuilder.*;

/**
 * This case consists of tests for operations which use update.
 * 
 * @author shroffk
 * 
 */
public class UpdateIT {

	private static OlogClient client;

	private static TagBuilder defaultTag = tag("defaultTag");
	private static LogbookBuilder defaultLogbook = logbook("defaultLogbook")
			.owner("me");

	@BeforeClass
	public static void beforeClass() {
		client = OlogClientBuilder.serviceURL().withHTTPAuthentication(true)
				.create();
		client.set(defaultLogbook);
		client.set(defaultTag);
	}

	@AfterClass
	public static void afterClass() {
		client.deleteLogbook(defaultLogbook.build().getName());
		client.deleteTag(defaultTag.build().getName());
		client.delete(client.findLogsBySearch("testlog*"));
	}

	/**
	 * update a single log
	 */
	@Test
	public void updateLog() {
		Log log1 = null;
		try {
			log1 = client.set(log("log1").description("some details")
					.level("Info").in(defaultLogbook));
			client.update(log(log1).with(defaultTag));
			assertTrue(
					"failed to update log with tag",
					client.findLogsBySearch("log1").iterator().next()
							.getTag(defaultTag.build().getName()) != null);
		} finally {
			if (log1 != null) {
				client.delete(log1.getId());
			}
		}
	}

	@Test
	public void updateLogs() {

	}

	@Test
	public void updateTag() {

	}

	@Test
	public void updateTag2Log() {
		Collection<Log> queryResult;
		Log testLog = null;
		try {
			queryResult = client.findLogsByTag(defaultTag.build().getName());
			testLog = client.set(log("log2").description("some details")
					.level("Info").in(defaultLogbook));
			assertTrue("Tag already present on log2",
					!queryResult.contains(testLog));
			client.update(defaultTag, testLog.getId());
			queryResult = client.findLogsByTag(defaultTag.build().getName());
			assertTrue("Failed to update log2 with tag", LogUtil
					.getLogSubjects(queryResult).contains(testLog.getSubject()));
		} finally {
			client.delete(client.findLogsBySearch("log2"));
		}
	}

	@Test
	public void updateTag2Logs() {
		TagBuilder newTag = tag("testTag");
		try {
			client.set(newTag);
			assertTrue("failed to create a empty new tag", client
					.findLogsByTag(newTag.build().getName()).size() == 0);
			Collection<Log> queryResult = client.findLogsBySearch("log*");
			client.update(newTag, LogUtil.getLogIds(queryResult));
			assertTrue(
					"Failed to add tag to logs",
					client.findLogsByTag(newTag.build().getName()).contains(
							queryResult));
		} finally {
			client.deleteTag(newTag.build().getName());
		}

	}

	@Test
	public void updateLogbook() {

	}

	@Test
	public void updateLogbook2Log() {
		LogbookBuilder logbook = logbook("testLogbook1").owner("me");
		Log testLog1 = null;
		Log testLog2 = null;
		try {
			// create a logbook with no logs
			client.set(logbook);
			// create test logs
			testLog1 = client.set(log("testLog1_updateLogbook2Log")
					.description("test log").in(defaultLogbook).level("Info"));
			testLog2 = client.set(log("testLog2_updateLogbook2Log")
					.description("test log").in(defaultLogbook).level("Info"));
			assertTrue("failed to create an empty logbook", client
					.findLogsByLogbook(logbook.build().getName()).size() == 0);
			// add testLog1 to logbook
			client.update(logbook, testLog1.getId());
			// check if the log was updated with the logbook
			assertTrue(
					"failed to update testLog1 with testlogbook1",
					checkEqualityWithoutID(
							client.findLogsByLogbook(logbook.build().getName()),
							testLog1));
			// add testLog2 to logbook
			client.update(logbook, testLog2.getId());
			// check if the testLog2 was updated with the logbook
			assertTrue(
					"failed to update testLog2 with testLogbook1",
					checkEqualityWithoutID(
							client.findLogsByLogbook(logbook.build().getName()),
							testLog2));
			// check if testLog1 was not affected by the update
			assertTrue(
					"failed to update testLog1 with testlogbook1",
					checkEqualityWithoutID(
							client.findLogsByLogbook(logbook.build().getName()),
							testLog1));
		} catch (Exception e) {
			fail(e.getMessage());
		} finally {
			client.deleteLogbook(logbook.build().getName());
			if (testLog1 != null)
				client.delete(testLog1.getId());
			if (testLog2 != null)
				client.delete(testLog2.getId());
		}
	}

	@Test
	public void updateLogbook2Logs() {
		LogbookBuilder logbook = logbook("testLogbook2").owner("me");
		Log testLog1 = null;
		Log testLog2 = null;
		try {
			// create a logbook with no logs
			client.set(logbook);
			assertTrue("failed to create an empty logbook", client
					.findLogsByLogbook(logbook.build().getName()).size() == 0);
			// create test logs
			testLog1 = client.set(log("testLog1_updateLogbook2Logs")
					.description("test log").in(defaultLogbook).level("Info"));
			testLog2 = client.set(log("testLog2_updateLogbook2Logs")
					.description("test log").in(defaultLogbook).level("Info"));
			// add testLog1 & testLog2 to logbook
			Collection<Log> logs = new ArrayList<Log>();
			logs.add(testLog1);
			logs.add(testLog2);
			client.update(logbook, LogUtil.getLogIds(logs));
			// check if the logs were added to the logbook
			assertTrue(
					"failed to update a group of logs(testLog1, testLog2) with testLogbook2",
					checkEqualityWithoutID(
							client.findLogsByLogbook(logbook.build().getName()),
							logs));
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			client.deleteLogbook(logbook.build().getName());
			if (testLog1 != null)
				client.delete(testLog1.getId());
			if (testLog2 != null)
				client.delete(testLog2.getId());
		}
	}

	/**
	 * This seems like an incorrect equality test but don't know how to test if
	 * the log I am sending has indeed been set/added since I don't have the id
	 * in the builder
	 * 
	 * @param returnedLogs
	 * @param setLogs
	 * @return
	 */

	private static boolean checkEqualityWithoutID(Collection<Log> returnedLogs,
			Collection<Log> setLogs) {
		Collection<String> logSubjects = LogUtil.getLogSubjects(returnedLogs);
		for (Log log : setLogs) {
			if (!logSubjects.contains(log.getSubject()))
				return false;
		}
		return true;
	}

	/**
	 * This seems like an incorrect equality test but don't know how to test if
	 * the log I am sending has indeed been set/added since I don't have the id
	 * in the builder
	 * 
	 * @param returnedLogs
	 * @param setLogs
	 * @return
	 */
	private static boolean checkEqualityWithoutID(Collection<Log> returnedLogs,
			LogBuilder setLog) {
		return checkEqualityWithoutID(returnedLogs, setLog.build());
	}

	private static boolean checkEqualityWithoutID(Collection<Log> returnedLogs,
			Log setLog) {
		Collection<String> logSubjects = LogUtil.getLogSubjects(returnedLogs);
		if (!logSubjects.contains(setLog.getSubject()))
			return false;
		return true;
	}

}
