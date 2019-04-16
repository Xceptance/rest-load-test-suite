package com.xceptance.loadtest.rest.tests.simple;

import org.junit.Assert;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.xceptance.loadtest.api.tests.RESTTestCase;
import com.xceptance.loadtest.api.util.Actions;
import com.xceptance.xlt.engine.httprequest.HttpRequest;
import com.xceptance.xlt.engine.httprequest.HttpResponse;


public class TRaw extends RESTTestCase
{
    /**
     * Fetching a json file that is stored someplace. Of course could be also something dynamic,
     * just to show what you can do when no limits are in place.
     *
     * Our Test JSON
     *
     * <pre>
     * {
     *      "posts": [
     *          { "id": "1", "title": "json-server", "author": "Bob", "body": "This is text" }
     *      ],
     *      "comments": [
     *           { "postId": "1", "id": "1", "name": "Cool", "author": "Elise", "body": "Example comment" }
     *      ]
     * }
     * </pre>
     *
     * @throws Throwable
     */
    @Override
    public void test() throws Throwable
    {
        // just fetch a post
        Actions.run("Fetch Json File", t ->
        {
            final HttpResponse r = new HttpRequest().timerName(t)
                            // this file can disappear at any time, no warranties
                            // and please don't load test against this, Google will
                            // throttle you down anyway quickly, this is demo content
                            // Need fun? Checkout *jsonserver.TAddPost and get your own
                            // Json-Server running. It is not fast as hell, but you can
                            // learn a lot about performance measurement with it.
                            .baseUrl("https://storage.googleapis.com")
                            .relativeUrl("/renetest/db.json")
                            .method(HttpMethod.GET)
                            .fire();
            r.checkStatusCode(200);

            // keep in mind, validation costs cpu power
            // when you leave this demo validation out, it is about 12% more throughput
            // on an 8-core machine in a GCP data center
            // the overall measured runtimes stay the same, because the measurement
            // is taken underneath and does not include this time here
            final String response = r.getContentAsString();

            // ok, get us the post code and use the jsonpath query language for that
            final ReadContext ctx = JsonPath.parse(response);
            Assert.assertEquals("1", ctx.read("$.posts[0].id", String.class));
            Assert.assertTrue(ctx.read("$.posts[0].title", String.class).length() > 0);
            Assert.assertTrue(ctx.read("$.posts[0].body", String.class).length() > 0);
            Assert.assertEquals("Bob", ctx.read("$.posts[0].author", String.class));
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

