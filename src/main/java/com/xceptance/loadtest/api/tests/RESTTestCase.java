package com.xceptance.loadtest.api.tests;

import java.text.MessageFormat;

import org.junit.Before;
import org.junit.Test;

import com.xceptance.loadtest.api.data.Site;
import com.xceptance.loadtest.api.data.SiteByMarketShare;
import com.xceptance.loadtest.api.util.Context;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.util.XltRandom;
import com.xceptance.xlt.engine.XltWebClient;
import com.xceptance.xlt.engine.httprequest.HttpRequest;

/**
 * Base class of a REST test
 *
 * @author Rene Schwietzke
 */
public abstract class RESTTestCase extends com.xceptance.xlt.api.tests.AbstractTestCase implements SiteByMarketShare
{
    /**
     * The determined site
     */
    private Site site;

    /**
     * The web client that is used by default for performing the requests.
     */
    private static final ThreadLocal<XltWebClient> WEBCLIENT = new ThreadLocal<>()
    {
        @Override
        protected XltWebClient initialValue()
        {
            XltLogger.runTimeLogger.warn("New XltWebClient created");
            return new XltWebClient();
        }
    };

    /**
     * Constructor
     */
    public RESTTestCase()
    {
        super();

        super.__setup();

        // ok, remove the client from the shutdown list so we can recylce it
        Session.getCurrent().removeShutdownListener(WEBCLIENT.get());

        // set the web client in this context
        HttpRequest.setDefaultWebClient(WEBCLIENT.get());

        // Set test name depending if we have sites or not
        setTestName(getSiteSpecificName(getTestName(), getSite().id));

        // this moved here to make sure we see the exceptions
        Context.createContext(
                        XltProperties.getInstance(),
                        Session.getCurrent().getUserName(),
                        getClass().getName(),
                        getSite());
    }

    public static String getSiteSpecificName(final String name, final String siteId)
    {
        if ("default".equals(siteId) == false && "noneSite".equals(siteId) == false)
        {
            // we have something non default
            return MessageFormat.format("{0}_{1}", name, siteId);
        }
        return name;
    }

    /**
     * Returns a random site
     *
     * @return
     */
    public Site getSite()
    {
        return site == null ? site = supplySite() : site;
    }

    /**
     * Test preparation. Nothing to do here by default. Feel free to override.
     *
     * @throws Throwable
     *             thrown on error
     */
    @Before
    public void init() throws Throwable
    {
    }

    /**
     * Overwritten setup to avoid the logger message, this is for the last piece of speed only
     */
    @Override
    public void setUp()
    {
    }

    /**
     * Run the test scenario.
     *
     * @throws Throwable
     */
    @Test
    public void run() throws Throwable
    {
        test();
    }

    /**
     * Main test method.
     *
     * @throws Throwable
     */
    protected abstract void test() throws Throwable;

    /**
     * To avoid closing the connection and restart TLS as well as the low level connection, you can
     * optionally only reset the cookie state of your client. Way lighter!
     */
    public void clearCookies()
    {
        WEBCLIENT.get().getCookieManager().clearCookies();
    }

    /**
     * If you don't need the state reset, don't call it. It closes the client and removes all state
     * such as cookies but also closes the network connection and the TLS session state.
     */
    public void closeWebClient()
    {
        // this is the hard close
        WEBCLIENT.get().close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDown()
    {
        // We don't call super, because it logs just a message and that costs even though
        // it is only important for debugging... speed!!!
        if (!Context.isLoadTest)
        {
            super.tearDown();

            // add some console output to repeat testcase with same random values
            System.out.println();
            System.out.println("To repeat this test case with the same random values, add the following line to your dev.properties file:");
            System.out.println("    com.xceptance.xlt.random.initValue = " + XltRandom.getSeed());
            System.out.println();
        }

    }

}
