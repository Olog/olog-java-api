package edu.msu.nscl.olog.api;

import java.io.File;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.jackrabbit.webdav.DavException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import static edu.msu.nscl.olog.api.TagBuilder.*;
import static edu.msu.nscl.olog.api.LogbookBuilder.*;
import static edu.msu.nscl.olog.api.LogBuilder.*;

/**
 * 
 * 
 * @author Eric Berryman taken from shroffk
 * 
 */
public class OlogClientImpl implements OlogClient {
	private final WebResource service;
	private final HttpClient webdav;
	private final ExecutorService executor;
	private final URI ologJCRBaseURI;

	/**
	 * Builder Class to help create a olog client.
	 * 
	 * @author shroffk
	 * 
	 */
	public static class OlogClientBuilder {
		// required
		private URI ologURI = null;

		private URI ologJCRURI;

		// optional
		private boolean withHTTPAuthentication = false;

		private ClientConfig clientConfig = null;
		private TrustManager[] trustManager = new TrustManager[] { new DummyX509TrustManager() };;
		@SuppressWarnings("unused")
		private SSLContext sslContext = null;

		private String protocol = null;
		private String username = null;
		private String password = null;

		private ExecutorService executor = Executors.newSingleThreadExecutor();

		private OlogProperties properties = new OlogProperties();

		private static final String DEFAULT_OLOG_URL = "http://localhost:8080/Olog/resources"; //$NON-NLS-1$
		private static final String DEFAULT_OLOG_JCR_URL = "http://localhost:8080/Olog/repository";

		private OlogClientBuilder() {
			this.ologURI = URI.create(this.properties.getPreferenceValue(
					"olog_url", DEFAULT_OLOG_URL));
			this.ologJCRURI = URI.create(this.properties.getPreferenceValue(
					"olog_jcr_url", DEFAULT_OLOG_JCR_URL));
			this.protocol = this.ologURI.getScheme();
		}

		private OlogClientBuilder(URI uri) {
			this.ologURI = uri;
			this.protocol = this.ologURI.getScheme();
		}

		/**
		 * Creates a {@link OlogClientBuilder} for a CF client to Default URL in
		 * the channelfinder.properties.
		 * 
		 * @return
		 */
		public static OlogClientBuilder serviceURL() {
			return new OlogClientBuilder();
		}

		/**
		 * Creates a {@link OlogClientBuilder} for a CF client to URI
		 * <tt>uri</tt>.
		 * 
		 * @param uri
		 * @return {@link OlogClientBuilder}
		 */
		public static OlogClientBuilder serviceURL(String uri) {
			return new OlogClientBuilder(URI.create(uri));
		}

		/**
		 * Creates a {@link OlogClientBuilder} for a CF client to {@link URI}
		 * <tt>uri</tt>.
		 * 
		 * @param uri
		 * @return {@link OlogClientBuilder}
		 */
		public static OlogClientBuilder serviceURL(URI uri) {
			return new OlogClientBuilder(uri);
		}

		/**
		 * Set the jcr url to be used for the attachment repository.
		 * 
		 * @param username
		 * @return {@link OlogClientBuilder}
		 */
		public OlogClientBuilder jcrURI(URI jcrURI) {
			this.ologJCRURI = jcrURI;
			return this;
		}

		/**
		 * Set the jcr url to be used for the attachment repository.
		 * 
		 * @param username
		 * @return {@link OlogClientBuilder}
		 */
		public OlogClientBuilder jcrURI(String jcrURI) {
			this.ologJCRURI = UriBuilder.fromUri(jcrURI).build();
			return this;
		}

		/**
		 * Enable of Disable the HTTP authentication on the client connection.
		 * 
		 * @param withHTTPAuthentication
		 * @return {@link OlogClientBuilder}
		 */
		public OlogClientBuilder withHTTPAuthentication(
				boolean withHTTPAuthentication) {
			this.withHTTPAuthentication = withHTTPAuthentication;
			return this;
		}

		/**
		 * Set the username to be used for HTTP Authentication.
		 * 
		 * @param username
		 * @return {@link OlogClientBuilder}
		 */
		public OlogClientBuilder username(String username) {
			this.username = username;
			return this;
		}

		/**
		 * Set the password to be used for the HTTP Authentication.
		 * 
		 * @param password
		 * @return {@link OlogClientBuilder}
		 */
		public OlogClientBuilder password(String password) {
			this.password = password;
			return this;
		}

