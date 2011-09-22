package edu.msu.nscl.olog.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Eric Berryman taken from shroffk
 * 
 */
public class Log implements Comparable<Log> {
	private final Long id;
	private final String owner;
	private final String subject;
	private final String description;
	private final String level;
	private final Date createdDate;
	private final Date modifiedDate;
	private final int version;
	// private final Set<Tag> tags;
	// private final Set<Logbook> logbooks;
	// private final Set<Attachment> attachments;
	// private final Set<Property> properties;

	private final Map<String, Tag> tags;
	private final Map<String, Logbook> logbooks;
	private final Map<String, Attachment> attachments;
	private final Map<String, Property> properties;

	Log(XmlLog log) {
		this.id = log.getId();
		this.owner = log.getOwner();
		this.subject = log.getSubject();
		this.description = log.getDescription();
		this.level = log.getLevel();
		this.createdDate = log.getCreatedDate();
		this.modifiedDate = log.getModifiedDate();
		this.version = log.getVersion();
		Map<String, Tag> newTags = new HashMap<String, Tag>();
		for (XmlTag tag : log.getXmlTags().getTags()) {
			newTags.put(tag.getName(), new Tag(tag));
		}
		this.tags = Collections.unmodifiableMap(newTags);
		Map<String, Logbook> newLogbooks = new HashMap<String, Logbook>();
		for (XmlLogbook logbook : log.getXmlLogbooks().getLogbooks()) {
			newLogbooks.put(logbook.getName(), new Logbook(logbook));
		}
		this.logbooks = Collections.unmodifiableMap(newLogbooks);
		Map<String, Attachment> newAttachments = new HashMap<String, Attachment>();
		for (XmlAttachment attachment : log.getXmlAttachments()
				.getAttachments()) {
			newAttachments.put(attachment.getUri(), new Attachment(attachment));
		}
		this.attachments = Collections.unmodifiableMap(newAttachments);
		Map<String, Property> newProperties = new HashMap<String, Property>();
		for (XmlProperty property : log.getXmlProperties().getProperties()) {
			newProperties.put(property.getName(), new Property(property));
		}
		this.properties = Collections.unmodifiableMap(newProperties);

	}

	public Long getId() {
		return id;
	}

	public String getOwner() {
		return owner;
	}

	public String getSubject() {
		return subject;
	}

	public String getDescription() {
		return description;
	}

	public String getLevel() {
		return level;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public int getVersion() {
		return version;
	}

	public Collection<Tag> getTags() {
		return tags.values();
	}

	public Collection<Logbook> getLogbooks() {
		return logbooks.values();
	}

	// @deprecated not really deprecated, but javadoc doesn't have future
	@Deprecated
	public Collection<Attachment> getAttachments() {
		return attachments.values();
	}

	public Collection<Property> getProperties() {
		return properties.values();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Log obj) throws ClassCastException {
		if (!(obj instanceof Log))
			throw new ClassCastException("A Log object expected.");
		return (int) (this.id - ((Log) obj).getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Log))
			return false;
		Log other = (Log) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id)) {
			return false;
		} // else if (version != other.version)
			// return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Log#" + id + ":v." + version + " [subject=" + subject
				+ ", description=" + description + "]";
	}

}
