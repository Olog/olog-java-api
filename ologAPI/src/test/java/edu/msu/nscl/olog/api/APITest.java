package edu.msu.nscl.olog.api;

import static edu.msu.nscl.olog.api.LogBuilder.*;
import static edu.msu.nscl.olog.api.LogUtil.*;
import static edu.msu.nscl.olog.api.LogbookBuilder.*;
import static edu.msu.nscl.olog.api.TagBuilder.*;
import static edu.msu.nscl.olog.api.PropertyBuilder.*;
import java.io.IOException;
import org.apache.jackrabbit.webdav.DavException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashSet;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.sun.jersey.api.client.ClientResponse.Status;

import edu.msu.nscl.olog.api.OlogClient.OlogClientBuilder;

import java.io.File;
import java.io.FileWriter;

public class APITest {
	private static OlogClient client;
	private static int logCount;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void beforeTests() {
		client = OlogClientBuilder.serviceURL().withHTTPAuthentication(true).create();
		logCount = client.getAllLogs().size();
	}

	@Test
	public void test() {

	}

	@Test
	public void builderTest() {
		exception.expect(is(OlogException.class));
		exception.expect(new StatusMatcher(Status.NOT_FOUND));
                Long logId = new Long(1);
		client.getLog(logId);
	}

	/**
	 * Add a single log
	 */
	@Test
	public void addRemoveLog() {
		String logSubject = "TestLogSubject";
                String owner = "me";
                Log returnLog = null;
		try {
                        client.add(logbook("TestLogbook").owner(owner));
			// Add a log
			returnLog = client.add(log(logSubject).description("TestDetail").level("Info")
                                .in(logbook("TestLogbook").owner(owner)));
			client.getLog(returnLog.getId());
			// Remove a log
			client.remove(returnLog.getId());
			assertTrue(!client.getAllLogs().contains(returnLog));
			assertTrue("CleanUp failed",
					client.getAllLogs().size() == logCount);
		} catch (OlogException e) {
			if (e.getStatus().equals(Status.NOT_FOUND))
				fail("Log not added. "+ returnLog.toString() + e.getMessage());
		} finally {
                        if(returnLog!=null) {
                            client.deleteLogbook("TestLogbook");
                        }

		}
	}

	/**
	 * Add a set of logs
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void addRemoveLogs() {
                String owner = "me";
		Collection<LogBuilder> logs = new HashSet<LogBuilder>();
                Collection<Log> returnLogs = new HashSet<Log>();
		logs.add(log("first").description("TestDescription").level("Info")
                        .in(logbook("TestLogbook").owner(owner)));
		logs.add(log("second").description("TestDescription").level("Info")
                        .in(logbook("TestLogbook").owner(owner)));
		try {
                        client.add(logbook("TestLogbook").owner(owner));
			returnLogs = client.add(logs);
                       // fail("Logs not added: " + returnLogs.toString()+"\n contained:\n"+client.getAllLogs().toString());
			assertTrue(client.getAllLogs()
					.containsAll(returnLogs));	
                } finally {
			client.remove(returnLogs);
                        client.deleteLogbook("TestLogbook");
			assertTrue("CleanUp failed",
					client.getAllLogs().size() == logCount);
		}
	}

	/**
	 * update an existing log with a new Logbook and new tag
	 * 
	 * Add/update test
	 */
	@Test
	public void updateLog() {
                String owner = "me";
		LogBuilder testLog = log("TestLog").description("some details").level("Info")
                        .in(logbook("TestLogbook").owner(owner));
		TagBuilder testTag1 = tag("someTag1");
		TagBuilder testTag2 = tag("someTag2");
                Log returnLog = null;
		try {
                        client.add(logbook("TestLogbook").owner(owner));
			// ensure that the tag exist.
			client.add(testTag1);
			client.add(testTag2);
			returnLog = client.add(testLog.with(testTag1));
			// check for no initial logbooks or tags
			assertTrue(returnLog.getTags().contains(testTag1.build()));
			// uses the POST method
			client.updateLog(log(returnLog).with(testTag2));
			assertTrue(client.getLog(returnLog.getId())
					.getTags().contains(testTag1.build()));
			assertTrue(client.getLog(returnLog.getId())
					.getTags().contains(testTag2.build()));
		} finally {
                    if(returnLog != null) {
                        client.deleteLogbook("TestLogbook");
                        client.deleteTag("someTag1");
                        client.deleteTag("someTag2");
			client.remove(returnLog.getId());
			assertTrue("CleanUp failed",
					client.getAllLogs().size() == logCount);
                    }
		}

	}

