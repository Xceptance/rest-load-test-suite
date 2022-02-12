package com.xceptance.loadtest.rest.tests.jsonserver;

import static org.junit.Assert.assertEquals;

import com.xceptance.loadtest.api.tests.RESTTestCase;
import com.xceptance.loadtest.api.util.Actions;
import com.xceptance.loadtest.rest.actions.jsonserver.Posts;
import com.xceptance.loadtest.rest.actions.jsonserver.data.Post;
import com.xceptance.loadtest.rest.util.GsonUtil;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.util.XltRandom;
import com.xceptance.xlt.engine.httprequest.HttpRequest;
import com.xceptance.xlt.engine.httprequest.HttpResponse;


public class TPostsGetAsObjects extends RESTTestCase
{
    /**
     * Using the http://jsonplaceholder.typicode.com/
     *
     * @throws Throwable
     */
    @Override
    public void test() throws Throwable
    {
        final var host = XltProperties.getInstance().getProperty("jsonplaceholder.host");

        // get us all notes and turn them into nice objects
        final var posts = Actions.get("Get Post", t ->
        {
            final HttpResponse r = new HttpRequest().timerName(t).baseUrl(host).relativeUrl("/posts").fire();
            r.checkStatusCode(200); // ok?

            final var p = GsonUtil.gson().fromJson(r.getContentAsString(), Post[].class);

            assertEquals(100, p.length);

            return p;
        });

        // ok, we have our posts, let's fetch a few
        final var amount = XltProperties.getInstance().getProperty("jsonplaceholder.get.count", 4);
        for (int i = 0; i < amount; i++)
        {
            // get us a random post and fetch it as a single activity
            // show a nice way of organizing code at the same time
            final var postToGet = posts[XltRandom.nextInt(posts.length)];
            final var post = Posts.byId(postToGet.id);

            // just check that it is the one we asked for
            assertEquals(postToGet.id, post.id);
        }
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

