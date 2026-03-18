package com.xceptance.loadtest.rest.tests;

import com.xceptance.loadtest.api.tests.HtmlUnitLoadTestCase;
import com.xceptance.loadtest.rest.util.Context;

/**
 * Base class for HtmlUnit based test cases, taking care of site specific context initialization and shutdown.
 */
public abstract class LoadTestCase extends HtmlUnitLoadTestCase
{
    /**
     * Creates and initializes our site specific load test case.
     */
    public LoadTestCase()
    {
        super();

        // Create and attach context instance
        Context.attach(new Context(getClass().getName(), getSite()));
    }

    /**
     * Shuts down the test case.
     */
    @Override
    public void tearDown()
    {
        // Release context instance
        Context.get().releaseContext();

        // Shut down test case
        super.tearDown();
    }

}