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

import edu.msu.nscl.olog.api.OlogClientImpl.OlogClientBuilder;

import java.io.File;
import java.io.FileWriter;

public class API {

    private static OlogClient client;
    private static int logCount;
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void beforeTests() {
		client = OlogClientBuilder.serviceURL().withHTTPAuthentication(true).create();
        logCount = client.listLogs().size();
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
            client.update(logbook("TestLogbook").owner(owner));
            // Add a log
            returnLog = client.update(log(logSubject).description("TestDetail").level("Info").in(logbook("TestLogbook").owner(owner)));
            client.getLog(returnLog.getId());
            // Remove a log
            client.delete(returnLog.getId());
            assertTrue(!client.listLogs().contains(returnLog));
            assertTrue("CleanUp failed",
                    client.listLogs().size() == logCount);
        } catch (OlogException e) {
            if (e.getStatus().equals(Status.NOT_FOUND)) {
                fail("Log not added. " + returnLog.toString() + e.getMessage());
            }
        } finally {
            if (returnLog != null) {
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
        logs.add(log("first").description("TestDescription").level("Info").in(logbook("TestLogbook").owner(owner)));
        logs.add(log("second").description("TestDescription").level("Info").in(logbook("TestLogbook").owner(owner)));
        try {
            client.update(logbook("TestLogbook").owner(owner));
            returnLogs = client.update(logs);
            // fail("Logs not added: " + returnLogs.toString()+"\n contained:\n"+client.getAllLogs().toString());
            assertTrue(client.listLogs().containsAll(returnLogs));
        } finally {
            client.delete(returnLogs);
            client.deleteLogbook("TestLogbook");
            assertTrue("CleanUp failed",
                    client.listLogs().size() == logCount);
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
        LogBuilder testLog = log("TestLog").description("some details").level("Info").in(logbook("TestLogbook").owner(owner));
        TagBuilder testTag1 = tag("someTag1");
        TagBuilder testTag2 = tag("someTag2");
        Log returnLog = null;
        try {
            client.update(logbook("TestLogbook").owner(owner));
            // ensure that the tag exist.
            client.update(testTag1);
            client.update(testTag2);
            returnLog = client.update(testLog.with(testTag1));
            // check for no initial logbooks or tags
            assertTrue(returnLog.getTags().contains(testTag1.build()));
            // uses the POST method
            client.update(log(returnLog).with(testTag2));
            assertTrue(client.getLog(returnLog.getId()).getTags().contains(testTag1.build()));
            assertTrue(client.getLog(returnLog.getId()).getTags().contains(testTag2.build()));
        } finally {
            if (returnLog != null) {
                client.deleteLogbook("TestLogbook");
                client.deleteTag("someTag1");
                client.deleteTag("someTag2");
                client.delete(returnLog.getId());
                assertTrue("CleanUp failed",
                        client.listLogs().size() == logCount);
            }
        }

    }

    /**
     * Test destructive update - existing log is completely replaced
     */
    @Test
    public void setLog() {
        String owner = "me";
        LogBuilder oldLog = log("old").description("some details").level("Info").in(logbook("TestLogbook").owner(owner)).with(tag("oldTag"));
        LogBuilder newLog = log("old").description("some details2").level("Info").in(logbook("TestLogbook").owner(owner)).with(tag("newTag"));
        Log returnLogOld = null;
        Log returnLogNew = null;
        client.update(logbook("TestLogbook").owner(owner));
        client.update(tag("oldTag"));
        client.update(tag("newTag"));
        try {
            returnLogOld = client.update(oldLog);

            assertTrue(client.findLogsByTag("oldTag").contains(
                    returnLogOld));
            XmlLog xmlLog = newLog.toXml();
            xmlLog.setId(returnLogOld.getId());
            newLog = log(new Log(xmlLog));
            client.update(newLog);
            returnLogNew = client.getLog(newLog.toXml().getId());
            assertTrue(!client.findLogsByTag("oldTag").contains(
                    returnLogNew));
            assertTrue(client.findLogsByTag("newTag").contains(
                    returnLogNew));
        } finally {
            if (returnLogNew != null) {
                client.deleteLogbook("TestLogbook");
                client.deleteTag("oldTag");
                client.deleteTag("newTag");
                client.delete(returnLogOld.getId());
                client.delete(returnLogNew.getId());
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
            client.update(logbook("TestLogbook").owner(owner));
            client.update(tag(tagName, "tagOwner"));
            returnLog = client.update(log(logSubject).description("TestDetail").level("Info").in(logbook("TestLogbook").owner(owner)));
            assertTrue(!getTagNames(client.getLog(returnLog.getId())).contains(
                    tagName));
            client.update(tag(tagName, "tagOwner"), returnLog.getId());
            assertTrue(getTagNames(client.getLog(returnLog.getId())).contains(tagName));
            client.delete(tag(tagName), returnLog.getId());
            assertTrue(!getTagNames(client.getLog(returnLog.getId())).contains(
                    tagName));
        } finally {
            if (returnLog != null) {
                client.deleteLogbook("TestLogbook");
                client.delete(returnLog.getId());
                assertTrue("CleanUp failed", !client.listLogs().contains(
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
        logSubSet.add(log("first").description("TestDetail").level("Info").in(logbook("TestLogbook").owner(owner)));
        logSubSet.add(log("second").description("TestDetail").level("Info").in(logbook("TestLogbook").owner(owner)));
        logSet.add(log("third").description("TestDetail").level("Info").in(logbook("TestLogbook").owner(owner)));

        try {
            client.update(logbook("TestLogbook").owner(owner));
            client.update(tag);
            returnLogSubSet = client.update(logSubSet);
            _returnLogSet = client.update(logSet);
            returnLogSet.addAll(_returnLogSet);
            returnLogSet.addAll(returnLogSubSet);

            client.update(tag, getLogIds(returnLogSet));
            assertTrue(client.findLogsByTag(tag.build().getName()).containsAll(returnLogSet));
            client.delete(tag, getLogIds(returnLogSubSet));
            Collection<Log> diffSet = new HashSet<Log>(
                    returnLogSet);
            diffSet.removeAll(returnLogSubSet);
            assertTrue(client.findLogsByTag(tag.build().getName()).containsAll(diffSet));

        } finally {
            client.deleteLogbook("TestLogbook");
            client.deleteTag("tag");
            // this method is not atomic
            client.delete(returnLogSet);
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
        logs.add(log("first").description("TestDetail").level("Info").in(logbook("TestLogbook").owner(owner)));
        logs.add(log("second").description("TestDetail").level("Info").in(logbook("TestLogbook").owner(owner)));
        try {
            client.update(logbook("TestLogbook").owner(owner));
            returnLogs = client.update(logs);
            TagBuilder tag = tag("TestTag");
            client.update(tag);
            client.update(tag, getLogIds(returnLogs));
            assertTrue(client.findLogsByTag("TestTag").size() > 0);
            client.deleteTag("TestTag");
            assertTrue(client.findLogsByTag("TestTag").isEmpty());
        } finally {
            client.deleteLogbook("TestLogbook");
            client.delete(returnLogs);
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
        logSet1.add(log("first").description("TestDetail").level("Info").in(logbook("TestLogbook").owner(owner)));
        logSet1.add(log("second").description("TestDetail").level("Info").in(logbook("TestLogbook").owner(owner)));
        Collection<LogBuilder> logSet2 = new HashSet<LogBuilder>();
        Collection<Log> returnLogSet2 = new HashSet<Log>();
        logSet2.add(log("third").description("TestDetail").level("Info").in(logbook("TestLogbook").owner(owner)));
        logSet2.add(log("forth").description("TestDetail").level("Info").in(logbook("TestLogbook").owner(owner)));
        TagBuilder tag = tag("TestTag");

        try {
            client.update(logbook("TestLogbook").owner(owner));
            returnLogSet2 = client.update(logSet2);
            returnLogSet1 = client.update(logSet1);
            client.update(tag);
            // add tag to set1
            client.update(tag, getLogIds(returnLogSet1));
            assertTrue(client.findLogsByTag(tag.toXml().getName()).containsAll(returnLogSet1));
            // set the tag on log first and remove it from every other
            // log
            Log firstLog = returnLogSet1.iterator().next();
            client.set(tag, firstLog.getId());
            assertTrue(client.findLogsByTag(tag.build().getName()).size() == 1);
            assertTrue(client.findLogsByTag(tag.build().getName()).contains(firstLog));
            // add the tag to set2 and remove it from every other log
            client.set(tag, getLogIds(returnLogSet2));
            assertTrue(client.findLogsByTag(tag.build().getName()).size() == 2);
            assertTrue(client.findLogsByTag(tag.toXml().getName()).containsAll(returnLogSet2));
        } finally {
            client.deleteLogbook("TestLogbook");
            client.deleteTag("TestTag");
            client.delete(returnLogSet1);
            client.delete(returnLogSet2);
        }

    }

    /**
     * Add and Remove a logbook to a single log
     */
    @Test
    public void addRemoveLogbook() {
        String owner = "me";
        LogBuilder testLog = log("TestLog").description("this is a description").level("Info").in(logbook("TestLogbook").owner(owner));
        LogbookBuilder logbook = logbook("TestLogbook2").owner(owner);
        Log returnLog = null;

        try {
            client.update(logbook("TestLogbook").owner(owner));
            returnLog = client.update(testLog);
            client.update(logbook);
            client.update(logbook, returnLog.getId());
            Collection<Log> result = client.findLogsByLogbook(logbook.build().getName());
            assertTrue(result.contains(returnLog));
            client.delete(logbook, returnLog.getId());
            assertTrue(client.findLogsByLogbook(logbook.toXml().getName()).isEmpty());
        } finally {
            if (returnLog != null) {
                client.deleteLogbook("TestLogbook");
                client.deleteLogbook("TestLogbook2");
                client.delete(returnLog.getId());
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
        logs.add(log("first").description("some details").level("Info").in(logbook("TestLogbook").owner(owner)));
        logs.add(log("second").description("some details").level("Info").in(logbook("TestLogbook").owner(owner)));
        LogbookBuilder logbook = logbook("TestLogbook2").owner(owner);
        try {
            client.update(logbook("TestLogbook").owner(owner));
            returnLogs = client.update(logs);
            client.update(logbook);
            int initialCount = client.findLogsByLogbook(
                    logbook.toXml().getName()).size();
            // TODO: problem here; version changes, and I currently use version in equal override
            client.update(logbook, getLogIds(returnLogs));
            assertTrue(client.findLogsByLogbook(logbook.toXml().getName()).containsAll(returnLogs));
            client.delete(logbook, getLogIds(returnLogs));
            assertTrue(client.findLogsByLogbook(logbook.toXml().getName()).size() == initialCount);
        } finally {
            if (returnLogs != null) {
                client.deleteLogbook("TestLogbook");
                client.deleteLogbook("TestLogbook2");
                client.delete(returnLogs);
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
        logs.add(log("first").description("some details").level("Info").in(logbook("TestLogbook").owner(owner)));
        logs.add(log("second").description("some details").level("Info").in(logbook("TestLogbook").owner(owner)));
        LogbookBuilder logbook = logbook("DeleteLogbook").owner(owner);
        try {
            client.update(logbook("TestLogbook").owner(owner));
            returnLogs = client.update(logs);
            client.update(logbook);
            client.update(logbook, getLogIds(returnLogs));
            assertTrue(client.findLogsByLogbook(logbook.toXml().getName()).size() == 2);
            client.deleteLogbook(logbook.toXml().getName());
            assertTrue(client.findLogsByLogbook(logbook.toXml().getName()).isEmpty());
        } finally {
            if (returnLogs != null) {
                client.deleteLogbook("TestLogbook");
                client.delete(returnLogs);
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
            client.update(logbook("TestLogbook").owner(owner));
            client.update(logbook("existingLogbook").owner(owner));
            client.update(tag("existingTag"));
            LogBuilder testLog = log("testLog").description("some details").level("Info").in(logbook("TestLogbook").owner(owner)).with(tag("existingTag")).in(logbook("existingLogbook"));
            returnLog = client.update(testLog);
            result = (client.getLog(returnLog.getId()));
            assertTrue(result.getTags().contains(
                    tag("existingTag").build()));
            assertTrue(result.getLogbooks().contains(
                    logbook("existingLogbook").build()));

            client.update(tag("newTag"));
            client.update(tag("newTag"), result.getId());

            client.update(logbook("newLogbook").owner(owner));
            client.update(logbook("newLogbook"), result.getId());

            result = (client.getLog(returnLog.getId()));
            assertTrue(result.getTags().contains(
                    tag("existingTag").build()));
            assertTrue(result.getTags().contains(tag("newTag").build()));
            assertTrue(result.getLogbooks().contains(
                    logbook("existingLogbook").build()));
            assertTrue(result.getLogbooks().contains(
                    logbook("newLogbook").build()));

        } finally {
            if (returnLog != null) {
                client.deleteLogbook("TestLogbook");
                client.deleteLogbook("existingLogbook");
                client.deleteLogbook("newLogbook");
                client.deleteTag("existingTag");
                client.deleteTag("newTag");
                client.delete(returnLog.getId());
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
            if (!f.exists()) {
                FileWriter fwrite = new FileWriter(f);
                fwrite.write("This is test file");
                fwrite.flush();
                fwrite.close();
            }
            returnLog = client.update(log("attachment test").description("TestDetail").level("Info").in(logbook("TestLogbook").owner(owner)));

            client.add(f, returnLog.getId());
            assertTrue(client.getAttachments(returnLog.getId()).size() == 1);
        } finally {
            if (returnLog != null) {
                client.delete(returnLog.getId());
                            client.delete("file.txt",returnLog.getId());                         
            }
            if (f.exists()) {
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
            returnLog = client.update(log("attachment test").description("TestDetail").level("Info").in(logbook("TestLogbook").owner(owner)));

            client.add(f, returnLog.getId());
            assertTrue(client.getAttachments(returnLog.getId()).size() == 1);
        } finally {
            if (returnLog != null) {
                client.delete(returnLog.getId());
                            client.delete("the_homercar.jpg",returnLog.getId());
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
        PropertyBuilder prop = property(propertyName, propertyValue);
        try {
            client.update(logbook("TestLogbook").owner(owner));
            returnLog = client.update(log(logSubject).description("TestDetail").level("Info").in(logbook("TestLogbook").owner(owner)));
            assertTrue(!((client.getLog(returnLog.getId()).getProperties()).contains(
                    prop.build())));
            client.update(prop, returnLog.getId());
            assertTrue((client.getLog(returnLog.getId()).getProperties()).contains(prop.build()));
            client.delete(property(propertyName), returnLog.getId());
            assertTrue(!(client.getLog(returnLog.getId()).getProperties()).contains(
                    prop.build()));
        } finally {
            if (returnLog != null) {
                client.deleteLogbook("TestLogbook");
                client.delete(returnLog.getId());
                assertTrue("CleanUp failed", !client.listLogs().contains(
                        returnLog));
            }
        }
    }

    @Test
    public void getAllLogbooks() {
        String owner = "me";
        String logbookName = "TestLogbook";
        client.update(logbook(logbookName).owner(owner));
        assertTrue(client.listLogbooks().contains(logbookName));
        client.deleteLogbook(logbookName);
        assertTrue("TestLogbook clean up failed", !client.listLogbooks().contains(logbookName));
    }

    @Test
    public void getAllTags() {
        client.update(tag("TestTag"));
        assertTrue(client.listTags().contains("TestTag"));
        client.deleteTag("TestTag");
        assertTrue("TestTag clean up failed", !client.listTags().contains(
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
            if (((OlogException) item).getStatus().equals(this.status)) {
                return true;
            } else {
                return false;
            }
        }
    }
}
