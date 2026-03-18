package com.xceptance.loadtest.rest.tests.jsonserver;

import com.xceptance.loadtest.api.data.supplier.DataLineSupplier;
import com.xceptance.loadtest.rest.actions.jsonserver.Posts;
import com.xceptance.loadtest.rest.actions.jsonserver.data.Post;
import com.xceptance.loadtest.rest.tests.LoadTestCase;
import com.xceptance.loadtest.rest.util.helpers.RESTUtils;

/**
 * Uses the http://jsonplaceholder.typicode.com/guide/ So don't hammer that public service, this is
 * rather demo!!!
 *
 * @throws Throwable
 */
public class TPostsCreatePost extends LoadTestCase
{
    @Override
    public void test() throws Throwable
    {
        // create a post and set data
        final Post post = new Post();
        post.author = DataLineSupplier.getRandomLine("firstnames.txt");
        post.title = "Entry of " + DataLineSupplier.getRandomLine("towns.txt");
        post.body = RESTUtils.getText(2, false);

        // add a post and get the id
        @SuppressWarnings("unused")
        final var id = Posts.create(post);
    }
}