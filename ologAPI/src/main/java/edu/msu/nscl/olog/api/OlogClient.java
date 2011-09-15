package edu.msu.nscl.olog.api;

import static edu.msu.nscl.olog.api.LogBuilder.*;
import static edu.msu.nscl.olog.api.PropertyBuilder.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import com.googlecode.sardine.*;
import java.util.List;

/**
 * TODO: make this not a singleton. Add a constructor to programmatically pass
 * the configuration.
 * 
 * TODO: replace the usage of Xml* types with log,tag,logbooks
 * 
 * @author Eric Berryman taken from shroffk
 * 
 */
/**
 * @author Eric Berryman taken from shroffk
 * 
 */
public class OlogClient {
	private static OlogClient instance;
	private WebResource service;
    private Sardine sardine;
	private OlogProperties properties;
    

	/**
	 * Create an instance of OlogClient
	 */
	private OlogClient() {
		
		properties = new OlogProperties();

		// Authentication and Authorization configuration
		TrustManager mytm[] = null;
		SSLContext ctx = null;

		try {
			// System.out.println(this.getClass()
			// .getResource("/config/truststore.jks").getPath());
			// mytm = new TrustManager[] { new MyX509TrustManager(
			// getPreferenceValue("trustStore", this.getClass()
			// .getResource("/config/truststore.jks").getPath()),
			//					getPreferenceValue("trustPass", "default").toCharArray()) }; //$NON-NLS-1$
			mytm = new TrustManager[] { new DummyX509TrustManager() };
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			ctx = SSLContext.getInstance(properties.getPreferenceValue("protocol", "SSL")); //$NON-NLS-1$
			ctx.init(null, mytm, null);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}

		ClientConfig config = new DefaultClientConfig();
		config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
				new HTTPSProperties(null, ctx));
		Client client = Client.create(config);
                client.addFilter(new HTTPBasicAuthFilter(properties.getPreferenceValue("username",
				"username"), properties.getPreferenceValue("password", "password"))); //$NON-NLS-1$ //$NON-NLS-2$

