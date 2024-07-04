package com.xceptance.loadtest.rest.tests.restAssured;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import com.google.gson.Gson;
import com.xceptance.loadtest.api.data.DataSupplier;
import com.xceptance.loadtest.api.tests.RESTTestCase;
import com.xceptance.loadtest.api.util.Actions;
import com.xceptance.loadtest.api.util.Context;
import com.xceptance.loadtest.rest.actions.jsonserver.data.Post;


public class TPostsCreatePost extends RESTTestCase
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

        // create a post and set data
        final Post post = new Post();
        post.author = DataSupplier.firstName();
        post.title = "Entry of " + DataSupplier.town();
        post.body = DataSupplier.getText(2, false);
        
        // Same scenario, just using REST-Assured
        Actions.run("RestAssuredRequest Post", t ->
        {
            given()
                .baseUri(host)
                .body(new Gson().toJson(post))
                .header("Content-Type", "application/json; charset=UTF-8")
            .when()
                .post("/posts")
            .then().assertThat()
                .statusCode(201)
                .header("Content-Type", "application/json; charset=utf-8")
                .body("title", equalTo(post.title))
                .body("body", equalTo(post.body))
                .body("author", equalTo(post.author));
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

