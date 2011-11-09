package edu.msu.nscl.olog.api;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.msu.nscl.olog.api.OlogClientImpl.OlogClientBuilder;
import static edu.msu.nscl.olog.api.LogbookBuilder.*;
import static edu.msu.nscl.olog.api.TagBuilder.*;

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
	 * create(set), find and delete a logbook
	 * 
	 */
	@Test
	public void logbookTest() {
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
	 * create(set), find and delete a tag
	 */
	@Test
	public void tagsTest() {
		TagBuilder tag = tag("testTag");
		try {
			// set a tag
			// list all tag
			client.set(tag);
			assertTrue("failed to set the testTag", client.listTags()
					.contains(tag.build()));
		} catch (Exception e) {
			fail(e.getCause().toString());
		} finally {
			// delete a tag
			client.deleteLogbook(tag.build().getName());
			assertFalse("failed to clean the testTag", client.listTags().contains(tag.build()));
		}
	}
}
