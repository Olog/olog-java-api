package edu.msu.nscl.olog.api;

import static edu.msu.nscl.olog.api.LogBuilder.log;
import static edu.msu.nscl.olog.api.LogbookBuilder.logbook;
import static edu.msu.nscl.olog.api.TagBuilder.tag;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import edu.msu.nscl.olog.api.OlogClientImpl.OlogClientBuilder;

//if (key.equals("search")) {
//    log_matches.addAll(match.getValue());
////    JcrSearch js = new JcrSearch();
////    jcr_search_ids = js.searchForIds(match.getValue().toString());
//} else if (key.equals("tag")) {
//    addTagMatches(match.getValue());
//} else if (key.equals("logbook")) {
//    addLogbookMatches(match.getValue());
//} else if (key.equals("page")) {
//    logPaginate_matches.putAll(key, match.getValue());
//} else if (key.equals("limit")) {
//    logPaginate_matches.putAll(key, match.getValue());
//} else if (key.equals("start")) {
//    date_matches.putAll(key, match.getValue());
//} else if (key.equals("end")) {
//    date_matches.putAll(key, match.getValue());
//} else {
//    value_matches.putAll(key, match.getValue());
//}

public class QueryIT {

	private static int initialLogCount;

	private static OlogClient client;
	// Logs
	static private Log pvk_01;
	static private Log pvk_02;
	static private Log pvk_03;
	static private Log distinctName;

	// Tags
	static TagBuilder tagA = tag("Taga", "me");
	static TagBuilder tagB = tag("Tagb", "me");
	static TagBuilder tagC = tag("Tagc", "me");
	static TagBuilder tagStar = tag("Tag*", "me");
	// Logbooks
	static LogbookBuilder book = logbook("book");
	static LogbookBuilder book2 = logbook("book2");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		client = OlogClientBuilder.serviceURL().withHTTPAuthentication(true)
				.create();

		initialLogCount = client.listLogs().size();
		// Add the tags and logbooks.
		client.set(book.owner("me"));
		client.set(book2.owner("me"));
		client.set(tagA);
		client.set(tagB);
		client.set(tagC);
		client.set(tagStar);
		pvk_01 = client.set(log()
				.description("pvk:01<first> " + "first details").level("Info")
				.appendToLogbook(book).appendToLogbook(book2).appendTag(tagA));
		pvk_02 = client.set(log()
				.description("pvk:02<second> " + "second details")
				.level("Info").appendToLogbook(book).appendTag(tagA)
				.appendTag(tagB));
		pvk_03 = client.set(log()
				.description("pvk:03<second> " + "some details").level("Info")
				.appendToLogbook(book).appendTag(tagB).appendTag(tagC));
		distinctName = client.set(log()
				.description("distinctName: " + "some details").level("Info")
				.appendToLogbook(book).appendTag(tagStar));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		client.delete(pvk_01.getId());
		client.delete(pvk_02.getId());
		client.delete(pvk_03.getId());
		client.delete(distinctName.getId());
		// clean up all the tags and logbooks
		client.deleteLogbook(book.build().getName());
		client.deleteLogbook(book2.toXml().getName());
		client.deleteTag(tagA.toXml().getName());
		client.deleteTag(tagB.toXml().getName());
		client.deleteTag(tagC.toXml().getName());
		client.deleteTag(tagStar.toXml().getName());

		assertTrue(client.listLogs().size() == initialLogCount);
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * check if all logs are returned
	 */
	@Test
	public void queryAllLogs() {
		Map<String, String> map = new Hashtable<String, String>();
		map.put("search", "*");
		Collection<Log> logs = client.findLogs(map);
		assertTrue(client.listLogs().size() == logs.size());
	}

	@Test
	public void querySingleLog() {
		assertTrue("failed to search using the logId",
				client.findLogById(pvk_01.getId()).equals(pvk_01));
	}

	/**
	 * Test searching based on
	 */
	@Test
	public void queryLogs() {
		Map<String, String> map = new Hashtable<String, String>();
		map.put("search", "pvk:*");
		Collection<Log> logs = client.findLogs(map);
		assertTrue(
				"Failed to get search based on subject pattern, expected 3 found "
						+ logs.size(), logs.size() == 3);
	}

	/**
	 * Test logbook query When multiple logbooks are queried, the result is a
	 * logical OR of all the query conditions
	 */
	@Test
	public void queryLogsbyLogbook() {
		MultivaluedMap<String, String> map = new MultivaluedMapImpl();
		map.add("logbook", "book2");
		Collection<Log> logs = client.findLogs(map);
		assertTrue("search for all logs in logbook book2 failed ",
				logs.size() == 1);
		// Query for logs from multiple logbooks.
		map.add("logbook", "book");
		map.add("logbook", "book2");
		logs = client.findLogs(map);
		assertTrue(
				"search for all logs in logbook book OR book2 failed, expected 4 found "
						+ logs.size(), logs.size() == 4);

	}

	/**
	 * Test tag query When multiple tags are queried, the result is a logical OR
	 * of all the query conditions
	 */
	@Test
	public void queryLogsbyTag() {
		MultivaluedMap<String, String> map = new MultivaluedMapImpl();
		Collection<Log> queryResult;
		// Search for a logs with tag 'Tag*'
		map.add("tag", "Tag*");
		queryResult = client.findLogs(map);
		assertTrue(queryResult.size() == 1);
		// query for logs with anyone of a group of tags
		map.clear();
		map.add("tag", "taga");
		map.add("tag", "tagb");
		queryResult = client.findLogs(map);
		assertTrue(queryResult.size() == 3);
	}