	/**
	 * Test destructive update - existing log is completely replaced
	 */
	@Test
	public void setLog() {
                String owner = "me";
		LogBuilder oldLog = log("old").description("some details").level("Info")
                        .in(logbook("TestLogbook").owner(owner))
                        .with(tag("oldTag"));
		LogBuilder newLog = log("old").description("some details2").level("Info")
                        .in(logbook("TestLogbook").owner(owner))
                        .with(tag("newTag"));
                Log returnLogOld = null;
                Log returnLogNew = null;
                client.add(logbook("TestLogbook").owner(owner));
		client.add(tag("oldTag"));
		client.add(tag("newTag"));
		try {
			returnLogOld = client.add(oldLog);
                        
			assertTrue(client.findLogsByTag("oldTag").contains(
					returnLogOld));
                        XmlLog xmlLog = newLog.toXml();
                        xmlLog.setId(returnLogOld.getId());
                        newLog = log(new Log(xmlLog));
			client.add(newLog);                        
                        returnLogNew = client.getLog(newLog.toXml().getId());
			assertTrue(!client.findLogsByTag("oldTag").contains(
					returnLogNew));
			assertTrue(client.findLogsByTag("newTag").contains(
					returnLogNew));
		} finally {
                    if(returnLogNew!=null) {
                        client.deleteLogbook("TestLogbook");
			client.deleteTag("oldTag");
			client.deleteTag("newTag");
                        client.remove(returnLogOld.getId());
			client.remove(returnLogNew.getId());
                    }
		}
	}

	/**
	 * Add a Tag to a single log
	 */
	@Test
	public void addRemoveTag() {
		String logSubject = "TestLog";
		String tagName = "TestTag";
                String owner = "me";
                Log returnLog = null;
                try {
                    client.add(logbook("TestLogbook").owner(owner));
                    client.add(tag(tagName, "tagOwner"));
                    returnLog = client.add(log(logSubject).description("TestDetail").level("Info")
                            .in(logbook("TestLogbook").owner(owner)));
                    assertTrue(!getTagNames(client.getLog(returnLog.getId())).contains(
				tagName));
                    client.add(tag(tagName, "tagOwner"), returnLog.getId());
                    assertTrue(getTagNames(client.getLog(returnLog.getId()))
				.contains(tagName));
                    client.remove(tag(tagName), returnLog.getId());
                    assertTrue(!getTagNames(client.getLog(returnLog.getId())).contains(
				tagName));
                } finally {
                    if (returnLog!=null){
                        client.deleteLogbook("TestLogbook");
                        client.remove(returnLog.getId());
                        assertTrue("CleanUp failed", !client.getAllLogs().contains(
				returnLog));
                    }
                }
            }

