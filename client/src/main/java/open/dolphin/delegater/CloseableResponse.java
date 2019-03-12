package open.dolphin.delegater;

/**
 * javax.ws.rs.core.Response to java.lang.AutoCloseable Adapter code.
 * https://gist.github.com/taichi/5526601
 * @author taichi SATO
 */

import javax.ws.rs.core.*;
import javax.ws.rs.core.Link.Builder;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CloseableResponse extends Response implements AutoCloseable {

	final private Response delegate;

	private CloseableResponse(Response response) { this.delegate = response; }

        @Override
	public int hashCode() {
            return delegate.hashCode();
        }

        @Override
	public int getStatus() {
		return delegate.getStatus();
	}

        @Override
	public StatusType getStatusInfo() {
		return delegate.getStatusInfo();
	}

        @Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

        @Override
	public Object getEntity() {
		return delegate.getEntity();
	}

        @Override
	public <T> T readEntity(Class<T> entityType) {
		return delegate.readEntity(entityType);
	}

        @Override
	public <T> T readEntity(GenericType<T> entityType) {
		return delegate.readEntity(entityType);
	}

        @Override
	public <T> T readEntity(Class<T> entityType, Annotation[] annotations) {
		return delegate.readEntity(entityType, annotations);
	}

        @Override
	public String toString() {
		return delegate.toString();
	}

        @Override
	public <T> T readEntity(GenericType<T> entityType, Annotation[] annotations) {
		return delegate.readEntity(entityType, annotations);
	}

        @Override
	public boolean hasEntity() {
		return delegate.hasEntity();
	}

        @Override
	public boolean bufferEntity() {
		return delegate.bufferEntity();
	}

        @Override
	public MediaType getMediaType() {
		return delegate.getMediaType();
	}

        @Override
	public Locale getLanguage() {
		return delegate.getLanguage();
	}

        @Override
	public int getLength() {
		return delegate.getLength();
	}

        @Override
	public Set<String> getAllowedMethods() {
		return delegate.getAllowedMethods();
	}

        @Override
	public Map<String, NewCookie> getCookies() {
		return delegate.getCookies();
	}

        @Override
	public EntityTag getEntityTag() {
		return delegate.getEntityTag();
	}

        @Override
	public Date getDate() {
		return delegate.getDate();
	}

        @Override
	public Date getLastModified() {
		return delegate.getLastModified();
	}

        @Override
	public URI getLocation() {
		return delegate.getLocation();
	}

        @Override
	public Set<Link> getLinks() {
		return delegate.getLinks();
	}

        @Override
	public boolean hasLink(String relation) {
		return delegate.hasLink(relation);
	}

        @Override
	public Link getLink(String relation) {
		return delegate.getLink(relation);
	}

        @Override
	public Builder getLinkBuilder(String relation) {
		return delegate.getLinkBuilder(relation);
	}

        @Override
	public MultivaluedMap<String, Object> getMetadata() {
		return delegate.getMetadata();
	}

        @Override
	public MultivaluedMap<String, Object> getHeaders() {
		return delegate.getHeaders();
	}

        @Override
	public MultivaluedMap<String, String> getStringHeaders() {
		return delegate.getStringHeaders();
	}

        @Override
	public String getHeaderString(String name) {
		return delegate.getHeaderString(name);
	}

	@Override
	public void close() {
		delegate.close();
	}

	public static CloseableResponse of(Response response) {
		return new CloseableResponse(response);
	}
}
