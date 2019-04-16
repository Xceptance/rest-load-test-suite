package com.xceptance.loadtest.rest.tests.postcode;

import org.junit.Assert;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.loadtest.api.tests.RESTTestCase;
import com.xceptance.loadtest.api.util.Actions;
import com.xceptance.xlt.engine.httprequest.HttpRequest;
import com.xceptance.xlt.engine.httprequest.HttpResponse;


public class TRandomPostcode extends RESTTestCase
{
    /**
     * Using the https://postcodes.io/ API
     *
     * @throws Throwable
     */
    @Override
    public void test() throws Throwable
    {
        // just fetch a post code
        Actions.run("Lookup Postcode", t ->
        {
            final HttpResponse r = new HttpRequest().timerName(t).baseUrl("https://api.postcodes.io").relativeUrl("/postcodes/M32 0JG").fire();
            r.checkStatusCode(200);

            final String response = r.getContentAsString();

            // ok, get us the post code and use the jsonpath query language for that
            final ReadContext ctx = JsonPath.parse(response);
            Assert.assertTrue(200 == ctx.read("$.status", Integer.class));
            Assert.assertEquals("M32 0JG", ctx.read("$.result.postcode", String.class));

            // if you fancy regexp, so be it, the XLT RegExUtils caches the regex for efficiency
            Assert.assertEquals("200", RegExUtils.getFirstMatch(response, "\"status\":([0-9]+),", 1));
            Assert.assertTrue(RegExUtils.isMatching(response, "\"postcode\":\"[A-Z0-9]{2,} [A-Z0-9]{3}\","));
        });

        // Ok, extract data and use it later again
        final String postcode = Actions.get("Get Random Postcode", t ->
        {
            final HttpResponse r = new HttpRequest().timerName(t).baseUrl("https://api.postcodes.io").relativeUrl("/random/postcodes").fire();
            r.checkStatusCode(200);

            // ok, we don't have to keep the parsed response here as seen above, because we need it
            // only once!
            return JsonPath.parse(r.getContentAsString()).read("$.result.postcode", String.class);
        });

        // check the that the reverse post code presented is valid using the service
        Actions.run("Validate Postcode", t ->
        {
            final HttpResponse r = new HttpRequest().timerName(t).baseUrl("https://api.postcodes.io").relativeUrl("/postcodes/" + postcode + "/validate").fire();
            r.checkStatusCode(200);

            Assert.assertTrue(JsonPath.parse(r.getContentAsString()).read("$.result", Boolean.class));
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

