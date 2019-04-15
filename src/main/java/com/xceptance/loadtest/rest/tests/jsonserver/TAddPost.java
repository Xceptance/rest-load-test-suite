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

    static class Post
    {
        final String author = GeneralDataProvider.getInstance().getFirstName(true);
        final String title = "Entry of " + GeneralDataProvider.getInstance().getTown(true);
        final String body = GeneralDataProvider.getInstance().getText(2, true);
    }

    @Override
    public void test() throws Throwable
    {
        Actions.run("Add Post", t ->
        {
            // add a post and check
            final Post post = new Post();

            final HttpResponse r = new HttpRequest()
                            .timerName(t)
                            .baseUrl("http://localhost:8080")
                            .relativeUrl("/posts")
                            .body(new Gson().toJson(post)) // Serialize it
                            .method(HttpMethod.POST)
                            .header("Content-type", "application/json; charset=UTF-8")
                            .fire();
            r.checkStatusCode(201);

            final String response = r.getContentAsString();

            // ok, get us the post code and use the jsonpath query language for that
            final ReadContext ctx = JsonPath.parse(response);
            Assert.assertTrue(ctx.read("$.id", String.class).length() > 0); // some UUID
            Assert.assertEquals(post.title, ctx.read("$.title", String.class));
            Assert.assertEquals(post.body, ctx.read("$.body", String.class));
            Assert.assertEquals(post.author, ctx.read("$.author", String.class));
        });
    }
}

