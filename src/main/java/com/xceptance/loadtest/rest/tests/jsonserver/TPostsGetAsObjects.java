package com.xceptance.loadtest.rest.tests.jsonserver;

import static org.junit.Assert.assertEquals;

import com.xceptance.loadtest.api.tests.RESTTestCase;
import com.xceptance.loadtest.api.util.Context;
import com.xceptance.loadtest.rest.actions.jsonserver.Posts;
import com.xceptance.xlt.api.util.XltRandom;


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
        // fetch all first
        final var posts = Posts.all();

        // ok, we have our posts, let's fetch a few

        // amount will be populated with a random value by the framework according to our
        // range spec in the properties such as 3-5, this is fix for this iteration!
        final var amount = Context.configuration().jsonplaceholderGetCount.value;
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