	/**
	 * Add and Remove a tag from multiple logs
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void addRemoveTag2Logs() {
                String owner = "me";
		TagBuilder tag = tag("tag");
		Collection<LogBuilder> logSet = new HashSet<LogBuilder>();
		Collection<LogBuilder> logSubSet = new HashSet<LogBuilder>();
		Collection<Log> returnLogSet = new HashSet<Log>();
                Collection<Log> _returnLogSet = new HashSet<Log>();
                Collection<Log> returnLogSubSet = new HashSet<Log>();
                logSubSet.add(log("first").description("TestDetail").level("Info")
                        .in(logbook("TestLogbook").owner(owner)));
		logSubSet.add(log("second").description("TestDetail").level("Info")
                        .in(logbook("TestLogbook").owner(owner)));
		logSet.add(log("third").description("TestDetail").level("Info")
                        .in(logbook("TestLogbook").owner(owner)));

		try {
                        client.add(logbook("TestLogbook").owner(owner));
                        client.add(tag);
                        returnLogSubSet = client.add(logSubSet);
			_returnLogSet = client.add(logSet);
                        returnLogSet.addAll(_returnLogSet);
                        returnLogSet.addAll(returnLogSubSet);

			client.add(tag, getLogIds(returnLogSet));
			assertTrue(client.findLogsByTag(tag.build().getName())
					.containsAll(returnLogSet));
			client.remove(tag, getLogIds(returnLogSubSet));
			Collection<Log> diffSet = new HashSet<Log>(
					returnLogSet);
			diffSet.removeAll(returnLogSubSet);
			assertTrue(client.findLogsByTag(tag.build().getName())
					.containsAll(diffSet));
			
		} finally {
                        client.deleteLogbook("TestLogbook");
                        client.deleteTag("tag");
                        // this method is not atomic
			client.remove(returnLogSet);
		}

	}

	/**
	 * Remove Tag from all logs
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void deleteTag() {
                String owner = "me";
		Collection<LogBuilder> logs = new HashSet<LogBuilder>();
                Collection<Log> returnLogs = new HashSet<Log>();
		logs.add(log("first").description("TestDetail").level("Info")
                        .in(logbook("TestLogbook").owner(owner)));
		logs.add(log("second").description("TestDetail").level("Info")
                        .in(logbook("TestLogbook").owner(owner)));
		try {
                        client.add(logbook("TestLogbook").owner(owner));
			returnLogs = client.add(logs);
			TagBuilder tag = tag("TestTag");
			client.add(tag);
			client.add(tag, getLogIds(returnLogs));
			assertTrue(client.findLogsByTag("TestTag").size() > 0);
			client.deleteTag("TestTag");
			assertTrue(client.findLogsByTag("TestTag").isEmpty());
		} finally {
                        client.deleteLogbook("TestLogbook");
			client.remove(returnLogs);
		}
	}

	/**
	 * test the destructive setting of tags on log/s
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void setTag() {
                String owner = "me";
		Collection<LogBuilder> logSet1 = new HashSet<LogBuilder>();
		Collection<Log> returnLogSet1 = new HashSet<Log>();
                logSet1.add(log("first").description("TestDetail").level("Info")
                        .in(logbook("TestLogbook").owner(owner)));
		logSet1.add(log("second").description("TestDetail").level("Info")
                        .in(logbook("TestLogbook").owner(owner)));
		Collection<LogBuilder> logSet2 = new HashSet<LogBuilder>();
                Collection<Log> returnLogSet2 = new HashSet<Log>();
		logSet2.add(log("third").description("TestDetail").level("Info")
                        .in(logbook("TestLogbook").owner(owner)));
		logSet2.add(log("forth").description("TestDetail").level("Info")
                        .in(logbook("TestLogbook").owner(owner)));
		TagBuilder tag = tag("TestTag");

		try {
                        client.add(logbook("TestLogbook").owner(owner));
			returnLogSet2 = client.add(logSet2);
			returnLogSet1 = client.add(logSet1);
			client.add(tag);
			// add tag to set1
			client.add(tag, getLogIds(returnLogSet1));
			assertTrue(client.findLogsByTag(tag.toXml().getName())
					.containsAll(returnLogSet1));
			// set the tag on log first and remove it from every other
			// log
                        Log firstLog = returnLogSet1.iterator().next();
			client.set(tag, firstLog.getId());
			assertTrue(client.findLogsByTag(tag.build().getName()).size() == 1);
			assertTrue(client.findLogsByTag(tag.build().getName())
					.contains(firstLog));
			// add the tag to set2 and remove it from every other log
			client.set(tag, getLogIds(returnLogSet2));
			assertTrue(client.findLogsByTag(tag.build().getName()).size() == 2);
			assertTrue(client.findLogsByTag(tag.toXml().getName())
					.containsAll(returnLogSet2));
		} finally {
                        client.deleteLogbook("TestLogbook");
                        client.deleteTag("TestTag");
			client.remove(returnLogSet1);
			client.remove(returnLogSet2);
		}

	}

	/**
	 * Add and Remove a logbook to a single log
	 */
	@Test
	public void addRemoveLogbook() {
                String owner = "me";
		LogBuilder testLog = log("TestLog").description("this is a description").level("Info")
                        .in(logbook("TestLogbook").owner(owner));
		LogbookBuilder logbook = logbook("TestLogbook2").owner(owner);
                Log returnLog = null;

		try {
                        client.add(logbook("TestLogbook").owner(owner));
			returnLog = client.add(testLog);
			client.add(logbook);
			client.add(logbook, returnLog.getId());
			Collection<Log> result = client.findLogsByLogbook(logbook
					.build().getName());
			assertTrue(result.contains(returnLog));
			client.remove(logbook, returnLog.getId());
			assertTrue(client.findLogsByLogbook(logbook.toXml().getName()).isEmpty());
		} finally {
                        if(returnLog!=null) {
                            client.deleteLogbook("TestLogbook");
                            client.deleteLogbook("TestLogbook2");
                            client.remove(returnLog.getId());
                        }
		}

	}

