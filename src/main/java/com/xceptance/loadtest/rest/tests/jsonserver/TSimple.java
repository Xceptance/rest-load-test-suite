package com.xceptance.loadtest.rest.tests.jsonserver;

import org.junit.Assert;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.xceptance.loadtest.api.tests.RESTTestCase;
import com.xceptance.loadtest.api.util.Actions;
import com.xceptance.xlt.engine.httprequest.HttpRequest;
import com.xceptance.xlt.engine.httprequest.HttpResponse;


public class TSimple extends RESTTestCase
{
    /**
     * Using the http://jsonplaceholder.typicode.com/
     *
     * @throws Throwable
     */
    @Override
    public void test() throws Throwable
    {
        // just fetch single post aka the first one
        Actions.run("Get Post", t ->
        {
            final HttpResponse r = new HttpRequest().timerName(t).baseUrl("https://jsonplaceholder.typicode.com").relativeUrl("/posts/1").fire();
            r.checkStatusCode(200);

            // ok, get us the post code and use the jsonpath query language for that
            final ReadContext ctx = JsonPath.parse(r.getContentAsString());

            // verify
            Assert.assertTrue(1 == ctx.read("$.userId", Integer.class));
            Assert.assertTrue(ctx.read("$.title", String.class).length() > 0);
            Assert.assertTrue(ctx.read("$.body", String.class).length() > 0);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDown()
    {
        super.tearDown();

        // if you don't close it, it can reuse the connection and the negotiated keys of TLS
        // that is about 100x (!) faster than closing... but you have state of course, your call!!!!
        // this.closeWebClient();

        // you can do alternatively just cleaning of the cookie state if you have any, if you
        // don't have any... don't run that code, because performance testing is performance
        // programming
        // this.clearCookies();
    }
}

