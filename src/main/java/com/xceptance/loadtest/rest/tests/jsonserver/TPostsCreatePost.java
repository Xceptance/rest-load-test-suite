package com.xceptance.loadtest.rest.tests.jsonserver;

import com.xceptance.loadtest.api.tests.RESTTestCase;
import com.xceptance.loadtest.rest.actions.jsonserver.Posts;
import com.xceptance.loadtest.rest.actions.jsonserver.data.Post;
import com.xceptance.xlt.api.data.GeneralDataProvider;

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
        post.author = GeneralDataProvider.getInstance().getFirstName(true);
        post.title = "Entry of " + GeneralDataProvider.getInstance().getTown(true);
        post.body = GeneralDataProvider.getInstance().getText(2, true);

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

