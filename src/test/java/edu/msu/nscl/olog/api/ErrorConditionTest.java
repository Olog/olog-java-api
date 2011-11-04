package edu.msu.nscl.olog.api;

import static edu.msu.nscl.olog.api.LogBuilder.log;
import static edu.msu.nscl.olog.api.TagBuilder.tag;
import static edu.msu.nscl.olog.api.LogbookBuilder.logbook;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Test;

import edu.msu.nscl.olog.api.OlogClientImpl.OlogClientBuilder;

public class ErrorConditionTest {

	// TODO
	// test check if error with the correct messages are thrown.

	// @Test
	// public void addEmptyLog() {
	// try {
	// OlogClient.getInstance().addLog(log(new XmlLog()));
	// fail("Added an empty log.");
	// } catch (OlogException e) {
	// assertTrue(true);
	// }
	//
	// }

	private static OlogClient client = OlogClientBuilder.serviceURL().withHTTPAuthentication(true).create();
	// TODO: javax.ws.rs.WebApplicationException should be in OlogException
	@Test(expected = javax.ws.rs.WebApplicationException.class)
	public void addOrphanLog() {
		XmlLog xmlLog = new XmlLog();
		xmlLog.setSubject("onlySubject");
		client.add(log(xmlLog.getSubject()).level("Info"));
	}

	@Test(expected = OlogException.class)
	public void addOrphanLogbook() {
		client.add(logbook("JustName"));
	}

	/**
	 * Add a set of logs with one incorrect log
	 */
	@Test
	public void addSetwithBadLog() {
		Collection<LogBuilder> logs = new HashSet<LogBuilder>();
		logs.add(log("name1").description("some details").level("Info").in(logbook("book")));
		logs.add(log("").in(logbook("book")));
		// log ch2 has empty name
		try {
                        client.add(logbook("book"));
			client.add(logs);
			assertTrue(false);
		} catch (OlogException e) {
			assertTrue(true);
		}

		// log ch2 has name but no owner
		logs.clear();
		logs.add(log("name1").description("some details").level("Info").in(logbook("book")));
		logs.add(log("name2").in(logbook("book")));
		try {
                        client.add(logbook("book"));
			client.add(logs);
			assertTrue(false);
		} catch (OlogException e) {
			assertTrue(true);
		}
	}

	/**
	 * Try to delete a non-existent log
	 */
	@Test
	public void removeNonExistentLog() {
		try {
                        Long nonExistingId = 0L;
			client.remove(nonExistingId);
			assertTrue(false);
		} catch (OlogException e) {
			assertTrue(true);
		}
	}

	public void removeSetWithNonExistentLog() {

	}

	/**
	 * Test detection of error condition - attempting to update a log that
	 * does not exist
	 */
        // TODO: NullPointerException should be in OlogException
	@Test
	public void updateNonExistentLog() {
		try {
                        Long nonExistingId = 0L;
                        XmlLog xmlLog = null;
                        xmlLog.setId(nonExistingId);
                        xmlLog.setSubject("subject");
                        xmlLog.setOwner("me");
                        xmlLog.addXmlLogbook(logbook("book").toXml());
                        Log log = new Log(xmlLog);
			client.updateLog(log(log));
			assertTrue(false);
		} catch (OlogException e) {
			assertTrue(true);
		} catch (java.lang.NullPointerException e) {
                        assertTrue(true);
                }
	}

	@Test
	public void addTag2NonExistentLog() {
                Long nonExistingId = 0L;
		try {
			client.add(tag("sometag"),
					nonExistingId);
			assertTrue(false);
		} catch (OlogException e) {
			assertTrue(true);
		}
	}

	public void addLogbook2NonExistentLog() {

	}

}
