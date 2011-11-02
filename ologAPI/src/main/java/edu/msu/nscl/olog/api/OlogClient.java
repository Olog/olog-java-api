package edu.msu.nscl.olog.api;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import net.coobird.thumbnailator.Thumbnails;
import org.apache.jackrabbit.webdav.DavException;

/**
 * 
 * TODO: replace the usage of Xml* types with log,tag,logbooks
 * 
 * @author Eric Berryman taken from shroffk
 * 
 */
public interface OlogClient {
	/**
	 * Get a list of all the logbooks currently existing
	 * 
	 * @return string collection of logbooks
	 */
	public Collection<String> getAllLogbooks();

	/**
	 * Get a list of all the tags currently existing
	 * 
	 * @return string collection of tags
	 */
	public Collection<String> getAllTags();

	/**
	 * Get a list of all the levels currently existing
	 * 
	 * @return string collection of levels
	 */
	public Collection<String> getAllLevels();

	/**
	 * Returns a collection of attachments that matches the logId <tt>logId</tt>
	 * 
	 * @param logId
	 *            log id
	 * @return attachments collection object
	 * @throws OlogException
	 */
	public Collection<String> getAttachments(Long logId) throws OlogException,
			DavException;

	/**
	 * Returns a log that exactly matches the logId <tt>logId</tt>
	 * 
	 * @param logId
	 *            log id
	 * @return Log object
	 * @throws OlogException
	 */
	public Log getLog(Long logId) throws OlogException;

	/**
	 * Add a single log <tt>log</tt>
	 * 
	 * @param log
	 *            the log to be added
	 * @throws OlogException
	 */
	public Log add(LogBuilder log) throws OlogException;

	/**
	 * Add a set of logs
	 * 
	 * @param logs
	 *            set of logs to be added
	 * @throws OlogException
	 */
	public Collection<Log> add(Collection<LogBuilder> logs)
			throws OlogException;

	/**
	 * Add a Tag <tt>tag</tt> with no associated logs to the database.
	 * 
	 * @param tag
	 */
	public void add(TagBuilder tag);

	/**
	 * Add Tag <tt>tag </tt> to Log with name <tt>logName</tt>
	 * 
	 * @param tag
	 *            tag builder
	 * @param logId
	 *            log id the tag to be added
	 */
	public void add(TagBuilder tag, Long logId);

	/**
	 * Add the Tag <tt>tag</tt> to the set of the logs with ids <tt>logIds</tt>
	 * 
	 * @param tag
	 *            tag builder
	 * @param logIds
	 *            collection of log ids
	 */
	public void add(TagBuilder tag, Collection<Long> logIds);

	/**
	 * Add a new logbook <tt>logbook</tt>
	 * 
	 * @param logbookBuilder
	 */
	public void add(LogbookBuilder logbookBuilder);

	/**
	 * Add Logbook <tt>logbook</tt> to the log <tt>logId</tt>
	 * 
	 * @param logbook
	 *            logbook builder
	 * @param logId
	 *            log id
	 */
	public void add(LogbookBuilder logbook, Long logId);

	/**
	 * @param logIds
	 * @param logbook
	 */
	public void add(LogbookBuilder logbook, Collection<Long> logIds);

	/**
	 * Add Property <tt>property</tt> to Log with id <tt>logId</tt>
	 * 
	 * @param property
	 *            property builder
	 * @param logId
	 *            log id the property to be added
	 */
	public void add(PropertyBuilder property, Long logId);

	/**
	 * Add the Property <tt>property</tt> to the set of the logs with ids
	 * <tt>logIds</tt>
	 * 
	 * @param property
	 *            property builder
	 * @param logIds
	 *            collection of log ids
	 */
	public void add(PropertyBuilder property, Collection<Long> logIds);

	/**
	 * @param logId
	 * @param local
	 */
	public void add(File local, Long logId);

	/**
	 * 
	 * @param pattern
	 * @return collection of Log objects
	 */
	public Collection<Log> findLogsBySearch(String pattern)
			throws OlogException;

	/**
	 * 
	 * @param pattern
	 * @return collection of Log objects
	 */
	public Collection<Log> findLogsByTag(String pattern) throws OlogException;

	/**
	 * This function is a subset of queryLogs - should it be removed??
	 * <p>
	 * TODO: add the usage of patterns and implement on top of the general query
	 * using the map
	 * 
	 * @param logbook
	 *            logbook name
	 * @return collection of Log objects
	 * @throws OlogException
	 */
	public Collection<Log> findLogsByLogbook(String logbook)
			throws OlogException;

