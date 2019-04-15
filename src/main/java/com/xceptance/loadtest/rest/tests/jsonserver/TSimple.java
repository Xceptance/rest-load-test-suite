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
     * Using the https://github.com/clue/docker-json-server of
     * https://github.com/typicode/json-server or http://jsonplaceholder.typicode.com/
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
        Actions.run("Get Post", t ->
        {
            final HttpResponse r = new HttpRequest().timerName(t).baseUrl("http://localhost:8080").relativeUrl("/posts/1").fire();
            r.checkStatusCode(200);

            final String response = r.getContentAsString();

            // ok, get us the post code and use the jsonpath query language for that
            final ReadContext ctx = JsonPath.parse(response);
            Assert.assertTrue(1 == ctx.read("$.id", Integer.class));
            Assert.assertTrue(ctx.read("$.title", String.class).length() > 0);
            Assert.assertTrue(ctx.read("$.body", String.class).length() > 0);
            Assert.assertEquals("Bob", ctx.read("$.author", String.class));
        });
    }
}

