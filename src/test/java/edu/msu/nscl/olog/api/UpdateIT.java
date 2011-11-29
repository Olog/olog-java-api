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

	private static TagBuilder tag = tag("defaultTag");
	private static LogbookBuilder logbook = logbook("defaultLogbook").owner(
			"me");
	private static Log log1;
	private static Log log2;

	@BeforeClass
	public static void beforeClass() {
		client = OlogClientBuilder.serviceURL().withHTTPAuthentication(true)
				.create();
		client.set(logbook);
		client.set(tag);

		log1 = client.set(log("log1").description("some details").level("Info")
				.in(logbook));
		log2 = client.set(log("log2").description("some details").level("Info")
				.in(logbook));

	}

	@AfterClass
	public static void afterClass() {
		client.deleteLogbook(logbook.build().getName());
		client.deleteTag(tag.build().getName());

		client.delete(client.findLogsBySearch("log*"));
	}

	/**
	 * update a single log
	 */
	@Test
	public void updateLog() {
		try {
			client.update(log(log1).with(tag));
			assertTrue(
					"failed to update log with tag",
					client.findLogsBySearch("log1").iterator().next()
							.getTag(tag.build().getName()) != null);
		} finally {

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
		queryResult = client.findLogsByTag(tag.build().getName());
		assertTrue("Tag already present on log2", !queryResult.contains(log2));
		client.update(tag, log2.getId());
		queryResult = client.findLogsByTag(tag.build().getName());
		assertTrue("Failed to update log2 with tag",
				LogUtil.getLogSubjects(queryResult).contains(log2.getSubject()));
	}

	@Test
	public void updateTag2Logs() {
		TagBuilder newTag = tag("newTag");
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

	}
}
