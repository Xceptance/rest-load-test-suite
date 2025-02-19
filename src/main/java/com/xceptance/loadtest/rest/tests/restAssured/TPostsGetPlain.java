package com.xceptance.loadtest.rest.tests.restAssured;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import com.xceptance.loadtest.api.tests.RESTTestCase;
import com.xceptance.loadtest.api.util.Actions;
import com.xceptance.loadtest.api.util.Context;

public class TPostsGetPlain extends RESTTestCase
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

        Actions.run("Get First Post", t ->
        {
            given()
                .baseUri(host)
            .when()
                .get("/posts/1")
            .then().assertThat()
                .statusCode(200)
                .body("userId", equalTo(1))
                .body("title", not(emptyString()))
                .body("body", containsString("rerum est autem"));
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

