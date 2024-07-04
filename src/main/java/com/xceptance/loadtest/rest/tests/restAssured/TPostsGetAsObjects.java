package com.xceptance.loadtest.rest.tests.restAssured;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

import com.xceptance.loadtest.api.tests.RESTTestCase;
import com.xceptance.loadtest.api.util.Actions;
import com.xceptance.loadtest.api.util.Context;
import com.xceptance.loadtest.rest.actions.jsonserver.data.Post;
import com.xceptance.xlt.api.util.XltRandom;

import io.restassured.response.Response;

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
        // this can be of course also passed to this method or look at the larger framework
        // https://github.com/Xceptance/posters-advanced-loadtest-suite and its Context concept
        final String host = Context.configuration().jsonplaceholderHost;

        
        // just fetch all posts aka the first one
        Post[] posts = Actions.get("RestAssuredRequest Get All", t ->
        {
            Response response = given()
                                    .baseUri(host)
                                .when()
                                    .get("/posts");

            response.then().assertThat()
                    .statusCode(200);
            
            final Post[] postsResponse = response.getBody().as(Post[].class);
            
            assertEquals(100, postsResponse.length);
            
            return postsResponse;
        });
        
        final var amount = Context.configuration().jsonplaceholderGetCount.value;
        
        for (int i = 0; i < amount; i++)
        {
            // get us a random post and fetch it as a single activity
            // show a nice way of organizing code at the same time
            final Post postToGet = posts[XltRandom.nextInt(posts.length)];
            
            Actions.run("RestAssuredRequest Get", t ->
            {
                Response response = given()
                                        .baseUri(host)
                                    .when()
                        .get("/posts/" + postToGet.id);
                
                response.then().assertThat()
                    .statusCode(200);
                
                final Post postFromRestAssured = response.getBody().as(Post.class);
                
                // just check that it is the one we asked for
                assertEquals(postToGet.id, postFromRestAssured.id);
            });
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

