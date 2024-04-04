package com.xceptance.loadtest.rest.actions.jsonserver;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;

import org.htmlunit.HttpMethod;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.xceptance.loadtest.api.util.Actions;
import com.xceptance.loadtest.api.util.Context;
import com.xceptance.loadtest.rest.actions.jsonserver.data.Post;
import com.xceptance.loadtest.rest.util.GsonUtil;
import com.xceptance.xlt.engine.httprequest.HttpRequest;
import com.xceptance.xlt.engine.httprequest.HttpResponse;

/**
 * This groups some activities together and centralizes code. This is an example and not a mandatory
 * setup.
 *
 * @author rschwietzke
 *
 */
public class Posts
{
    /**
     * Fetches all posts
     *
     * @throws Throwable
     */
    public static Post[] all() throws Throwable
    {
        // get us all notes and turn them into nice objects
        return Actions.get("Get Posts", t ->
        {
            final HttpResponse r = new HttpRequest()
                            .timerName(t)
                            .baseUrl(Context.configuration().jsonplaceholderHost)
                            .relativeUrl("/posts")
                            .fire();
            r.checkStatusCode(200); // ok?

            final var p = GsonUtil.gson().fromJson(r.getContentAsString(), Post[].class);

            assertEquals(100, p.length);

            return p;
        });
    }

    /**
     * Fetches a post by ID
     *
     * @param id
     *            the id to fetch
     * @return the post returned
     * @throws Throwable
     */
    public static Post byId(final String id) throws Throwable
    {
        // this is the measurement block to assign the proper naming to everything that happens
        // here and report the action aka activity as its own measurement block
        return Actions.get("Get Post", t ->
        {
            // "t" is the time name we have to pass to the HttpRequest,
            // we can name it manually too of course
            final HttpResponse r = new HttpRequest()
                            .timerName(t)
                            .baseUrl(Context.configuration().jsonplaceholderHost)
                            .relativeUrl("/posts/" + id) // simple formatting of the url
                            .fire();
            r.checkStatusCode(200); // ok?

            // turn it into an object
            return GsonUtil.gson().fromJson(r.getContentAsString(), Post.class);
        });
    }

    /**
     * Create a post
     *
     * @return the id of the created post
     * @throws Throwable
     */
    public static String create(final Post post) throws Throwable
    {
        // add a post and get preserve the id, it cannot be used here again,
        // because the test service is not using a database, hence it does not
        // preserve our data
        return Actions.get("Create Post", t ->
        {
            final HttpResponse r = new HttpRequest()
                            .timerName(t)
                            .baseUrl(Context.configuration().jsonplaceholderHost)
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
            Assert.assertTrue(Integer.valueOf(id) > 0);
            Assert.assertEquals(post.title, ctx.read("$.title", String.class));
            Assert.assertEquals(post.body, ctx.read("$.body", String.class));
            Assert.assertEquals(post.author, ctx.read("$.author", String.class));

            return id;
        });
    }
}

