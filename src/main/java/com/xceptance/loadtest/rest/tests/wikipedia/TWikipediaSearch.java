package com.xceptance.loadtest.rest.tests.wikipedia;

import java.util.Map;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.xceptance.loadtest.api.data.DataSupplier;
import com.xceptance.loadtest.api.tests.RESTTestCase;
import com.xceptance.loadtest.api.util.Context;
import com.xceptance.loadtest.api.util.SimpleRESTJSONAction;
import com.xceptance.xlt.api.util.XltRandom;

import net.minidev.json.JSONArray;

/**
 * Simple site specific example test case for the Wikipedia API. It follows the steps below:
 *
 * 1. Perform Search on Wikimedia API 
 * 2. Continue Search using a configured probability. 
 * 3. Choose random Wikipedia page 
 * 4. Get links which, leads to this page 
 * 5. Choose random link 
 * 6. Repeat steps 4 and 5 in a configured distribution 
 * 7. Get first sentences for the last chosen page.
 *
 * @author Bernd Weigel
 *
 */
public class TWikipediaSearch extends RESTTestCase
{

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void test() throws Throwable
    {
        // Get a random search term from a predefined list
        // (config/data/languages/<LANG_OR_LOCALE>/serchterms.txt).
        final String searchTerm = DataSupplier.searchterms();

        // Perfrom search call again wiki API, store, some values from the response.
        new SimpleRESTJSONAction("Search")
                        .baseUrl(Context.get().configuration.baseUrl)
                        .relativeUrl("/w/api.php")
                        .method(HttpMethod.GET)
                        .header("User-Agent", Context.get().configuration.userAgent)
                        .param("format", "json")
                        .param("action", "query")
                        .param("list", "search")
                        .param("srsearch", searchTerm)
                        .assertStatus(200)
                        .validateExists("$.query.searchinfo.totalhits")
                        .storeResponseValue("$.query.search", "search_result")
                        .storeResponseValue("$.continue.continue", "search_continue")
                        .storeResponseValue("$.continue.sroffset", "search_offset")
                        .run();

        // Choose by configured probability, whether we want to continue the search (next page) or
        // not
        if (Context.configuration().continueSearch.random())
        {
            new SimpleRESTJSONAction("Continue Search")
                            .baseUrl(Context.get().configuration.baseUrl)
                            .relativeUrl("/w/api.php")
                            .method(HttpMethod.GET)
                            .header("User-Agent", Context.get().configuration.userAgent)
                            .param("format", "json")
                            .param("action", "query")
                            .param("list", "search")
                            .param("continue", (String) Context.get().getStored("search_continue"))
                            .param("sroffset", String.valueOf(Context.get().data.store.get("search_offset")))
                            .param("srsearch", searchTerm)
                            .assertStatus(200)
                            .validateExists("$.query.searchinfo.totalhits")
                            .storeResponseValue("$.query.search", "search_result")
                            .run();
        }

        // Get stored search result from the Context store.
        final JSONArray results = (JSONArray) Context.get().data.store.get("search_result");

        // Chose random value (take XLTRandom, to ensure to that the same test can be rerun, given
        // the same initial value is set in the config).
        final Map<String, Object> randomPage = (Map<String, Object>) results.get(XltRandom.nextInt(results.size()));

        String pageId = String.valueOf(randomPage.get("pageid"));
        String title = (String) randomPage.get("title");

        // Open the first incoming link of the selected page for some rounds, according to
        // configured distribution in the sites.yaml or site.yaml.
        for (int i = 0; i < Context.configuration().articleCount.random(); i++)
        {
            new SimpleRESTJSONAction("GetPageLinks")
                            .baseUrl(Context.get().configuration.baseUrl)
                            .relativeUrl("/w/api.php")
                            .method(HttpMethod.GET)
                            .header("User-Agent", Context.get().configuration.userAgent)
                            .param("format", "json")
                            .param("action", "query")
                            .param("prop", "linkshere")
                            .param("titles", title)
                            .validateExists("$.query.pages")
                            .storeResponseValue("$.query.pages." + pageId + ".linkshere[0].title", "next_title")
                            .storeResponseValue("$.query.pages." + pageId + ".linkshere[0].pageid", "next_pageId")
                            .assertStatus(200)
                            .run();

            // get Id an page title, for the next round.
            title = (String) Context.get().data.store.get("next_title");
            pageId = String.valueOf(Context.get().data.store.get("next_pageId"));

        }

        // Retrieve first sentence of the last chosen page.
        new SimpleRESTJSONAction("GetExtracts")
                        .baseUrl(Context.get().configuration.baseUrl)
                        .relativeUrl("/w/api.php")
                        .method(HttpMethod.GET)
                        .header("User-Agent", Context.get().configuration.userAgent)
                        .param("format", "json")
                        .param("exlimit", "5")
                        .param("exintro", "true")
                        .param("explaintext", "true")
                        .param("action", "query")
                        .param("prop", "extracts")
                        .param("titles", title)
                        .assertStatus(200)
                        .validateExists("$.query.pages")
                        .run();

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