	/**
	 * TODO fix assert stmt Add and Remove a logbook from multiple logs
	 */
	@Test
	public void addRemoveLogbook2Logs() {
                String owner = "me";
		Collection<LogBuilder> logs = new HashSet<LogBuilder>();
		Collection<Log> returnLogs = new HashSet<Log>();
                logs.add(log("first").description("some details").level("Info")
                        .in(logbook("TestLogbook").owner(owner)));
		logs.add(log("second").description("some details").level("Info")
                        .in(logbook("TestLogbook").owner(owner)));
		LogbookBuilder logbook = logbook("TestLogbook2").owner(owner);
		try {
                        client.add(logbook("TestLogbook").owner(owner));
			returnLogs = client.add(logs);
			client.add(logbook);
			int initialCount = client.findLogsByLogbook(
					logbook.toXml().getName()).size();
			// TODO: problem here; version changes, and I currently use version in equal override
                        client.add(logbook, getLogIds(returnLogs));
			assertTrue(client.findLogsByLogbook(logbook.toXml().getName())
                                .containsAll(returnLogs));
			client.remove(logbook, getLogIds(returnLogs));
			assertTrue(client.findLogsByLogbook(logbook.toXml().getName())
                                .size() == initialCount);
		} finally {
                        if(returnLogs!=null) {
                            client.deleteLogbook("TestLogbook");
                            client.deleteLogbook("TestLogbook2");
                            client.remove(returnLogs);
                        }
		}
	}

	/**
	 * TODO fix the asserts
	 */
	@Test
	public void deleteLogbook() {
                String owner = "me";
		Collection<LogBuilder> logs = new HashSet<LogBuilder>();
                Collection<Log> returnLogs = new HashSet<Log>();
		logs.add(log("first").description("some details").level("Info")
                        .in(logbook("TestLogbook").owner(owner)));
		logs.add(log("second").description("some details").level("Info")
                        .in(logbook("TestLogbook").owner(owner)));
		LogbookBuilder logbook = logbook("DeleteLogbook").owner(owner);
		try {
                        client.add(logbook("TestLogbook").owner(owner));
			returnLogs = client.add(logs);
			client.add(logbook);
			client.add(logbook, getLogIds(returnLogs));
			assertTrue(client.findLogsByLogbook(logbook.toXml().getName())
                                .size() == 2);
			client.deleteLogbook(logbook.toXml().getName());
			assertTrue(client.findLogsByLogbook(logbook.toXml().getName()).isEmpty());
		} finally {
                        if(returnLogs!=null) {
                            client.deleteLogbook("TestLogbook");
                            client.remove(returnLogs);
                        }
		}
	}

	/**
	 * check non-destructive addition of tags and logs
	 */
	@Test
	public void addTagsLogbook() {
                String owner = "me";
                Log returnLog = null;
                Log result = null;
                try {
                    client.add(logbook("TestLogbook").owner(owner));
                    client.add(logbook("existingLogbook").owner(owner));
                    client.add(tag("existingTag"));
                    LogBuilder testLog = log("testLog").description("some details").level("Info")
                                .in(logbook("TestLogbook").owner(owner))
				.with(tag("existingTag")).in(logbook("existingLogbook"));
                    returnLog = client.add(testLog);
                    result = (client.getLog(returnLog.getId()));
                    assertTrue(result.getTags().contains(
				tag("existingTag").build()));
                    assertTrue(result.getLogbooks().contains(
				logbook("existingLogbook").build()));

                    client.add(tag("newTag"));
                    client.add(tag("newTag"), result.getId());

                    client.add(logbook("newLogbook").owner(owner));
                    client.add(logbook("newLogbook"), result.getId());

                    result = (client.getLog(returnLog.getId()));
                    assertTrue(result.getTags().contains(
				tag("existingTag").build()));
                    assertTrue(result.getTags().contains(tag("newTag").build()));
                    assertTrue(result.getLogbooks().contains(
				logbook("existingLogbook").build()));
                    assertTrue(result.getLogbooks().contains(
				logbook("newLogbook").build()));

            } finally {
                    if(returnLog!=null){
                        client.deleteLogbook("TestLogbook");
                        client.deleteLogbook("existingLogbook");
                        client.deleteLogbook("newLogbook");
                        client.deleteTag("existingTag");
                        client.deleteTag("newTag");
                        client.remove(returnLog.getId());
                    }
            }
	}

