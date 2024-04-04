package com.xceptance.loadtest.api.net.restassured;

import org.apache.http.params.AbstractHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;
import org.htmlunit.util.NameValuePair;

import java.util.ArrayList;
import java.util.List;

public class MyHttpParams extends AbstractHttpParams
{
    final List<NameValuePair> parameters;

    public MyHttpParams(final List<NameValuePair> parameters)
    {
        this.parameters = parameters;
    }

    @Override
    public HttpParams setParameter(final String name, final Object value)
    {
        Args.check(value instanceof String, "Parameter value must be of type String.");

        for (int i = 0; i < parameters.size(); i++)
        {
            final NameValuePair p = parameters.get(i);
            if (p.getName().equals(name))
            {
                parameters.set(0, new NameValuePair(name, (String) value));
                break;
            }
        }
        return this;
    }

    @Override
    public boolean removeParameter(final String name)
    {
        return parameters.removeIf(p -> p.getName().equals(name));
    }

    @Override
    public Object getParameter(final String name)
    {
        return parameters.stream().filter(p -> p.getName().equals(name)).map(NameValuePair::getValue).findFirst().orElse(null);
    }

    @Override
    public HttpParams copy()
    {
        return new MyHttpParams(new ArrayList<>(parameters));
    }
}
