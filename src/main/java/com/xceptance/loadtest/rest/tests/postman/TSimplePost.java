package com.xceptance.loadtest.rest.tests.postman;

import org.junit.Assert;

import org.htmlunit.HttpMethod;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.xceptance.loadtest.api.data.DataSupplier;
import com.xceptance.loadtest.api.data.NonSiteRelatedTest;
import com.xceptance.loadtest.api.tests.RESTTestCase;
import com.xceptance.loadtest.api.util.Actions;
import com.xceptance.loadtest.api.util.Context;
import com.xceptance.xlt.api.util.XltRandom;
import com.xceptance.xlt.engine.httprequest.HttpRequest;
import com.xceptance.xlt.engine.httprequest.HttpResponse;

/**
 * This is a simple test case using the postman echo service to demonstrate how to submit different
 * kinds of post parameters.
 *
 * For a documentation of the service please visit https://docs.postman-echo.com/
 *
 * @author Bernd Weigel
 *
 */
public class TSimplePost extends RESTTestCase implements NonSiteRelatedTest
{

    /**
     * A simple data object to submit
     */
    static class Post
    {
        String id;

        final String author = DataSupplier.firstName();
        final String title = "Entry of " + DataSupplier.town();
        final String body = DataSupplier.getText(1, true);
    }

    @Override
    public void test() throws Throwable
    {
        // Send a post request which contains form data parameters.
        final String lastId = Actions.get("Post Form Data Parameter", t ->
        {
            final String id = String.valueOf(XltRandom.nextInt());

            // let's submit some post parameter (form data)
            final HttpResponse response = new HttpRequest().timerName(t)
                            .baseUrl("https://postman-echo.com")
                            .relativeUrl("/post/")
                            .param("id", id)
                            .param("foo", "true")
                            .param("bar", "false")
                            .param("parameter", "value")
                            .method(HttpMethod.POST)
                            .fire();
            response.checkStatusCode(200);

            // Keep in mind, validation costs cpu power
            // when you leave this demo validation out, it is about 12% more throughput
            // on an 8-core machine in a GCP data center
            // the overall measured runtimes stay the same, because the measurement
            // is taken underneath and does not include this time here.
            final String responseContent = response.getContentAsString();

            // Ok, get us some response content for validation and use the jsonpath query
            // language for that.
            final ReadContext ctx = JsonPath.parse(responseContent);
            Assert.assertTrue(Boolean.valueOf(ctx.read("$.form.foo", String.class)));
            Assert.assertFalse(Boolean.valueOf(ctx.read("$.form.bar", String.class)));
            Assert.assertNotNull(Boolean.valueOf(ctx.read("$.form.parameter")));

            // Also let's return the id from the response.
            final String responseId = ctx.read("$.form.id", String.class);
            Assert.assertEquals(id, responseId);

            return responseId;
        });

        // We don't need any further data from this action, so we can use run() instead of get().
        Actions.run("Post Body", t ->
        {
            final Post post = new Post();

            // Let us reuse the Id from the last action.
            post.id = lastId;

            // Let's submit our post object in the request body.
            final HttpResponse response = new HttpRequest().timerName(t)
                            .baseUrl("https://postman-echo.com")
                            .relativeUrl("/post/")
                            .body(new Gson().toJson(post)) // Serialize it.
                            .method(HttpMethod.POST)
                            .fire();

            // Status code is sufficient validation on this one.
            response.checkStatusCode(200);
        });

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDown()
    {
        super.tearDown();

        // Please note: the code below could also be placed in the super class and is mostly placed
        // here for visibility. Still it depends on the actual implementation purposes.

        // You can do alternatively just cleaning of the cookie state if you have any, if you
        // don't have any... don't run that code, because performance testing is performance
        // programming.
        if (Context.configuration().clearCookies)
        {
            this.clearCookies();
        }

        // ** Release all resources so we don't have state
        // If you test from a server against a service, you might want to keep that
        // disabled because the server also won't close the pool. If you test from
        // a client that does only a few calls in a session/transaction, you might
        // want to use close() to emulate the state of the fresh connection. But that
        // greatly limits throughput but it is as close to the real deal as possible.
        // If your client collects a state aka cookies for instance, you probably have
        // to take care of cleaning this manually of you don't want to close the connections
        // to avoid the most expensive pieces aka HTTPS negotiations.
        //
        // If you don't close it, it can reuse the connection and the negotiated keys of TLS
        // that is about 100x (!) faster than closing... but you have state of course, your call!
        if (Context.configuration().closeWebClient)
        {
            this.closeWebClient();
        }
    }
}

