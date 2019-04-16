package com.xceptance.loadtest.api.tests;

import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.engine.XltWebClient;
import com.xceptance.xlt.engine.httprequest.HttpRequest;

/**
 * Base class of a REST test with recycling of the web client to avoid high start up cost.
 *
 * @author Rene Schwietzke
 */
public abstract class RESTTestCase extends com.xceptance.xlt.api.tests.AbstractTestCase
{
    /**
     * The web client that is used by default for performing the requests.
     */
    private static final ThreadLocal<XltWebClient> WEBCLIENT = new ThreadLocal<XltWebClient>()
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
        // super.tearDown();

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
        // Prefer to handle that in your test case manually by overwriting tearDown()
        // WEBCLIENT.get().close();
    }

}
