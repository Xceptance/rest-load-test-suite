package com.xceptance.loadtest.api.net.restassured;

import com.xceptance.common.util.RegExUtils;
import com.xceptance.loadtest.api.util.Context;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicListHeaderIterator;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;
import org.htmlunit.WebResponse;
import org.htmlunit.util.NameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Wrapping response from XLT ({@link WebResponse})
 */
public class MyCloseableHttpResponse implements CloseableHttpResponse
{
    private List<Header> headers;
    private HttpParams params;
    private final WebResponse webResponse;
    private HttpEntity entity;

    public MyCloseableHttpResponse(final WebResponse webResponse)
    {
        this.webResponse = webResponse;
    }

    private List<Header> getHeaders()
    {
        headers = Optional.ofNullable(headers).orElseGet(() -> webResponse.getResponseHeaders().stream().map(MyHeader::new).collect(Collectors.toList()));
        return headers;
    }

    @Override
    public void close() throws IOException
    {
        // nothing to close, XLT takes care of connections
    }

    @Override
    public void setParams(final HttpParams params)
    {
        this.params = params;
    }
    
    @Override
    public HttpParams getParams()
    {
        return params;
    }

    @Override
    public void setHeaders(final Header[] headers)
    {
        this.headers = ArrayUtils.isEmpty(headers) ? new ArrayList<>() : List.of(headers);
    }

    @Override
    public void setHeader(final String name, final String value)
    {
        // search the header of interest, first match only
        final List<Header> _headers = getHeaders();
        for (int i = 0; i < _headers.size(); i++)
        {
            // when found
            if (_headers.get(i).getName().equals(name))
            {
                // replace it
                _headers.set(i, new MyHeader(new NameValuePair(name, value)));
                break;
            }
        }
    }

    @Override
    public void setHeader(final Header header)
    {
        setHeader(header.getName(), header.getValue());
    }

    @Override
    public void removeHeaders(final String name)
    {
        getHeaders().removeIf(h -> h.getName().equals(name));
    }

    @Override
    public void removeHeader(final Header header)
    {
        getHeaders().removeIf(h -> h.getName().equals(header.getName()) && h.getValue().equals(header.getValue()));
    }

    @Override
    public HeaderIterator headerIterator(final String name)
    {
        return new BasicListHeaderIterator(getHeaders(), name);
    }

    @Override
    public HeaderIterator headerIterator()
    {
        return new BasicListHeaderIterator(getHeaders(), null);
    }

    @Override
    public ProtocolVersion getProtocolVersion()
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


    @Override
    public Header getLastHeader(final String name)
    {
        final List<Header> _headers = getHeaders();
        return _headers.get(_headers.size() - 1);
    }
    
    private Stream<Header> _getHeaders(final String name)
    {
        return getHeaders().stream().filter(h -> h.getName().equals(name));
    }

    @Override
    public Header[] getHeaders(final String name)
    {
        final List<Header> _headers = _getHeaders(name).collect(Collectors.toList());
        return _headers.toArray(new Header[_headers.size()]);
    }

    @Override
    public Header getFirstHeader(final String name)
    {
        return _getHeaders(name).findFirst().orElse(null);
    }

    @Override
    public Header[] getAllHeaders()
    {
        final List<Header> _headers = getHeaders();
        return _headers.toArray(new Header[_headers.size()]);
    }

    @Override
    public boolean containsHeader(final String name)
    {
        return getHeaders().stream().anyMatch(h -> h.getName().equals(name));
    }

    @Override
    public void addHeader(final String name, final String value)
    {
        setHeader(name, value);
    }

    @Override
    public void addHeader(final Header header)
    {
        setHeader(header);
    }

    @Override
    public void setStatusLine(final ProtocolVersion ver, final int code, final String reason)
    {
        // ignore, we read that from XLT response
    }

    @Override
    public void setStatusLine(final ProtocolVersion ver, final int code)
    {
        // ignore, we read that from XLT response
    }

    @Override
    public void setStatusLine(final StatusLine statusline)
    {
        // ignore, we read that from XLT response
    }

    @Override
    public void setStatusCode(final int code) throws IllegalStateException
    {
        // ignore, we read that from XLT response
    }

    @Override
    public void setReasonPhrase(final String reason) throws IllegalStateException
    {
        // ignore, we read that from XLT response
    }

    @Override
    public void setLocale(final Locale loc)
    {
        // ignore
    }

    @Override
    public void setEntity(final HttpEntity entity)
    {
        this.entity = entity;
    }

    @Override
    public StatusLine getStatusLine()
    {
        return new BasicStatusLine(getProtocolVersion(), webResponse.getStatusCode(), webResponse.getStatusMessage());
    }

    @Override
    public Locale getLocale()
    {
        return Locale.forLanguageTag(Context.getSite().locale);
    }

    @Override
    public HttpEntity getEntity()
    {
        if (entity == null)
        {
            final BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
            try
            {
                basicHttpEntity.setChunked(false);
                basicHttpEntity.setContent(webResponse.getContentAsStream());
                basicHttpEntity.setContentEncoding(webResponse.getResponseHeaderValue("Content-Encoding"));
                basicHttpEntity.setContentLength(webResponse.getContentLength());
                basicHttpEntity.setContentType(webResponse.getResponseHeaderValue("Content-Type"));
                entity = basicHttpEntity;
            }
            catch (final IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        return entity;
    }
}
