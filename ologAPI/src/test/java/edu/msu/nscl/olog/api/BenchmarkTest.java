package edu.msu.nscl.olog.api;

import static edu.msu.nscl.olog.api.LogBuilder.log;
import static edu.msu.nscl.olog.api.LogbookBuilder.logbook;
import static edu.msu.nscl.olog.api.TagBuilder.tag;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BenchmarkTest {

	private static Collection<LogBuilder> logs = new HashSet<LogBuilder>();
        private static Collection<Log> returnLogs = new HashSet<Log>();
	private static long originalLogCount;
	private long time;
	private static OlogClient client = OlogClient
			.getInstance();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// create a table of 2000 logs
		originalLogCount = client.getAllLogs().size();
		// Add the tags and the logbooks to be used.
		client.add(logbook("logbook").owner("me"));
		client.add(tag("tagA").state("boss"));
		client.add(tag("tagB").state("boss"));

		for (int i = 0; i < 2000; i++) {
			String logName = "2000";
			logName += getSubject(i);
			LogBuilder log = log(logName).description("boss").in(
					logbook("logbook"));
			if (i < 1000)
				log.with(tag("tagA", "boss"));
			if ((i >= 500) || (i < 1500))
				log.with(tag("tagB", "boss"));
			logs.add(log);
		}
		// Add all the logs;
		try {
			returnLogs= OlogClient.getInstance().add(logs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		OlogClient.getInstance().remove(returnLogs);
		assertTrue(OlogClient.getInstance().getAllLogs().size() == originalLogCount);
	}

	private static String getSubject(int i) {
		if (i < 1000)
			return "first:" + getSubject500(i);
		else
			return "second:" + getSubject500(i - 1000);
	}

	private static String getSubject500(int i) {
		if (i < 500)
			return "a" + getSubject100(i);
		else
			return "b" + getSubject100(i - 500);
	}

	private static String getSubject100(int i) {
		return "(" + Integer.toString(i / 100) + "00)" + getSubjectID(i % 100);
	}

	private static String getSubjectID(int i) {
		return ":" + Integer.toString(i / 10) + ":" + Integer.toString(i);
	}

	@Test
	public synchronized void query1Log() {
		time = System.currentTimeMillis();
		try {
			Log ch = OlogClient.getInstance().getLog(
					returnLogs.iterator().next().getId());
			assertTrue(ch.getSubject().equals("2000first:a<000>:0:0"));
			System.out.println("query1Log duration : "
					+ (System.currentTimeMillis() - time));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void query10Logs() {
		time = System.currentTimeMillis();
		Collection<Log> chs = OlogClient.getInstance()
				.findLogsBySearch("2000first:a<400>:0*");
		assertTrue(chs.size() == 10);
		System.out.println("query10Logs duration : "
				+ (System.currentTimeMillis() - time));
	}

	@Test
	public void query100Logs() {
		time = System.currentTimeMillis();
		Collection<Log> chs = OlogClient.getInstance()
				.findLogsBySearch("2000first:a<400>:*");
		assertTrue(chs.size() == 100);
		System.out.println("query100Logs duration : "
				+ (System.currentTimeMillis() - time));
	}

	@Test
	public void query500Logs() {
		time = System.currentTimeMillis();
		Collection<Log> chs = OlogClient.getInstance()
				.findLogsBySearch("2000first:b*");
		assertTrue(chs.size() == 500);
		System.out.println("query500Logs duration : "
				+ (System.currentTimeMillis() - time));
	}

	@Test
	public void query1000Logs() {
		time = System.currentTimeMillis();
		Collection<Log> chs = OlogClient.getInstance()
				.findLogsBySearch("2000second:*");
		assertTrue(chs.size() == 1000);
		System.out.println("query1000Logs duration : "
				+ (System.currentTimeMillis() - time));
	}

	@Test
	public synchronized void query2000Logs() {
		time = System.currentTimeMillis();
		Collection<Log> chs = OlogClient.getInstance()
				.findLogsBySearch("2000*");
		assertTrue(chs.size() == 2000);
		System.out.println("query2000Logs duration : "
				+ (System.currentTimeMillis() - time));
	}

}
