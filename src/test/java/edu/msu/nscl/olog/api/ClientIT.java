package edu.msu.nscl.olog.api;

import static edu.msu.nscl.olog.api.LogBuilder.log;
import static edu.msu.nscl.olog.api.LogUtil.getLogSubjects;
import static edu.msu.nscl.olog.api.LogUtil.toLogs;
import static edu.msu.nscl.olog.api.LogbookBuilder.logbook;
import static edu.msu.nscl.olog.api.TagBuilder.tag;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.msu.nscl.olog.api.OlogClientImpl.OlogClientBuilder;

public class ClientIT {

	private static OlogClient client;

	private static String logOwner;
	private static String logbookOwner;
	private static String tagOwner;
	private static String propertyOwner;

	private static LogbookBuilder defaultLogBook;

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

		// Add a default logbook
		defaultLogBook = logbook("DefaultLogBook").owner(logbookOwner);
		client.set(defaultLogBook);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		client.deleteLogbook(defaultLogBook.build().getName());
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
	 * 
	 * FIXME (shroffk) setlog should return the log id
	 */
	@Test
	public void logSimpleTest() {
		LogBuilder log = log("testLog").description("some details")
				.level("Info").in(defaultLogBook);

		Map<String, String> map = new Hashtable<String, String>();
		map.put("search", "testLog");
		Log result = null;

		try {
			// set a log
			result = client.set(log);
			// check if the returned id is the same
			Collection<Log> queryResult = client.findLogs(map);
			assertTrue("The returned id is not valid",
					queryResult.contains(result));
		} catch (Exception e) {
			fail(e.getCause().toString());
		} finally {
			// delete a log
			client.delete(log(result));
			assertFalse("Failed to clean up the testLog", client.findLogs(map)
					.contains(result));
		}

	}

	/**
	 * create(set), list and delete a group of logs
	 * 
	 * FIXME (shroffk) setlog should return the log id
	 */
	@Test
	public void logsSimpleTest() {
		LogBuilder log1 = log("testLog1").description("some details")
				.level("Info").in(defaultLogBook);
		LogBuilder log2 = log("testLog2").description("some details")
				.level("Info").in(defaultLogBook);
		Collection<LogBuilder> logs = new ArrayList<LogBuilder>();
		logs.add(log1);
		logs.add(log2);

		Map<String, String> map = new Hashtable<String, String>();
		map.put("search", "testLog*");
		Collection<Log> result = null;
		Collection<Log> queryResult;

		try {
			// set a group of channels
			client.set(logs);
			// list all logs
			result = client.listLogs();
			queryResult = client.findLogs(map);
			// check the returned logids match the number expected
			assertTrue("unexpected return after creation of log entries",
					queryResult.size() == logs.size());
			// check if all the logs have been created
			assertTrue("Failed to set the group of logs",
					queryResult.containsAll(result));
		} catch (Exception e) {
			fail(e.getCause().toString());
		} finally {
			// delete a group of logs
			for (Log log : result) {
				client.delete(log(log));
			}
			queryResult = client.findLogs(map);
			for (Log log : result) {
				assertFalse("Failed to clean up the group of test logs",
						queryResult.contains(log));
			}
		}
	}

	/**
	 * Test set on a logbook, the logbook should be added to only those logs
	 * specified and removed from all others
	 */
	@Test
	public void logbookSetTest() {

	}

	@Test
	public void listLogsTest() {
		try {
			Collection<Log> result = client.listLogs();
		} catch (Exception e) {
			fail("failed to list logs");
		}
	}

	// @XmlRootElement
	// public static class XmlProperty{
	// public String name;
	// public Map<String,List<String>> map;
	//
	// }
	//
	// public void testparsin() throws JAXBException{
	// MultivaluedMap<String, String> map = new MultivaluedMapImpl();
	// map.add("ticket", "1234");
	//
	// XmlProperty prop = new XmlProperty();
	// prop.name = "trac";
	// prop.map = map;
	//
	//
	// JAXBContext context = JAXBContext.newInstance(XmlProperty.class);
	// Marshaller m = context.createMarshaller();
	// m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	// m.marshal(prop, System.out);
	//
	// MultivaluedMap<String, String> map2 = new MultivaluedMapImpl();
	// map2.add("type", "coupler");
	// map2.add("fieldname", "coupler1");
	//
	// XmlProperty prop2 = new XmlProperty();
	// prop2.name = "component";
	// prop2.map = map2;
	//
	// JAXBContext context2 = JAXBContext.newInstance(XmlProperty.class);
	// Marshaller m2 = context.createMarshaller();
	// m2.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	// m2.marshal(prop2, System.out);
	// }
}
