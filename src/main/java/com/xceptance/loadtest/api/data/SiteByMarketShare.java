package com.xceptance.loadtest.api.tests;

import com.xceptance.loadtest.api.data.Site;
import com.xceptance.loadtest.api.data.SiteSupplier;

/**
 *
 * General Interface to use the site specific configuration as well as the automated test
 * distribution by the market share of the site.
 *
 */
public interface SiteByMarketShare
{
    default Site supplySite()
    {
        return SiteSupplier.randomSite().get();
    }
}
