package com.xceptance.loadtest.rest.tests.postman;

import org.junit.Assert;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.xceptance.loadtest.api.data.NonSiteRelatedTest;
import com.xceptance.loadtest.api.net.AuthorizedHttpRequest;
import com.xceptance.loadtest.api.tests.RESTTestCase;
import com.xceptance.loadtest.api.util.Actions;
import com.xceptance.loadtest.api.util.Context;
import com.xceptance.xlt.engine.httprequest.HttpRequest;
import com.xceptance.xlt.engine.httprequest.HttpResponse;

/**
 * A simple test to work with basic auth calls, targeting the postman echo service.
 *
 * For a documentation of the service please visit https://docs.postman-echo.com/
 *
 * @author Bernd Weigel
 *
 */
public class TBasicAuth extends RESTTestCase implements NonSiteRelatedTest
{

    @Override
    public void test() throws Throwable
    {
        Actions.run("Try Basic Auth", t ->
        {
            // Let's give it a try without any auth header and see if we fail.
            final HttpResponse unauthorizedResponse = new HttpRequest().timerName(t)
                            .baseUrl("https://postman-echo.com")
                            .relativeUrl("/basic-auth")
                            .method(HttpMethod.GET)
                            .fire();
            unauthorizedResponse.checkStatusCode(401);

            // Now let's use the AuthorizedHttpRequest, which takes the authorization header from
            // the configuration.
            final HttpResponse authorizedResponse = new AuthorizedHttpRequest().timerName(t)
                            .baseUrl("https://postman-echo.com")
                            .relativeUrl("/basic-auth")
                            .method(HttpMethod.GET)
                            .fire();
            authorizedResponse.checkStatusCode(200);

            // Keep in mind, validation costs cpu power
            // when you leave this demo validation out, it is about 12% more throughput
            // on an 8-core machine in a GCP data center
            // the overall measured runtimes stay the same, because the measurement
            // is taken underneath and does not include this time here.
            final String responseContent = authorizedResponse.getContentAsString();

            // Validate the response.
            final ReadContext ctx = JsonPath.parse(responseContent);
            Assert.assertEquals(true, ctx.read("$.authenticated", Boolean.class));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDown()
    {
        super.tearDown();

        // Please note: the code below could also be placed in the super class and is mostly placed
        // here for visibility. Still it depends on the actual implementation purposes.

        // You can do alternatively just cleaning of the cookie state if you have any, if you
        // don't have any... don't run that code, because performance testing is performance
        // programming.
        if (Context.configuration().clearCookies)
        {
            this.clearCookies();
        }

        // ** Release all resources so we don't have state
        // If you test from a server against a service, you might want to keep that
        // disabled because the server also won't close the pool. If you test from
        // a client that does only a few calls in a session/transaction, you might
        // want to use close() to emulate the state of the fresh connection. But that
        // greatly limits throughput but it is as close to the real deal as possible.
        // If your client collects a state aka cookies for instance, you probably have
        // to take care of cleaning this manually of you don't want to close the connections
        // to avoid the most expensive pieces aka HTTPS negotiations.
        //
        // If you don't close it, it can reuse the connection and the negotiated keys of TLS
        // that is about 100x (!) faster than closing... but you have state of course, your call!
        if (Context.configuration().closeWebClient)
        {
            this.closeWebClient();
        }
    }
}