		/**
		 * set the {@link ClientConfig} to be used while creating the
		 * channelfinder client connection.
		 * 
		 * @param clientConfig
		 * @return {@link OlogClientBuilder}
		 */
		public OlogClientBuilder withClientConfig(ClientConfig clientConfig) {
			this.clientConfig = clientConfig;
			return this;
		}

		@SuppressWarnings("unused")
		private OlogClientBuilder withSSLContext(SSLContext sslContext) {
			this.sslContext = sslContext;
			return this;
		}

		/**
		 * Set the trustManager that should be used for authentication.
		 * 
		 * @param trustManager
		 * @return {@link OlogClientBuilder}
		 */
		public OlogClientBuilder withTrustManager(TrustManager[] trustManager) {
			this.trustManager = trustManager;
			return this;
		}

		/**
		 * Provide your own executor on which the queries are to be made. <br>
		 * By default a single threaded executor is used.
		 * 
		 * @param executor
		 * @return {@link OlogClientBuilder}
		 */
		public OlogClientBuilder withExecutor(ExecutorService executor) {
			this.executor = executor;
			return this;
		}

		public OlogClientImpl create() {
			if (this.protocol.equalsIgnoreCase("http")) { //$NON-NLS-1$
				this.clientConfig = new DefaultClientConfig();
			} else if (this.protocol.equalsIgnoreCase("https")) { //$NON-NLS-1$
				if (this.clientConfig == null) {
					SSLContext sslContext = null;
					try {
						sslContext = SSLContext.getInstance("SSL"); //$NON-NLS-1$
						sslContext.init(null, this.trustManager, null);
					} catch (NoSuchAlgorithmException e) {
						throw new OlogException();
					} catch (KeyManagementException e) {
						throw new OlogException();
					}
					this.clientConfig = new DefaultClientConfig();
					this.clientConfig.getProperties().put(
							HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
							new HTTPSProperties(new HostnameVerifier() {
								@Override
								public boolean verify(String hostname,
										SSLSession session) {
									return true;
								}
							}, sslContext));
				}
				this.username = ifNullReturnPreferenceValue(this.username,
						"username", "username");
				this.password = ifNullReturnPreferenceValue(this.password,
						"password", "password");
			}
			return new OlogClientImpl(this.ologURI, this.ologJCRURI,
					this.clientConfig, this.withHTTPAuthentication,
					this.username, this.password, this.executor);
		}

		private String ifNullReturnPreferenceValue(String value, String key,
				String Default) {
			if (value == null) {
				return this.properties.getPreferenceValue(key, Default);
			} else {
				return value;
			}
		}

	}

	private OlogClientImpl(URI ologURI, URI ologJCRURI, ClientConfig config,
			boolean withHTTPBasicAuthFilter, String username, String password,
			ExecutorService executor) {
		this.ologJCRBaseURI = ologJCRURI;
		this.executor = executor;
		Client client = Client.create(config);
		if (withHTTPBasicAuthFilter) {
			client.addFilter(new HTTPBasicAuthFilter(username, password));
		}
		client.addFilter(new RawLoggingFilter(Logger
				.getLogger(OlogClientImpl.class.getName())));
		service = client.resource(UriBuilder.fromUri(ologURI).build());

		ApacheHttpClient client2Apache = ApacheHttpClient.create(config);
		webdav = client2Apache.getClientHandler().getHttpClient();
		webdav.getHostConfiguration().setHost(getJCRBaseURI().getHost(), 8181);
		Credentials credentials = new UsernamePasswordCredentials(username,
				password);
		webdav.getState().setCredentials(AuthScope.ANY, credentials);
		webdav.getParams().setAuthenticationPreemptive(true);
	}

	private URI getJCRBaseURI() {
		return this.ologJCRBaseURI;
	}

	@Override
	public Collection<Logbook> listLogbooks() throws OlogException {
		return wrappedSubmit(new Callable<Collection<Logbook>>() {

			@Override
			public Collection<Logbook> call() throws Exception {

				Collection<Logbook> allLogbooks = new HashSet<Logbook>();
				XmlLogbooks allXmlLogbooks = service.path("logbooks")
						.accept(MediaType.APPLICATION_XML)
						.get(XmlLogbooks.class);
				for (XmlLogbook xmlLogbook : allXmlLogbooks.getLogbooks()) {
					allLogbooks.add(new Logbook(xmlLogbook));
				}
				return allLogbooks;
			}

		});
	}