	@Test
	public void queryLogsbyDescription() {
		MultivaluedMap<String, String> map = new MultivaluedMapImpl();
		// search a single log based on description.
		map.add("search", pvk_01.getDescription());
		Collection<Log> queryResult = client.findLogs(map);
		assertTrue(
				"Failed to search based on the log descrition expected 1 found "
						+ queryResult.size(), queryResult.size() == 1
						&& queryResult.contains(pvk_01));
		// search for "some detail" which matches multiple logs.
		map.clear();
		map.add("search", "*some details");
		queryResult = client.findLogs(map);
		assertTrue(
				"Failed to search based on the log description expected 2 found "
						+ queryResult.size(),
				queryResult.size() == 2 && queryResult.contains(pvk_03)
						&& queryResult.contains(distinctName));
		// search for logs with one
		map.clear();
		map.add("search", pvk_01.getDescription());
		map.add("search", pvk_02.getDescription());
		queryResult = client.findLogs(map);
		assertTrue(
				"Failed to search based on the log descrition expected 2 found "
						+ queryResult.size(),
				queryResult.size() == 2 && queryResult.contains(pvk_01)
						&& queryResult.contains(pvk_02));

	}

	/**
	 * Test the querying based on the create time
	 * 
	 * @param first
	 */
	@Test
	public void queryLogsbyTime() {
		Log first;
		Log third;
		int initialLogCount;

		Collection<Log> searchResult;
		Map<String, String> searchParameters = new HashMap<String, String>();

		try {
			synchronized (this) {
				initialLogCount = client.listLogs().size();
				this.wait(1000L);
				client.set(log().description("Test log1 for time")
						.appendDescription("test log").level("info")
						.appendToLogbook(book));
				this.wait(1000L);
				client.set(log().description("Test log2 for time")
						.appendDescription("test log").level("info")
						.appendToLogbook(book));
				this.wait(1000L);
				client.set(log().description("Test log3 for time")
						.appendDescription("test log").level("info")
						.appendToLogbook(book));
				this.wait(1000L);
				// XXX
				// A search is required because the response from the service
				// for a put only contains Id info and not the create time.

				first = client.findLogsBySearch("Test log1 for time*")
						.iterator().next();
				third = client.findLogsBySearch("Test log3 for time*")
						.iterator().next();
			}

			// check the _start_ search condition
			searchParameters.put("start",
					String.valueOf(first.getCreatedDate().getTime() / 1000L));
			searchResult = client.findLogs(searchParameters);
			assertTrue(
					"failed to search based on the start time, expected 3 found "
							+ searchResult.size(), searchResult.size() == 3);

			searchParameters.clear();
			searchParameters.put("start",
					String.valueOf(third.getCreatedDate().getTime() / 1000L));
			searchResult = client.findLogs(searchParameters);
			assertTrue(
					"failed to search based on the start time, expect 1 found "
							+ searchResult.size(), searchResult.size() == 1);

			// Check the _end_ search condition
			searchParameters.clear();
			searchParameters.put("end", String.valueOf(0L));
			searchResult = client.findLogs(searchParameters);
			assertTrue(
					"failed to search based on the end time, expected 0 found "
							+ searchResult.size(), searchResult.size() == 0);
			searchParameters.clear();
			searchParameters.put("end", String.valueOf((first.getCreatedDate()
					.getTime() / 1000L) - 1));
			searchResult = client.findLogs(searchParameters);
			assertTrue("failed to search based on the end time, expected "
					+ initialLogCount + " found " + searchResult.size(),
					searchResult.size() == initialLogCount);
			searchParameters.clear();
			searchParameters.put("end", String.valueOf((third.getCreatedDate()
					.getTime() / 1000L) + 1));
			searchResult = client.findLogs(searchParameters);
			assertTrue("failed to search based on the end time, expected "
					+ (initialLogCount + 3) + " found " + searchResult,
					searchResult.size() == (initialLogCount + 3));

			// check the _start_ and _end_ search conditions
			searchParameters.clear();
			searchParameters.put("start",
					String.valueOf(first.getCreatedDate().getTime() / 1000L));
			searchParameters.put("end",
					String.valueOf((first.getCreatedDate().getTime() / 1000L)));
			searchResult = client.findLogs(searchParameters);
			assertTrue(
					"failed to search based on the start & end time, expected 1 found "
							+ searchResult.size(), searchResult.size() == 1);
			searchParameters.clear();
			searchParameters.put("start",
					String.valueOf(first.getCreatedDate().getTime() / 1000L));
			searchParameters.put("end",
					String.valueOf((third.getCreatedDate().getTime() / 1000L)));
			searchResult = client.findLogs(searchParameters);
			assertTrue(
					"failed to search based on the start & end time, expected 3 found "
							+ searchResult.size(), searchResult.size() == 3);

		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			// clean up
			searchResult = client.findLogsBySearch("Test log* for time*");
			client.delete(searchResult);
		}
	}

	/**
	 * Test findLogsby*() methods
	 */
	@Test
	public void queryTest() {
		// find by search
		assertTrue("Failed to query using findbysearch method ", client
				.findLogsBySearch("pvk_*").size() == 3);

		// find by tag
		assertTrue("Failed to query using the findbytag method ", client
				.findLogsByTag(tagA.build().getName()).size() == 2);

		// find by logbook
		assertTrue("Failed to query using the findbylogbook method", client
				.findLogsByLogbook(book2.build().getName()).size() == 1);
	}
}
