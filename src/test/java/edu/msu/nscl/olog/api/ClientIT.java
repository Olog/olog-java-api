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
	 */
	@Test
	public void logSimpleTest() {
		LogBuilder log = log("testLog").description("some details")
				.level("Info").in(defaultLogBook);
		Collection<Log> result = null;
		try {
			// set a log
			client.set(log);
			// list all logs
			result = client.listLogs();
			// assertTrue("failed to set the testLog",
			// client.listLogs().contains(log.build()));
			assertTrue("Failed to set the testLog", getLogSubjects(result)
					.contains(log.build().getSubject()));

		} catch (Exception e) {
			fail(e.getCause().toString());
		} finally {
			// delete a log
			for (Log resultLog : result) {
				client.delete(log(resultLog));
			}
			result = client.listLogs();
			assertFalse("Failed to clean up the testLog",
					getLogSubjects(result).contains(log.build().getSubject()));
		}

	}

	/**
	 * create(set), list and delete a group of logs
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

		Collection<Log> result = null;

		try {
			// set a group of channels
			client.set(logs);
			// list all logs
			result = client.listLogs();
			// can't use the equals cause I don't have the id which is needed
			// for the equal check.
			//
			// assertTrue("Failed to set the group of logs.",
			// result.containsAll(LogUtil.toLogs(logs)));
			assertTrue(
					"Failed to set the group of logs",
					getLogSubjects(result).containsAll(
							getLogSubjects(toLogs(logs))));

		} catch (Exception e) {
			fail(e.getCause().toString());
		} finally {
			// delete a group of logs
			for (Log log : result) {
				client.delete(log(log));
			}
			result = client.listLogs();
			for (Log userLog : LogUtil.toLogs(logs)) {
				assertFalse("Failed to clean up the group of test logs",
						getLogSubjects(result).contains(userLog.getSubject()));
			}
		}
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
