package com.xceptance.loadtest.rest.tests.postman;

import org.junit.Assert;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.xceptance.loadtest.api.data.NonSiteRelatedTest;
import com.xceptance.loadtest.api.tests.RESTTestCase;
import com.xceptance.loadtest.api.util.Actions;
import com.xceptance.loadtest.api.util.Context;
import com.xceptance.loadtest.api.util.SimpleRESTJSONAction;
import com.xceptance.xlt.api.util.XltRandom;
import com.xceptance.xlt.engine.httprequest.HttpRequest;
import com.xceptance.xlt.engine.httprequest.HttpResponse;

/**
 * This is a more complex test case using the postman echo service and performing the following
 * steps.
 *
 * 1. Get a time stamp from the service 
 * 2. Validate it against the service 
 * 3. Subtract a random number of days from the time stamp 
 * 4. Get an object interpretation for this time stamp 
 * 5. Check if the resulting year is a leap year
 *
 * For a documentation of the service please visit https://docs.postman-echo.com/
 *
 * @author Bernd Weigel
 *
 */
public class TTimestamp extends RESTTestCase implements NonSiteRelatedTest
{

    @Override
    public void test() throws Throwable
    {
        // Get a new time stamp and store it under the key "timestamp" in the context store.
        // Since this is a quite simple task, it can be done with a SimpleRESTJSONAction.
        new SimpleRESTJSONAction("Get Timestamp")
                        .baseUrl("https://postman-echo.com")
                        .relativeUrl("/time/now")
                        .assertStatus(200)
                        .storeResponseValue("$", "timestamp")
                        .run();

        // Retrieve time stamp from context store.
        final String timestamp = String.valueOf(Context.get().getStored("timestamp"));

        // Validate the new time stamp.
        new SimpleRESTJSONAction("Validate Timestamp")
                        .baseUrl("https://postman-echo.com")
                        .relativeUrl("/time/valid")
                        .param("timestamp", timestamp)
                        .assertStatus(200)
                        .validateEquals("Timestamp not valid.", "$.valid", true)
                        .run();

        // Subtract a random number of days (365..20,000 days ~ 54.8 years) and override the stored
        // time stamp value.
        new SimpleRESTJSONAction("Subtract Random Time")
                        .baseUrl("https://postman-echo.com")
                        .relativeUrl("/time/subtract")
                        .param("timestamp", timestamp)
                        .param("days", String.valueOf(XltRandom.nextInt(365, 20000)))
                        .assertStatus(200)
                        .validateNotEquals("$.difference", timestamp)
                        .storeResponseValue("$.difference", "timestamp")
                        .run();

        // Retrieve new time stamp from context store.
        final String randomizedTimestamp = String.valueOf(Context.get().getStored("timestamp"));

        // Because we are lazy, and don't want to extract the year from the formated time stamp, we
        // ask the service and store the response
        new SimpleRESTJSONAction("Get Object Representation")
                        .baseUrl("https://postman-echo.com")
                        .relativeUrl("/time/object")
                        .param("timestamp", randomizedTimestamp)
                        .assertStatus(200)
                        .storeResponseValue("$.years", "year")
                        .run();

        // Let's check if the random year is a leap year. This time we have do do some calculations
        // inside the action, so we use another approach for the action concept.
        Actions.run("Check Leap Year", t ->
        {
            final HttpResponse response = new HttpRequest().timerName(t)
                            .baseUrl("https://postman-echo.com")
                            .relativeUrl("/time/leap")
                            .param("timestamp", randomizedTimestamp)
                            .fire();

            response.checkStatusCode(200);

            // keep in mind, validation costs cpu power
            // when you leave this demo validation out, it is about 12% more throughput
            // on an 8-core machine in a GCP data center
            // the overall measured runtimes stay the same, because the measurement
            // is taken underneath and does not include this time here
            final String responseContent = response.getContentAsString();

            // ok, get us some response content for validation and use the jsonpath query
            // language for that
            final ReadContext ctx = JsonPath.parse(responseContent);

            final boolean serviceResponse = ctx.read("$.leap", Boolean.class);

            // Now, for validation, we calculate if the year is a leap year and see if we agree to
            // the service about this.

            final int year = (int) Context.get().getStored("year");

            boolean isLeapYear = false;

            // calculate leap year
            if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0))
            {
                isLeapYear = true;
            }

            Assert.assertEquals("Service is lying wheter " + year + " was a leap year or not.", serviceResponse, isLeapYear);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDown()
    {
        super.tearDown();

        // please note: the code below could also be placed in the super class and is mostly placed
        // here for visibility. Still it depends on the actual implenetation purposes

        // you can do alternatively just cleaning of the cookie state if you have any, if you
        // don't have any... don't run that code, because performance testing is performance
        // programming
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
        // if you don't close it, it can reuse the connection and the negotiated keys of TLS
        // that is about 100x (!) faster than closing... but you have state of course, your call!
        if (Context.configuration().closeWebClient)
        {
            this.closeWebClient();
        }
    }
}
