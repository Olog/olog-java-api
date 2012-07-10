/**
 * 
 */
package edu.msu.nscl.olog.api;

import static edu.msu.nscl.olog.api.LogbookBuilder.logbook;
import static edu.msu.nscl.olog.api.TagBuilder.tag;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.msu.nscl.olog.api.OlogClientImpl.OlogClientBuilder;

/**
 * @author shroffk
 * 
 */
public class ScalabilityIT {

	private static final String Owner = "owner";
	private static OlogClientImpl client;
	private static Collection<Logbook> logbooks;
	private static Collection<Tag> tags;
	private static Collection<Level> levels;
	private static Collection<LogBuilder> logs;

	// Default logbook for all tests
	private static LogbookBuilder defaultLogBook;
	// Default tag for all tests
	private static TagBuilder defaultTag;

	private static Collection<Log> setLogs;

	@BeforeClass
	public static void setup() {
		try {
			client = OlogClientBuilder.serviceURL()
					.withHTTPAuthentication(true).create();
			logbooks = client.listLogbooks();
			tags = client.listTags();
			levels = client.listLevels();

			// Add a default logbook
			defaultLogBook = logbook("ScalabilityLogbook").owner(Owner);
			client.set(defaultLogBook);
			// Add a default Tag
			defaultTag = tag("ScalabilityTag");
			client.set(defaultTag);
			logs = new ArrayList<LogBuilder>();

			for (int i = 0; i < 7000; i++) {
				logs.add(LogBuilder.log().level("Info")
						.description("Scalability test log entry:" + i)
						.appendToLogbook(defaultLogBook).appendTag(defaultTag));
			}
			setLogs = client.set(logs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Collection<Log> result = client.findLogsBySearch("Scalability*");
			client.delete(result);
			e.printStackTrace();
		}
	}

	@Test
	public void test() {
		// create a new entry
		LogBuilder newEntry = LogBuilder.log()
				.description("Scalability test: post setup entry")
				.level("Info").appendToLogbook(defaultLogBook)
				.appendTag(defaultTag);
		Log createdLog = client.set(newEntry);
		assertTrue(client.findLogById(createdLog.getId()).equals(createdLog));
		// update an entry
		client.update(LogBuilder.log(createdLog).appendDescription(
				"updating existing Entry"));
		assertTrue(client.findLogsBySearch("*updating existing Entry*").size() != 0);
		// create a few new entries
		Collection<LogBuilder> newLogs = new ArrayList<LogBuilder>();
		for (int i = 0; i < 5; i++) {
			newLogs.add(LogBuilder.log()
					.description("Scalability test: post setup entry" + i)
					.level("Info").appendToLogbook(defaultLogBook)
					.appendTag(defaultTag));
		}
		client.set(newLogs);
		assertTrue(client.findLogsBySearch("*post setup entry*").size() == 6);
		// update a few entries

	}

	@AfterClass
	public static void cleanup() {
		for (Log log : setLogs) {
			client.delete(log.getId());
		}
		Collection<Log> result = client.findLogsBySearch("Scalability*");
		client.delete(result);
		client.deleteLogbook(defaultLogBook.build().getName());
		client.deleteTag(defaultTag.build().getName());
		System.out.println("finished test");
	}

}
