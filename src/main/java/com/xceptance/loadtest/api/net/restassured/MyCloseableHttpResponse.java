package com.xceptance.loadtest.api.net.restassured;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.htmlunit.WebResponse;

import com.xceptance.common.util.RegExUtils;

/**
 * Wrapping response from XLT ({@link WebResponse})
 */
public class MyCloseableHttpResponse extends BasicHttpResponse implements CloseableHttpResponse
{
    public MyCloseableHttpResponse(final WebResponse webResponse)
    {
    	super(createProtocolVersion(webResponse), webResponse.getStatusCode(), webResponse.getStatusMessage());

    	setHeaders(createHeaders(webResponse));
    	setEntity(createEntity(webResponse));
    }

    @Override
    public void close() throws IOException
    {
        // nothing to close, XLT takes care of connections
    }

    private static ProtocolVersion createProtocolVersion(final WebResponse webResponse)
    {
        final String protocol = webResponse.getProtocolVersion();
        // this will result in something like "HTTP/1.1"

        // HTTP
        final String name = RegExUtils.getFirstMatch(protocol, "^[^/]+");

        // 1
        final String majorRaw = RegExUtils.getFirstMatch(protocol, "/(\\d+)", 1);
        final int major = majorRaw == null ? 0 : Integer.parseInt(majorRaw);

        // 1
        final String minorRaw = RegExUtils.getFirstMatch(protocol, "\\.(d+)$", 1);
        final int minor = minorRaw == null ? 0 : Integer.parseInt(minorRaw);

        return new ProtocolVersion(name, major, minor);
    }

    private static Header[] createHeaders(final WebResponse webResponse)
    {
    	return webResponse.getResponseHeaders().stream().map(h -> new BasicHeader(h.getName(), h.getValue())).toArray(Header[]::new);
    }

    private static HttpEntity createEntity(final WebResponse webResponse)
    {
        final BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
        try
        {
            basicHttpEntity.setChunked(false);
            basicHttpEntity.setContent(webResponse.getContentAsStream());
            basicHttpEntity.setContentEncoding(webResponse.getResponseHeaderValue("Content-Encoding"));
            basicHttpEntity.setContentLength(webResponse.getContentLength());
            basicHttpEntity.setContentType(webResponse.getResponseHeaderValue("Content-Type"));
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }

        return basicHttpEntity;
    }
}
