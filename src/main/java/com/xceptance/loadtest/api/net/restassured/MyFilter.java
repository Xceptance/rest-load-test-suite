package com.xceptance.loadtest.api.net.restassured;

import java.util.Map;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class MyFilter implements Filter
{
    public final static ThreadLocal<FilterableRequestSpecification> localRequestSpec = new ThreadLocal<FilterableRequestSpecification>();

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec,
            FilterContext ctx)
    {
        localRequestSpec.set(requestSpec);

        // requestSpec2 = requestSpec;
        return ctx.next(requestSpec, responseSpec);
    }
    
    public String getBody()
    {
        return localRequestSpec.get().getBody();
    }
    
    public Map<String, String> getQueryParams()
    {
        return localRequestSpec.get().getQueryParams();
    }
    
    public Map<String, String> getRequestParams()
    {
        return localRequestSpec.get().getRequestParams();
    }
}