	@Override
	public Collection<Tag> listTags() throws OlogException {
		return wrappedSubmit(new Callable<Collection<Tag>>() {

			@Override
			public Collection<Tag> call() throws Exception {
				Collection<Tag> allTags = new HashSet<Tag>();
				XmlTags allXmlTags = service.path("tags")
						.accept(MediaType.APPLICATION_XML).get(XmlTags.class);
				for (XmlTag xmlTag : allXmlTags.getTags()) {
					allTags.add(new Tag(xmlTag));
				}
				return allTags;
			}

		});
	}

	@SuppressWarnings("deprecation")
	@Override
	public Collection<Level> listLevels() throws OlogException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Log> listLogs() {
		return wrappedSubmit(new Callable<Collection<Log>>() {
			@Override
			public Collection<Log> call() throws Exception {
				XmlLogs xmlLogs = service.path("logs")
						.accept(MediaType.APPLICATION_XML)
						.accept(MediaType.APPLICATION_JSON).get(XmlLogs.class);
				return LogUtil.toLogs(xmlLogs);
			}
		});
	}

	@Override
	public Log getLog(Long logId) throws OlogException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getAttachments(Long logId) throws OlogException,
			DavException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Log set(LogBuilder log) throws OlogException {
		Collection<Log> result = wrappedSubmit(new SetLogs(log));
		if(result.size() == 1){
			return result.iterator().next();
		} else {
			throw new OlogException();
		}
	}

	@Override
	public Collection<Log> set(Collection<LogBuilder> logs) throws OlogException {
		return wrappedSubmit(new SetLogs(logs));
	}

	private class SetLogs implements Callable<Collection<Log>> {
		private Collection<LogBuilder> logs;

		public SetLogs(LogBuilder log) {
			this.logs = new ArrayList<LogBuilder>();
			this.logs.add(log);
		}

		public SetLogs(Collection<LogBuilder> logs) {
			this.logs = new ArrayList<LogBuilder>(logs);
		}

		@Override
		public Collection<Log> call() throws Exception {
			XmlLogs xmlLogs = new XmlLogs();
			for (LogBuilder log : logs) {
				xmlLogs.getLogs().add(log.toXml());
			}
			ClientResponse response = service.path("logs")
					.accept(MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_JSON)
					.post(ClientResponse.class, xmlLogs);
			XmlLogs responseLogs = response.getEntity(XmlLogs.class);
			Collection<Log> returnLogs = new HashSet<Log>();
			for (XmlLog xmllog : responseLogs.getLogs()) {
				returnLogs .add(new Log(xmllog));
			}
			return Collections.unmodifiableCollection(returnLogs);
		}
	}

	@Override
	public void set(TagBuilder tag) throws OlogException {
		wrappedSubmit(new SetTag(tag));

	}

	@Override
	public void set(TagBuilder tag, Long logId) throws OlogException {
		// TODO Auto-generated method stub

	}

	@Override
	public void set(TagBuilder tag, Collection<Long> logIds)
			throws OlogException {
		// TODO Auto-generated method stub
	}

	private class SetTag implements Runnable {
		private final TagBuilder tag;

		public SetTag(TagBuilder tag) {
			this.tag = tag(tag.build());
		}

		@Override
		public void run() {
			XmlTag xmlTag = tag.toXml();
			service.path("tags").path(tag.toXml().getName())
					.accept(MediaType.APPLICATION_XML).put(xmlTag);
		}
	}

	@Override
	public void set(LogbookBuilder logbook) throws OlogException {
		wrappedSubmit(new SetLogBook(logbook));
	}

	@Override
	public void set(LogbookBuilder logbook, Long logId) throws OlogException {
		// TODO Auto-generated method stub

	}

	@Override
	public void set(LogbookBuilder logbook, Collection<Long> logIds)
			throws OlogException {
		// TODO Auto-generated method stub

	}

	private class SetLogBook implements Runnable {
		private final LogbookBuilder logbook;

		SetLogBook(LogbookBuilder logbook) {
			this.logbook = logbook;
		}

		@Override
		public void run() {
			XmlLogbook xmlLogbook = logbook.toXml();
			service.path("logbooks").path(xmlLogbook.getName())
					.accept(MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_JSON).put(xmlLogbook);
		}

	}