		// Logging filter - raw request and response printed to sys.o
		if (properties.getPreferenceValue("raw_html_logging", "off").equals("on")) { //$NON-NLS-1$ //$NON-NLS-2$
			client.addFilter(new LoggingFilter());
		}
		service = client.resource(getBaseURI());
                sardine = SardineFactory.begin(properties.getPreferenceValue("username","username"),
                		properties.getPreferenceValue("password", "password"));
	}
       /**
	 * Create an instance of OlogClient
	 */
	private OlogClient(String username, String password) {
		
		properties = new OlogProperties();	

		// Authentication and Authorization configuration
		TrustManager mytm[] = null;
		SSLContext ctx = null;

		try {
			// System.out.println(this.getClass()
			// .getResource("/config/truststore.jks").getPath());
			// mytm = new TrustManager[] { new MyX509TrustManager(
			// getPreferenceValue("trustStore", this.getClass()
			// .getResource("/config/truststore.jks").getPath()),
			//					getPreferenceValue("trustPass", "default").toCharArray()) }; //$NON-NLS-1$
			mytm = new TrustManager[] { new DummyX509TrustManager() };
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			ctx = SSLContext.getInstance(properties.getPreferenceValue("protocol", "SSL")); //$NON-NLS-1$
			ctx.init(null, mytm, null);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}

		ClientConfig config = new DefaultClientConfig();
		config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
				new HTTPSProperties(null, ctx));
		Client client = Client.create(config);

                client.addFilter(new HTTPBasicAuthFilter(username, password)); //$NON-NLS-1$ //$NON-NLS-2$

		// Logging filter - raw request and response printed to sys.o
		if (properties.getPreferenceValue("raw_html_logging", "off").equals("on")) { //$NON-NLS-1$ //$NON-NLS-2$
			client.addFilter(new LoggingFilter());
		}
		service = client.resource(getBaseURI());
                sardine = SardineFactory.begin(username, password);
	}

	/**
	 * Get a list of all the logbooks currently existing
	 * 
	 * @return string collection of logbooks
	 */
	public Collection<String> getAllLogbooks() {
		Collection<String> allLogbooks = new HashSet<String>();
		try {
			XmlLogbooks allXmlLogbooks = service.path("logbooks").accept(
					MediaType.APPLICATION_XML).get(XmlLogbooks.class);
			for (XmlLogbook xmlLogbook : allXmlLogbooks.getLogbooks()) {
				allLogbooks.add(xmlLogbook.getName());
			}
			return allLogbooks;
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}
	}

	/**
	 * Get a list of all the tags currently existing
	 * 
	 * @return string collection of tags
	 */
	public Collection<String> getAllTags() {
		Collection<String> allTags = new HashSet<String>();
		try {
			XmlTags allXmlTags = service.path("tags").accept(
					MediaType.APPLICATION_XML).get(XmlTags.class);
			for (XmlTag xmlTag : allXmlTags.getTags()) {
				allTags.add(xmlTag.getName());
			}
			return allTags;
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}
	}
        	/**
	 * Get a list of all the levels currently existing
	 *
	 * @return string collection of levels
	 */
	public Collection<String> getAllLevels() {
		Collection<String> allLevels = new HashSet<String>();
		try {
			XmlLevels allXmlLevels = service.path("levels").accept(
					MediaType.APPLICATION_XML).get(XmlLevels.class);
			for (XmlLevel xmlLevel : allXmlLevels.getLevels()) {
				allLevels.add(xmlLevel.getName());
			}
			return allLevels;
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}
	}

        /**
	 * Returns the (singleton) instance of OlogClient
	 *
	 * @return the instance of OlogClient
	 */
	public static OlogClient getInstance() {
                instance = new OlogClient();
		return instance;
	}

        /**
	 * Returns the (singleton) instance of OlogClient
	 *
	 * @return the instance of OlogClient
	 */
	public static OlogClient getInstance(String username, String password) {
		instance = new OlogClient(username, password);
                return instance;
	}
	
	private URI getBaseURI() {
		return UriBuilder.fromUri(
				properties.getPreferenceValue("olog_url", null)).build(); //$NON-NLS-1$
	}
    
	private URI getJCRBaseURI() {
		return UriBuilder.fromUri(
				properties.getPreferenceValue("olog_jcr_url", null)).build(); //$NON-NLS-1$
	}

        /**
	 * Returns a collection of attachments that matches the logId
	 * <tt>logId</tt>
	 *
	 * @param logId log id
	 * @return attachments collection object
	 * @throws OlogException
	 */
	public Collection<String> getAttachments(Long logId) throws OlogException {
                Collection<String> allFiles = new HashSet<String>();
		try {
			URI remote = UriBuilder.fromUri(getJCRBaseURI()).path("{arg1}/").build(logId);
                        List<DavResource> resources = sardine.list(remote.toASCIIString());
                        for (DavResource file : resources) {
                            if (!file.isDirectory())
                                allFiles.add(file.getHref().toASCIIString());
                        }
                        return allFiles;
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
                } catch (IOException e) {
			throw new OlogException(e);
		}
	}

	/**
	 * Returns a log that exactly matches the logId
	 * <tt>logId</tt>
	 * 
	 * @param logId log id
	 * @return Log object
	 * @throws OlogException
	 */
	public Log getLog(Long logId) throws OlogException {
		try {
			return new Log(service.path("logs").path(logId.toString()).accept( //$NON-NLS-1$
							MediaType.APPLICATION_XML).get(XmlLog.class));
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}
	}

	/**
	 * Add a single log <tt>log</tt>
	 * 
	 * @param log
	 *            the log to be added
	 * @throws OlogException
	 */
	public Log add(LogBuilder log) throws OlogException {
		try {
                        ClientResponse incResponse;
                        XmlLogs xmlLogs = new XmlLogs();
                        xmlLogs.addXmlLog(log.toXml());

                        if(log.toXml().getId()!=null){
                            //throw new OlogException();
                            // TODO: Fail?  use UpdateLog instead?
                            // service logs/id does not reply with inserted Log
                            // this doesn't seem right to just return the object given
                            service.path("logs").path(log.toXml().getId().toString()).accept( //$NON-NLS-1$
                                        MediaType.APPLICATION_XML).type( //$NON-NLS-1$
					MediaType.APPLICATION_XML).put(ClientResponse.class,log.toXml());
                            return log.build();
                        } else {
                            incResponse = service.path("logs").accept( //$NON-NLS-1$
                                        MediaType.APPLICATION_XML).type( //$NON-NLS-1$
					MediaType.APPLICATION_XML).post(ClientResponse.class,xmlLogs);
                        }
                        XmlLogs response = incResponse.getEntity(XmlLogs.class);
                        return new Log(response.getLogs().iterator().next());
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}
	}

	/**
	 * Add a set of logs
	 * 
	 * @param logs
	 *            set of logs to be added
	 * @throws OlogException
	 */
	public Collection<Log> add(Collection<LogBuilder> logs) throws OlogException {
		try {
			XmlLogs xmlLogs = new XmlLogs();
                        Collection<Log> returnLogs = new HashSet<Log>();

			for (LogBuilder log : logs) {
				xmlLogs.addXmlLog(log.toXml());
			}
			ClientResponse incResponse = service.path("logs").accept( //$NON-NLS-1$
                                        MediaType.APPLICATION_XML).type( //$NON-NLS-1$
                                        MediaType.APPLICATION_XML).post(ClientResponse.class,xmlLogs);
                        XmlLogs response = incResponse.getEntity(XmlLogs.class);
                        for (XmlLog xmllog : response.getLogs()) {
				returnLogs.add(new Log(xmllog));
			}
			return Collections.unmodifiableCollection(returnLogs);
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}
	}

	/**
	 * Add a Tag <tt>tag</tt> with no associated logs to the database.
	 * 
	 * @param tag
	 */
	public void add(TagBuilder tag) {
		try {
			XmlTag xmlTag = tag.toXml();
			service.path("tags").path(xmlTag.getName()).accept(
					MediaType.APPLICATION_XML).accept(
					MediaType.APPLICATION_JSON).put(xmlTag);
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}
	}

	/**
	 * Add Tag <tt>tag </tt> to Log with name <tt>logName</tt>
	 * 
	 * @param tag tag builder
	 * @param logId log id
	 *            the tag to be added
	 */
	public void add(TagBuilder tag, Long logId) {
		Log log = getLog(logId);
		if (log != null) {
			updateLog(log(log).with(tag));
		}
	}

	/**
	 * Add the Tag <tt>tag</tt> to the set of the logs with ids
	 * <tt>logIds</tt>
	 * 
	 * @param tag tag builder
	 * @param logIds collection of log ids
	 */
	public void add(TagBuilder tag, Collection<Long> logIds) {
		for (Long logId : logIds) {
			add(tag, logId);
		}
	}

	/**
	 * Add a new logbook <tt>logbook</tt>
	 * 
	 * @param logbookBuilder
	 */
	public void add(LogbookBuilder logbookBuilder) {
		try {
			XmlLogbook logbook = logbookBuilder.toXml();
			service.path("logbooks").path(logbook.getName()).accept(
					MediaType.APPLICATION_XML).accept(
					MediaType.APPLICATION_JSON).put(logbook);
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}
	}

	/**
	 * Add Logbook <tt>logbook</tt> to the log <tt>logId</tt>
	 * 
	 * @param logbook logbook builder
	 * @param logId log id
	 */
	public void add(LogbookBuilder logbook, Long logId) {
		Log log = getLog(logId);
		if (log != null) {
			updateLog(log(log).in(logbook));
		}
	}

	/**
	 * @param logIds
	 * @param logbook
	 */
	public void add(LogbookBuilder logbook, Collection<Long> logIds) {
		for (Long logId : logIds) {
			add(logbook, logId);
		}
	}

	/**
	 * Add Property <tt>property</tt> to Log with id <tt>logId</tt>
	 *
	 * @param property property builder
	 * @param logId log id
	 *            the property to be added
	 */
	public void add(PropertyBuilder property, Long logId) {
		Log log = getLog(logId);
		if (log != null) {
			updateLog(log(log).property(property));
		}
	}

	/**
	 * Add the Property <tt>property</tt> to the set of the logs with ids
	 * <tt>logIds</tt>
	 *
	 * @param property property builder
	 * @param logIds collection of log ids
	 */
	public void add(PropertyBuilder property, Collection<Long> logIds) {
		for (Long logId : logIds) {
			add(property, logId);
		}
	}

        /**
         * @param logId
         * @param local
         */
        public void add(File local, Long logId) {
                URI remote = UriBuilder.fromUri(getJCRBaseURI()).path("{arg1}").path("{arg2}").build(logId,local.getName());
                URI remoteDir = UriBuilder.fromUri(getJCRBaseURI()).path("{arg1}").build(logId);
                final int ndx = local.getName().lastIndexOf(".");
        		final String extension = local.getName().substring(ndx + 1);
                try {
                    
                        if(!sardine.exists(remoteDir.toASCIIString()))
                            sardine.createDirectory(remoteDir.toASCIIString());
                    } catch (IOException ex) {
                            throw new OlogException(ex);
                    }
                try {
                        FileInputStream fis = new FileInputStream(local);                      
                        sardine.put(remote.toASCIIString(), fis);
                		// If image add thumbnail
                		if ((extension.equals("jpeg") ||
                				extension.equals("jpg") ||
                				extension.equals("gif") ||
                				extension.equals("png") )){
                			//BufferedImage img = new BufferedImage(80, 80, BufferedImage.TYPE_INT_RGB);
                			//img.createGraphics().drawImage(ImageIO.read(local).getScaledInstance(100, 100, BufferedImage.SCALE_SMOOTH),0,0,null);
                			//ImageIO.write(img, "jpg", new File(local.getName()));
                			//FileInputStream fis2 = new FileInputStream(new File(local.getName()));                      
                			//sardine.put(remote.toASCIIString(), fis2);
                		}
                        try {
                            fis.close();
                        } catch (IOException e) {
                            throw new OlogException(e);
                        }
                } catch (IOException e) {
                        throw new OlogException(e);
                }
        }

	/**
	 * 
	 * @param pattern
	 * @return collection of Log objects
	 */
	public Collection<Log> findLogsBySearch(String pattern)
			throws OlogException {
		try {
			Collection<Log> logs = new HashSet<Log>();
			XmlLogs xmlLogs = service
					.path("logs").queryParam("search", pattern).accept( //$NON-NLS-1$ //$NON-NLS-2$
							MediaType.APPLICATION_XML).accept(
							MediaType.APPLICATION_JSON).get(XmlLogs.class);
			for (XmlLog xmllog : xmlLogs.getLogs()) {
				logs.add(new Log(xmllog));
			}
			return Collections.unmodifiableCollection(logs);
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}
	}

	/**
	 * 
	 * @param pattern
	 * @return collection of Log objects
	 */
	public Collection<Log> findLogsByTag(String pattern)
			throws OlogException {
		try {
			Collection<Log> logs = new HashSet<Log>();
			XmlLogs xmlLogs = service
					.path("logs").queryParam("tag", pattern).accept( //$NON-NLS-1$ //$NON-NLS-2$
							MediaType.APPLICATION_XML).accept(
							MediaType.APPLICATION_JSON).get(XmlLogs.class);
			for (XmlLog xmllog : xmlLogs.getLogs()) {
				logs.add(new Log(xmllog));
			}
			return Collections.unmodifiableCollection(logs);

		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}
	}

	/**
	 * This function is a subset of queryLogs - should it be removed??
	 * <p>
	 * TODO: add the usage of patterns and implement on top of the general query
	 * using the map
	 * 
	 * @param logbook logbook name
	 * @return collection of Log objects
	 * @throws OlogException
	 */
	public Collection<Log> findLogsByLogbook(String logbook)
                               throws OlogException {
		try {
			Collection<Log> logs = new HashSet<Log>();
			XmlLogs xmlLogs = service
					.path("logs").queryParam("logbook", logbook).accept( //$NON-NLS-1$ //$NON-NLS-2$
							MediaType.APPLICATION_XML).accept(
							MediaType.APPLICATION_JSON).get(XmlLogs.class);
			for (XmlLog xmllog : xmlLogs.getLogs()) {
				logs.add(new Log(xmllog));
			}
			return Collections.unmodifiableCollection(logs);
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}
	}

	/**
	 * Query for logs based on the criteria specified in the map
	 * 
	 * @param map
	 * @return collection of Log objects
	 */
	public Collection<Log> findLogs(Map<String, String> map) {
		MultivaluedMapImpl mMap = new MultivaluedMapImpl();
		Iterator<Map.Entry<String, String>> itr = map.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<String, String> entry = itr.next();
			mMap
					.put(entry.getKey(), Arrays.asList(entry.getValue().split(
							",")));
		}
		return findLogs(mMap);
	}

	/**
	 * Multivalued map used to search for a key with multiple values. e.g.
	 * logbook a=1 or logbook a=2
	 * 
	 * @param map
	 *            Multivalue map for searching a key with multiple values
	 * @return collection of Log objects
	 */
	public Collection<Log> findLogs(MultivaluedMapImpl map) {
		Collection<Log> logs = new HashSet<Log>();
		XmlLogs xmlLogs = service.path("logs").queryParams(map)
				.accept(MediaType.APPLICATION_XML).accept(
						MediaType.APPLICATION_JSON).get(XmlLogs.class);
		for (XmlLog xmllog : xmlLogs.getLogs()) {
			logs.add(new Log(xmllog));
		}
		return Collections.unmodifiableCollection(logs);
	}

	/**
	 * Remove {tag} from all logs
	 * 
	 * @param tag
	 */
	public void deleteTag(String tag) {
		try {
			service.path("tags").path(tag).accept(MediaType.APPLICATION_XML) //$NON-NLS-1$
					.delete();
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}
	}

	/**
	 * 
	 * @param logbook
	 * @throws LogFinderException
	 */
	public void deleteLogbook(String logbook) throws OlogException {
		try {
			service.path("logbooks").path(logbook).accept(
					MediaType.APPLICATION_XML).accept(
					MediaType.APPLICATION_JSON).delete();
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}
	}

	public Collection<Log> getAllLogs() {
		try {
			XmlLogs logs = service.path("logs").accept( //$NON-NLS-1$
					MediaType.APPLICATION_XML).get(XmlLogs.class);
			Collection<Log> set = new HashSet<Log>();
			for (XmlLog log : logs.getLogs()) {
				set.add(new Log(log));
			}
			return set;
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}
	}

	/**
	 * Remove the log identified by <tt>log</tt>
	 * 
	 * @param log
	 *            log to be removed
	 * @throws OlogException
	 */
	public void remove(LogBuilder log) throws OlogException {
		try {
			service.path("logs").path(log.toXml().getId().toString()).delete(); //$NON-NLS-1$
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}
	}
        /**
	 * Remove the log identified by <tt>log</tt>
	 *
	 * @param logId log id
	 *            log id to be removed
	 * @throws OlogException
	 */
	public void remove(Long logId) throws OlogException {
		try {
			service.path("logs").path(logId.toString()).delete(); //$NON-NLS-1$
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}
	}
	/**
	 * Remove the log collection identified by <tt>log</tt>
	 *
	 * @param logs
	 *            logs to be removed
	 * @throws OlogException
	 */
	public void remove(Collection<Log> logs) throws OlogException {
		try {
                        for ( Log log : logs) {
                            service.path("logs").path(log.getId().toString()).delete(); //$NON-NLS-1$
                        }
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}
	}
	/**
	 * Remove tag <tt>tag</tt> from the log with the id
	 * <tt>logId</tt>
	 * 
	 * @param tag
	 * @param logId
	 */
	public void remove(TagBuilder tag, Long logId)
			throws OlogException {
		try {
			service.path("tags").path(tag.toXml().getName()).path(logId.toString()).accept( //$NON-NLS-1$
							MediaType.APPLICATION_XML).delete();
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}
	}

	/**
	 * Remove the tag <tt>tag </tt> from all the logs <tt>logNames</tt>
	 * 
	 * @param tag
	 * @param logIds
	 * @throws OlogException
	 */
	public void remove(TagBuilder tag, Collection<Long> logIds)
			throws OlogException {
		// TODO optimize using the /tags/<name> payload with list of logs
		for (Long logId : logIds) {
			remove(tag, logId);
		}
	}

	/**
	 * Remove logbook <tt>logbook</tt> from the log with name
	 * <tt>logName</tt>
	 * 
	 * @param logbook logbook builder
	 * @param logId log id
	 * @throws OlogException
	 */
	public void remove(LogbookBuilder logbook, Long logId)
			throws OlogException {
		try {
			service.path("logbooks").path(logbook.toXml().getName()).path(
					logId.toString()).accept(MediaType.APPLICATION_XML).accept(
					MediaType.APPLICATION_JSON).delete();
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}
	}

	/**
	 * Remove the logbook <tt>logbook</tt> from the set of logs
	 * <tt>logIds</tt>
	 * 
	 * @param logbook
	 * @param logIds
	 * @throws OlogException
	 */
	public void remove(LogbookBuilder logbook,
			Collection<Long> logIds) throws OlogException {
		for (Long log : logIds) {
			remove(logbook, log);
		}
	}

	/**
	 * Remove property <tt>property</tt> from the log with id
	 * <tt>logId</tt>
	 * TODO:  Should this be it's own service?
	 * @param property property builder
	 * @param logId log id
	 * @throws OlogException
	 */
	public void remove(PropertyBuilder property, Long logId)
			throws OlogException {
                Log log = getLog(logId);
                XmlLog xmlLog = log(log).toXml();
                XmlProperties props = new XmlProperties();
                for(Property prop : log.getProperties()){
                    if(!prop.getName().equals(property.toXml().getName())){
                        props.addXmlProperty(property(prop).toXml());
                    }
                }
                xmlLog.setXmlProperties(props);
		if (log != null) {
			add(log(new Log(xmlLog)));
		}
		//try {
		//	service.path("logs").path(logId.toString()).path(property.toXml().getName())
		//			.accept(MediaType.APPLICATION_XML).accept(
		//			MediaType.APPLICATION_JSON).delete();
		//} catch (UniformInterfaceException e) {
		//	throw new OlogException(e);
		//}
	}

	/**
	 * Remove the property <tt>property</tt> from the set of logs
	 * <tt>logIds</tt>
	 *
	 * @param property
	 * @param logIds
	 * @throws OlogException
	 */
	public void remove(PropertyBuilder property,
			Collection<Long> logIds) throws OlogException {
		for (Long log : logIds) {
			remove(property, log);
		}
	}
        /**
         *  Remove file attachment from log
         * <tt>logId<tt>
         *
         * TODO: sardine delete hangs up, using jersey for delete
         *
         * @param String fileName
         * @param Long logId
         * @throws OlogException
         */
        public void remove(String fileName,Long logId){
		try {
			URI remote = UriBuilder.fromUri(getJCRBaseURI())
                                .path("{arg1}")
                                .path("{arg2}")
                                .build(logId,fileName);
                        service.uri(remote).accept(MediaType.APPLICATION_XML).accept(
					MediaType.APPLICATION_JSON).delete();
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}

        }

	/**
	 * Update logbooks and tags of existing log <tt>log</tt>
	 * 
	 * @param log
	 * @throws OlogException
	 */
	public void updateLog(LogBuilder log)
			throws OlogException {
		try {
			service.path("logs").path(log.toXml().getId().toString()).type( //$NON-NLS-1$
					MediaType.APPLICATION_XML).post(log.toXml());
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}
	}

	/**
	 * Add tag <tt>tag</tt> to log <tt>logId</tt> and remove the tag
	 * from all other logs
	 * 
	 * @param tag
	 * @param logId
	 * @throws OlogException
	 */
	public void set(TagBuilder tag, Long logId)
			throws OlogException {
		try {
                    	 Collection<Long> logs = new ArrayList<Long>();
			 logs.add(logId);
			 set(tag, logs);
			// service.path("tags").path(tag.toXml().getName()).path(logId.toString())
			// .type(MediaType.APPLICATION_XML).put(tag.toXml());
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}

	}

	/**
	 * Set tag <tt>tag</tt> on the set of logs {logs} and remove it from
	 * all others
	 * 
	 * @param tag tag builder
	 * @param logIds collection of log ids
	 */
	public void set(TagBuilder tag, Collection<Long> logIds) {
		// Better than recursively calling set(tag, log) for each log
		try {
			XmlTag xmlTag = tag.toXml();
			XmlLogs logs = new XmlLogs();
			LogBuilder log = null;
			for (Long logId : logIds) {
				log = log(getLog(logId));
				logs.addXmlLog(log.toXml());
			}
			xmlTag.setXmlLogs(logs);
			service.path("tags").path(tag.toXml().getName()).accept(
					MediaType.APPLICATION_XML).put(xmlTag);
		} catch (UniformInterfaceException e) {
			throw new OlogException(e);
		}
	}

}
