package com.xceptance.loadtest.rest.tests.jsonserver;

import com.xceptance.loadtest.api.data.DataSupplier;
import com.xceptance.loadtest.api.tests.RESTTestCase;
import com.xceptance.loadtest.rest.actions.jsonserver.Posts;
import com.xceptance.loadtest.rest.actions.jsonserver.data.Post;

/**
 * Uses the http://jsonplaceholder.typicode.com/guide/ So don't hammer that public service, this is
 * rather demo!!!
 *
 * @throws Throwable
 */
public class TPostsCreatePost extends RESTTestCase
{
    @Override
    public void test() throws Throwable
    {
        // create a post and set data
        final Post post = new Post();
        post.author = DataSupplier.firstName();
        post.title = "Entry of " + DataSupplier.town();
        post.body = DataSupplier.getText(2, false);

        // add a post and get the id
        final var id = Posts.create(post);
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