	/**
	 * Query for logs based on the criteria specified in the map
	 * 
	 * @param map
	 * @return collection of Log objects
	 */
	public Collection<Log> findLogs(Map<String, String> map);

	/**
	 * Multivalued map used to search for a key with multiple values. e.g.
	 * logbook a=1 or logbook a=2
	 * 
	 * @param map
	 *            Multivalue map for searching a key with multiple values
	 * @return collection of Log objects
	 */
	public Collection<Log> findLogs(MultivaluedMap<String, String> map);

	/**
	 * Remove {tag} from all logs
	 * 
	 * @param tag
	 */
	public void deleteTag(String tag);

	/**
	 * 
	 * @param logbook
	 * @throws LogFinderException
	 */
	public void deleteLogbook(String logbook) throws OlogException;

	public Collection<Log> getAllLogs();

	/**
	 * Remove the log identified by <tt>log</tt>
	 * 
	 * @param log
	 *            log to be removed
	 * @throws OlogException
	 */
	public void remove(LogBuilder log) throws OlogException;

	/**
	 * Remove the log identified by <tt>log</tt>
	 * 
	 * @param logId
	 *            log id log id to be removed
	 * @throws OlogException
	 */
	public void remove(Long logId) throws OlogException;

	/**
	 * Remove the log collection identified by <tt>log</tt>
	 * 
	 * @param logs
	 *            logs to be removed
	 * @throws OlogException
	 */
	public void remove(Collection<Log> logs) throws OlogException;

	/**
	 * Remove tag <tt>tag</tt> from the log with the id <tt>logId</tt>
	 * 
	 * @param tag
	 * @param logId
	 */
	public void remove(TagBuilder tag, Long logId) throws OlogException;

	/**
	 * Remove the tag <tt>tag </tt> from all the logs <tt>logNames</tt>
	 * 
	 * @param tag
	 * @param logIds
	 * @throws OlogException
	 */
	public void remove(TagBuilder tag, Collection<Long> logIds)
			throws OlogException;

	/**
	 * Remove logbook <tt>logbook</tt> from the log with name <tt>logName</tt>
	 * 
	 * @param logbook
	 *            logbook builder
	 * @param logId
	 *            log id
	 * @throws OlogException
	 */
	public void remove(LogbookBuilder logbook, Long logId) throws OlogException;

	/**
	 * Remove the logbook <tt>logbook</tt> from the set of logs <tt>logIds</tt>
	 * 
	 * @param logbook
	 * @param logIds
	 * @throws OlogException
	 */
	public void remove(LogbookBuilder logbook, Collection<Long> logIds)
			throws OlogException;

	/**
	 * Remove property <tt>property</tt> from the log with id <tt>logId</tt>
	 * TODO: Should this be it's own service?
	 * 
	 * @param property
	 *            property builder
	 * @param logId
	 *            log id
	 * @throws OlogException
	 */
	public void remove(PropertyBuilder property, Long logId)
			throws OlogException;

	/**
	 * Remove the property <tt>property</tt> from the set of logs
	 * <tt>logIds</tt>
	 * 
	 * @param property
	 * @param logIds
	 * @throws OlogException
	 */
	public void remove(PropertyBuilder property, Collection<Long> logIds)
			throws OlogException;

	/**
	 * Remove file attachment from log <tt>logId<tt>
	 * 
	 * TODO: sardine delete hangs up, using jersey for delete
	 * 
	 * @param String
	 *            fileName
	 * @param Long
	 *            logId
	 * @throws OlogException
	 */
	public void remove(String fileName, Long logId);

	/**
	 * Update logbooks and tags of existing log <tt>log</tt>
	 * 
	 * @param log
	 * @throws OlogException
	 */
	public void updateLog(LogBuilder log) throws OlogException;

	/**
	 * Add tag <tt>tag</tt> to log <tt>logId</tt> and remove the tag from all
	 * other logs
	 * 
	 * @param tag
	 * @param logId
	 * @throws OlogException
	 */
	public void set(TagBuilder tag, Long logId) throws OlogException;

	/**
	 * Set tag <tt>tag</tt> on the set of logs {logs} and remove it from all
	 * others
	 * 
	 * @param tag
	 *            tag builder
	 * @param logIds
	 *            collection of log ids
	 */
	public void set(TagBuilder tag, Collection<Long> logIds);

}
