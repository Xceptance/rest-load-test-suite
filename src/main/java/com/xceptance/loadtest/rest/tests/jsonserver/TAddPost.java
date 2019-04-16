package com.xceptance.loadtest.rest.tests.jsonserver;

import org.junit.Assert;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.xceptance.loadtest.api.tests.RESTTestCase;
import com.xceptance.loadtest.api.util.Actions;
import com.xceptance.xlt.api.data.GeneralDataProvider;
import com.xceptance.xlt.engine.httprequest.HttpRequest;
import com.xceptance.xlt.engine.httprequest.HttpResponse;


public class TAddPost extends RESTTestCase
{
    /**
     * Using the https://github.com/clue/docker-json-server of
     * https://github.com/typicode/json-server or http://jsonplaceholder.typicode.com/
     *
     * @throws Throwable
     */

    static class Post
    {
        transient String id;

        final String author = GeneralDataProvider.getInstance().getFirstName(true);
        final String title = "Entry of " + GeneralDataProvider.getInstance().getTown(true);
        final String body = GeneralDataProvider.getInstance().getText(2, true);
    }

    @Override
    public void test() throws Throwable
    {
        // add a post and check
        final Post post = new Post();

        // add a post and get us the id for a second interaction
        post.id = Actions.get("Add Post", t ->
        {
            final HttpResponse r = new HttpRequest()
                            .timerName(t)
                            .baseUrl("https://jsonplaceholder.typicode.com")
                            .relativeUrl("/posts")
                            .body(new Gson().toJson(post)) // Serialize it
                            .method(HttpMethod.POST)
                            .header("Content-type", "application/json; charset=UTF-8")
                            .fire();
            r.checkStatusCode(201);

            final String response = r.getContentAsString();

            // ok, get us the post code and use the jsonpath query language for that
            final ReadContext ctx = JsonPath.parse(response);

            final String id = ctx.read("$.id", String.class);
            Assert.assertTrue(id.length() > 0); // some UUID
            Assert.assertEquals(post.title, ctx.read("$.title", String.class));
            Assert.assertEquals(post.body, ctx.read("$.body", String.class));
            Assert.assertEquals(post.author, ctx.read("$.author", String.class));

            return id;
        });

        // You have to run your own Json-Server to make that work, the public version
        // does not accept new data or not all the time
        // In addition, it seems to have UTF-8 issues, at least it returned garbage sometimes
        // when it returned stuff
        Actions.run("Verify Post", t ->
        {
            final HttpResponse r = new HttpRequest()
                            .timerName(t)
                            .baseUrl("https://jsonplaceholder.typicode.com")
                            .relativeUrl("/posts/" + post.id)
                            .method(HttpMethod.GET)
                            .fire();
            r.checkStatusCode(200);

            final String response = r.getContentAsString();

            // ok, get us the post code and use the jsonpath query language for that
            final ReadContext ctx = JsonPath.parse(response);

            final String id = ctx.read("$.id", String.class);
            Assert.assertTrue(id.length() > 0); // some UUID
            Assert.assertEquals(post.title, ctx.read("$.title", String.class));
            Assert.assertEquals(post.body, ctx.read("$.body", String.class));
            Assert.assertEquals(post.author, ctx.read("$.author", String.class));
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