	@Override
	public Log update(LogBuilder log) throws OlogException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Log> update(Collection<LogBuilder> logs)
			throws OlogException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(TagBuilder tag) throws OlogException {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(TagBuilder tag, Long logId) throws OlogException {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(TagBuilder tag, Collection<Long> logIds)
			throws OlogException {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(LogbookBuilder logbookBuilder) throws OlogException {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(LogbookBuilder logbook, Long logId) throws OlogException {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(LogbookBuilder logbook, Collection<Long> logIds)
			throws OlogException {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(PropertyBuilder property, Long logId)
			throws OlogException {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(PropertyBuilder property, Collection<Long> logIds)
			throws OlogException {
		// TODO Auto-generated method stub

	}

	@Override
	public void add(File local, Long logId) throws OlogException {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<Log> findLogsBySearch(String pattern)
			throws OlogException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Log> findLogsByTag(String pattern) throws OlogException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Log> findLogsByLogbook(String logbook)
			throws OlogException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Log> findLogs(Map<String, String> map)
			throws OlogException {
		return wrappedSubmit(new FindLogs(map));
	}

	@Override
	public Collection<Log> findLogs(MultivaluedMap<String, String> map)
			throws OlogException {
		// TODO Auto-generated method stub
		return null;
	}

	private class FindLogs implements Callable<Collection<Log>> {

		private final MultivaluedMap<String, String> map;

		public FindLogs(String queryParameter, String pattern) {
			MultivaluedMap<String, String> mMap = new MultivaluedMapImpl();
			mMap.putSingle(queryParameter, pattern);
			this.map = mMap;
		}

		public FindLogs(MultivaluedMap<String, String> map) {
			this.map = map;
		}

		public FindLogs(Map<String, String> map) {
			MultivaluedMap<String, String> mMap = new MultivaluedMapImpl();
			Iterator<Map.Entry<String, String>> itr = map.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry<String, String> entry = itr.next();
				mMap.put(entry.getKey(),
						Arrays.asList(entry.getValue().split(",")));
			}
			this.map = mMap;
		}

		@Override
		public Collection<Log> call() throws Exception {
			Collection<Log> logs = new HashSet<Log>();
			XmlLogs xmlLogs = service.path("logs").queryParams(map)
					.accept(MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_JSON).get(XmlLogs.class);
			for (XmlLog xmllog : xmlLogs.getLogs()) {
				logs.add(new Log(xmllog));
			}
			return Collections.unmodifiableCollection(logs);
		}

	}
	
	@Override
	public void deleteTag(String tag) throws OlogException {
		final String deleteTag = tag;
		wrappedSubmit(new Runnable() {

			@Override
			public void run() {
				service.path("tags").path(deleteTag)
						.accept(MediaType.APPLICATION_XML)
						.accept(MediaType.APPLICATION_JSON).delete();
			}
		});

	}

	@Override
	public void deleteLogbook(String logbook) throws OlogException {
		final String logbookName = logbook;
		wrappedSubmit(new Runnable() {

			@Override
			public void run() {
				service.path("logbooks").path(logbookName)
						.accept(MediaType.APPLICATION_XML)
						.accept(MediaType.APPLICATION_JSON).delete();
			}

		});
	}

	@Override
	public void delete(LogBuilder log) throws OlogException {
		delete(log.build().getId());
	}

	@Override
	public void delete(Long logId) throws OlogException {
		final Long deleteLogId = logId;
		wrappedSubmit(new Runnable() {
			@Override
			public void run() {
				service.path("logs").path(String.valueOf(deleteLogId))
						.accept(MediaType.APPLICATION_XML)
						.accept(MediaType.APPLICATION_JSON).delete();
			}
		});
	}

	@Override
	public void delete(Collection<Log> logs) throws OlogException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(TagBuilder tag, Long logId) throws OlogException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(TagBuilder tag, Collection<Long> logIds)
			throws OlogException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(LogbookBuilder logbook, Long logId) throws OlogException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(LogbookBuilder logbook, Collection<Long> logIds)
			throws OlogException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(PropertyBuilder property, Long logId)
			throws OlogException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(PropertyBuilder property, Collection<Long> logIds)
			throws OlogException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(String fileName, Long logId) {
		// TODO Auto-generated method stub

	}

	private <T> T wrappedSubmit(Callable<T> callable) {
		try {
			return this.executor.submit(callable).get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			if (e.getCause() != null
					&& e.getCause() instanceof UniformInterfaceException) {
				throw new OlogException(
						(UniformInterfaceException) e.getCause());
			}
			throw new RuntimeException(e);
		}
	}

	private void wrappedSubmit(Runnable runnable) {
		try {
			this.executor.submit(runnable).get(60, TimeUnit.SECONDS);
		} catch (ExecutionException e) {
			if (e.getCause() != null
					&& e.getCause() instanceof UniformInterfaceException) {
				throw new OlogException(
						(UniformInterfaceException) e.getCause());
			}
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
