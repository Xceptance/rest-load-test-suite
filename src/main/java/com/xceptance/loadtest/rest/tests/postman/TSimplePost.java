package com.xceptance.loadtest.rest.tests.postman;

import org.htmlunit.HttpMethod;
import org.junit.Assert;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.xceptance.loadtest.api.data.supplier.DataLineSupplier;
import com.xceptance.loadtest.rest.data.NonSiteRelatedTest;
import com.xceptance.loadtest.rest.tests.LoadTestCase;
import com.xceptance.loadtest.rest.util.helpers.Actions;
import com.xceptance.loadtest.rest.util.helpers.DataSupplier;
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
public class TSimplePost extends LoadTestCase implements NonSiteRelatedTest
{

    /**
     * A simple data object to submit
     */
    static class Post
    {
        String id;

        final String author = DataLineSupplier.getRandomLine("firstnames.txt");
        final String title = "Entry of " + DataLineSupplier.getRandomLine("towns.txt");
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
}

