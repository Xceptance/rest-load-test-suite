package com.xceptance.loadtest.api.net.restassured;

import java.util.Map;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class MyFilter implements Filter
{
    private FilterableRequestSpecification requestSpec2;

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec,
            FilterContext ctx)
    {
        requestSpec2 = requestSpec;

        return ctx.next(requestSpec, responseSpec);
    }
    
    public String getBody()
    {
        return requestSpec2.getBody();
    }
    
    public Map<String, String> getQueryParams()
    {
        return requestSpec2.getQueryParams();
    }
    
    public Map<String, String> getRequestParams()
    {
        return requestSpec2.getRequestParams();
    }
}
