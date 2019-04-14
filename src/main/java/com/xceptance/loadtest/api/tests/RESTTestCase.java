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
     * {@inheritDoc}
     */
    @Override
    public void tearDown()
    {
        super.tearDown();

        // release all resources so we don't have state
        WEBCLIENT.get().close();
    }

}
