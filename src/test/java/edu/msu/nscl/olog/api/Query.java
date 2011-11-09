package edu.msu.nscl.olog.api;

import static edu.msu.nscl.olog.api.LogBuilder.*;
import static edu.msu.nscl.olog.api.LogUtil.*;
import static edu.msu.nscl.olog.api.LogbookBuilder.*;
import static edu.msu.nscl.olog.api.TagBuilder.*;
import static edu.msu.nscl.olog.api.PropertyBuilder.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import edu.msu.nscl.olog.api.OlogClientImpl.OlogClientBuilder;

//multivalue map

public class Query {

        private static Collection<Log> returnLogs = new HashSet<Log>();
	private static int initialLogCount;

	private static OlogClient client = OlogClientBuilder.serviceURL().withHTTPAuthentication(true).create();

	// Tags
	static TagBuilder tagA = tag("Taga", "me");
	static TagBuilder tagB = tag("Tagb", "me");
	static TagBuilder tagC = tag("Tagc", "me");
	static TagBuilder tagStar = tag("Tag*", "me");
	// Logbooks
	static LogbookBuilder book = logbook("book");
	static LogbookBuilder book2 = logbook("book2");

	/**
	 * insert test data - for performing the queries described below.
	 */
	@BeforeClass
	public static void populateLogs() {

		try {
			initialLogCount = client.listLogs().size();
			// Add the tags and logbooks.
			client.set(book.owner("me"));
			client.set(book2.owner("me"));
			client.set(tagA);
			client.set(tagB);
			client.set(tagC);
			client.set(tagStar);

			// Add the logs
			returnLogs.add(client.set(log("pvk:01<first>").description("some details").level("Info").in(
					book).in(book2).with(tagA)));
			returnLogs.add(client.set(log("pvk:02<second>").description("some details").level("Info").in(
					book).with(tagA).with(tagB)));
			returnLogs.add(client.set(log("pvk:03<second>").description("some details").level("Info").in(
					book).with(tagB).with(tagC)));
			returnLogs.add(client.set(log("distinctName").description("some details").level("Info").in(
					book).with(tagStar)));
		} catch (OlogException e) {
			fail(e.getMessage());
		}
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

	/**
	 * 
	 */
	@Test
	public void queryLogs() {
		Map<String, String> map = new Hashtable<String, String>();
		map.put("search", "pvk:*");
		Collection<Log> logs = client.findLogs(map);
		assertTrue(logs.size() == 3);
	}

	/**
	 * When multiple logbooks are queried, the result is a logical AND of all
	 * the query conditions
	 */
	@Test
	public void queryLogsbyLogbook() {
		Map<String, String> map = new Hashtable<String, String>();
		map.put("logbook", "*k");
		Collection<Log> logs = client.findLogs(map);
		assertTrue(logs.size() == 2);

		map.put("logbook", "*k");
		map.put("logbook", "*k2");
		logs = client.findLogs(map);
		assertTrue(logs.size() == 1);

	}

	/**
	 * When you have multiple value for same logbook results in the values
	 * being OR'ed
	 */
	@Test
	public void testMultipleParameters() {
		MultivaluedMapImpl map = new MultivaluedMapImpl();
		map.add("logbook", "1");
		map.add("logbook", "2");
		Collection<Log> logs = client.findLogs(map);
		assertTrue(logs.size() == 3);
	}

	/**
	 * Testing for the use of special chars.
	 */
	@Test
	public void testQueryForSpecialChar() {
		MultivaluedMapImpl map = new MultivaluedMapImpl();
		// logbook values are special chars
		map.add("search", "*");
		assertTrue(client.findLogs(map).size() == 4);
		map.clear();
		map.add("search", "\\*");
		assertTrue(client.findLogs(map).size() == 1);
		// tag names are special chars
		map.clear();
		map.add("tag", "Tag*");
		assertTrue(client.findLogs(map).size() == 4);
		map.clear();
		map.add("tag", "Tag\\*");
		assertTrue(client.findLogs(map).size() == 1);
	}

	@AfterClass
	public static void cleanup() {
		client.delete(returnLogs);
		// clean up all the tags and logbooks
		client.deleteLogbook(book.build().getName());
		client.deleteLogbook(book2.toXml().getName());
		client.deleteTag(tagA.toXml().getName());
		client.deleteTag(tagB.toXml().getName());
		client.deleteTag(tagC.toXml().getName());
		client.deleteTag(tagStar.toXml().getName());
		assertTrue(client.listLogs().size() == initialLogCount);
	}
}