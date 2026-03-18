package com.xceptance.loadtest.rest.tests.postman;

import org.htmlunit.HttpMethod;
import org.junit.Assert;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.xceptance.loadtest.rest.data.NonSiteRelatedTest;
import com.xceptance.loadtest.rest.tests.LoadTestCase;
import com.xceptance.loadtest.rest.util.helpers.Actions;
import com.xceptance.loadtest.rest.util.helpers.AuthorizedHttpRequest;
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
public class TBasicAuth extends LoadTestCase implements NonSiteRelatedTest
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

            // we have not supplied the auth in any properties, such as general.authorization
            // hence we set it here for demo purposes in our context which will be asked first
            // if this is part of an XTC demo, you will find the setup in the secret properties!
            // Context.get().data.authorization = Optional.of("Basic cG9zdG1hbjpwYXNzd29yZA==");

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
}
