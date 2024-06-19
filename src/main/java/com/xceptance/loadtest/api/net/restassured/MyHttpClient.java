package com.xceptance.loadtest.api.net.restassured;

import com.xceptance.loadtest.api.util.Context;
import com.xceptance.xlt.engine.httprequest.HttpResponse;

import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.specification.RequestSpecification;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.RequestDirector;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.htmlunit.HttpMethod;
import org.htmlunit.WebResponse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Wrapping XLT web client
 */
public class MyHttpClient extends AbstractHttpClient
{
    public MyHttpClient()
    {
        this(null, null);
    }

    protected MyHttpClient(final ClientConnectionManager conman, final HttpParams params)
    {
        super(conman, params);
    }

    @Override
    protected HttpParams createHttpParams()
    {
        return new BasicHttpParams();
    }

    @Override
    protected BasicHttpProcessor createHttpProcessor()
    {
        return new BasicHttpProcessor();
    }
    
    @Override
    public void close()
    {
        // there's nothing to close
    }
    
    @Override
    protected RequestDirector createClientRequestDirector(
            final HttpRequestExecutor requestExec,
            final ClientConnectionManager conman,
            final ConnectionReuseStrategy reustrat,
            final ConnectionKeepAliveStrategy kastrat,
            final HttpRoutePlanner rouplan,
            final HttpProcessor httpProcessor,
            final HttpRequestRetryHandler retryHandler,
            final RedirectStrategy redirectStrategy,
            final AuthenticationStrategy targetAuthStrategy,
            final AuthenticationStrategy proxyAuthStrategy,
            final UserTokenHandler userTokenHandler,
            final HttpParams params) {
        return this::fire;
    }

    private CloseableHttpResponse fire(final HttpHost target, final HttpRequest request, final HttpContext context)
            throws IOException, ClientProtocolException
    {
        final HttpResponse response = load(target, request);
        final WebResponse webResponse = response.getWebResponse();
        return new MyCloseableHttpResponse(webResponse);
    }

    private HttpResponse load(final HttpHost target, final HttpRequest request)
    {
        final com.xceptance.xlt.engine.httprequest.HttpRequest xltRequest = new com.xceptance.xlt.engine.httprequest.HttpRequest();
        
        MyFilter requestFilter = getRequestFilter();
        
        xltRequest.timerName(Context.get().timerName)
                  .baseUrl(target.toURI())
                  .body(requestFilter.getBody()) // if the method is not POST the body will be empty
                  .relativeUrl(request.getRequestLine().getUri())
                  .method(HttpMethod.valueOf(request.getRequestLine().getMethod()));

        Stream.of(request.getAllHeaders()).forEachOrdered(h -> xltRequest.header(h.getName(), h.getValue()));
        
        xltRequest.params(requestFilter.getQueryParams());
        xltRequest.params(requestFilter.getRequestParams());
        
        try
        {
            return xltRequest.fire();
        }
        catch (IOException | URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private MyFilter getRequestFilter()
    {
        MyFilter filter = null; 
        List<Filter> filters = RestAssured.filters();
        
        Optional<Filter> first = filters.stream().filter(p -> p instanceof MyFilter) .findFirst();
        
        if (first.isPresent())
        {
            // safe to cast -> it extends the default filter
            filter = (MyFilter) first.get();
        }
        
        return filter;
    }
}