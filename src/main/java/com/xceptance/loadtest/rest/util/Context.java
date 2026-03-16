package com.xceptance.loadtest.rest.util;

import com.xceptance.loadtest.api.data.Site;
import com.xceptance.loadtest.rest.configuration.Configuration;
import com.xceptance.loadtest.rest.data.TestData;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltProperties;

/**
 * Site specific version of the context, parametrized by the employed
 * Configuration and TestData variants.
 */
public class Context extends com.xceptance.loadtest.addons.util.Context<Configuration, TestData>
{
    /**
     * Creates an instance of the context.
     *
     * @param fullTestClassName The full class name of the test case.
     * @param site              The current site.
     */
    public Context(final String fullTestClassName, final Site site)
    {
        super(XltProperties.getInstance(), Session.getCurrent().getUserName(), fullTestClassName, site,
                Configuration.class, new TestData());

    }


    /**
     * Retrieves an instance of the context.
     *
     * @return The context instance.
     */
    public static Context get()
    {
        return (Context) com.xceptance.loadtest.addons.util.Context.get();
    }

    /**
     * Wrapper method for the data store in the test data. Retrieves values
     * which were stored previously during this test case.
     *
     * @param key
     * @return
     */
    public Object getStored(final String key)
    {
        return testData.store.get(key);
    }

}
