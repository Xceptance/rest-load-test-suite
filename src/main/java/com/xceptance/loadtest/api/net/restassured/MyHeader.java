package com.xceptance.loadtest.api.net.restassured;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.ParseException;
import org.htmlunit.util.NameValuePair;

public class MyHeader implements Header
{
    final NameValuePair header;
    final HeaderElement[] elements = {};

    public MyHeader(final NameValuePair header)
    {
        this.header = header;
    }

    @Override
    public String getName()
    {
        return header.getName();
    }

    @Override
    public String getValue()
    {
        return header.getValue();
    }

    @Override
    public HeaderElement[] getElements() throws ParseException
    {
        return elements;
    }
}