        @Test
        public void attachFileToLogId() throws IOException, DavException {
                String owner = "me";
                Log returnLog = null;
                File f = null;
                try {
                    f = new File("file.txt");
                    if(!f.exists()){
                        FileWriter fwrite = new FileWriter(f);
                        fwrite.write("This is test file");
                        fwrite.flush();
                        fwrite.close();
                    }
                    returnLog = client.add(log("attachment test").description("TestDetail").level("Info")
                                .in(logbook("TestLogbook").owner(owner)));

                    client.add(f,returnLog.getId());
                    assertTrue(client.getAttachments(returnLog.getId()).size()==1);
                } finally {
                    if(returnLog!=null){
                            client.remove(returnLog.getId());
                            client.remove("file.txt",returnLog.getId());                         
                    }
                    if(f.exists()){
                            boolean success = f.delete();
                            assertTrue("attachment File clean up failed", success);
                    }
                }
        }
        @Test
        public void attachImageFileToLogId() throws IOException, DavException {
                String owner = "me";
                Log returnLog = null;
                File f = null;
                try {
                    f = new File("the_homercar.jpg");
                    returnLog = client.add(log("attachment test").description("TestDetail").level("Info")
                                .in(logbook("TestLogbook").owner(owner)));

                    client.add(f,returnLog.getId());
                    assertTrue(client.getAttachments(returnLog.getId()).size()==1);
                } finally {
                    if(returnLog!=null){
                            client.remove(returnLog.getId());
                            client.remove("the_homercar.jpg",returnLog.getId());
                    }
                }
        }
	/**
	 * Add a Property to a single log
	 */
	@Test
	public void addRemoveProperty() {
		String logSubject = "TestLog";
		String propertyName = "Component Type";
                String propertyValue = "EVR";
                String owner = "me";
                Log returnLog = null;
                PropertyBuilder prop = property(propertyName,propertyValue);
                try {
                    client.add(logbook("TestLogbook").owner(owner));
                    returnLog = client.add(log(logSubject).description("TestDetail").level("Info")
                            .in(logbook("TestLogbook").owner(owner)));
                    assertTrue(!((client.getLog(returnLog.getId()).getProperties()).contains(
				prop.build())));
                    client.add(prop, returnLog.getId());
                    assertTrue((client.getLog(returnLog.getId()).getProperties())
				.contains(prop.build()));
                    client.remove(property(propertyName), returnLog.getId());
                    assertTrue(!(client.getLog(returnLog.getId()).getProperties()).contains(
				prop.build()));
                } finally {
                    if (returnLog!=null){
                        client.deleteLogbook("TestLogbook");
                        client.remove(returnLog.getId());
                        assertTrue("CleanUp failed", !client.getAllLogs().contains(
				returnLog));
                    }
                }
            }

	@Test
	public void getAllLogbooks() {
                String owner = "me";
		String logbookName = "TestLogbook";
		client.add(logbook(logbookName).owner(owner));
		assertTrue(client.getAllLogbooks().contains(logbookName));
		client.deleteLogbook(logbookName);
		assertTrue("TestLogbook clean up failed", !client.getAllLogbooks()
				.contains(logbookName));
	}

        @Test
	public void getAllTags() {
		client.add(tag("TestTag"));
		assertTrue(client.getAllTags().contains("TestTag"));
		client.deleteTag("TestTag");
		assertTrue("TestTag clean up failed", !client.getAllTags().contains(
				"TestTag"));
	}

	class StatusMatcher extends BaseMatcher<OlogException> {

		private Status status;

		StatusMatcher(Status status) {
			this.status = status;
		}

		@Override
		public void describeTo(Description description) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean matches(Object item) {
			if (((OlogException) item).getStatus().equals(this.status))
				return true;
			else
				return false;
		}
	}
}
