package com.xceptance.loadtest.api.net.restassured;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
import org.apache.http.client.utils.URIBuilder;
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
import org.apache.http.util.Args;
import org.htmlunit.HttpMethod;
import org.htmlunit.WebResponse;
import org.htmlunit.util.NameValuePair;

import com.xceptance.loadtest.api.util.Context;
import com.xceptance.xlt.engine.httprequest.HttpResponse;

import io.restassured.RestAssured;
import io.restassured.filter.Filter;

/**
 * Wrapping XLT web client
 */
public class MyHttpClient extends AbstractHttpClient
{
    private URIBuilder uriBuilder;

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
        
        uriBuilder = null;
        try
        {
            uriBuilder = new URIBuilder(target.toURI());
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }

        requestFilter.getQueryParams().entrySet()
                .forEach(e -> ((URIBuilder) uriBuilder).addParameter(e.getKey(), e.getValue()));

        // add the params to the url

        if (HttpMethod.valueOf(request.getRequestLine().getMethod()).equals(HttpMethod.POST))
        {
            xltRequest.params(requestFilter.getRequestParams());
        }
        
        xltRequest.timerName(Context.get().timerName)
                  .baseUrl(uriBuilder.toString())
                  .body(requestFilter.getBody()) // if the method is not POST the body will be empty
                  .relativeUrl(request.getRequestLine().getUri())
                  .method(HttpMethod.valueOf(request.getRequestLine().getMethod()));

        Stream.of(request.getAllHeaders()).forEachOrdered(h -> xltRequest.header(h.getName(), h.getValue()));
        
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
        
        Optional<Filter> first = filters.stream().filter(p -> p instanceof MyFilter).findFirst();
        
        if (first.isPresent())
        {
            // safe to cast -> it extends the default filter
            filter = (MyFilter) first.get();
        }
        
        return filter;
    }
    
    /**
     * Add an URL parameter.
     *
     * @param name  the parameter's name
     * @param value the parameter's value
     * @return HttpRequest configuration
     */
    public URIBuilder param(final String name, String value, URIBuilder uriBuilder)
    {
        Args.notBlank(name, "Parameter name");

        uriBuilder.addParameter(name, value);
        return uriBuilder;
    }
    
    /**
     * Add URL parameters.
     *
     * @param params the URL parameters given as name-value pairs
     * @return HttpRequest configuration
     */
    public URIBuilder params(final List<NameValuePair> params, URIBuilder uriBuilder)
    {
        Args.notNull(params, "Parameters");
        params.forEach(p -> param(p.getName(), p.getValue(), uriBuilder));
        return uriBuilder;
    }
}