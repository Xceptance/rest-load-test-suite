package com.xceptance.loadtest.rest.tests.wikipedia;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.xceptance.loadtest.api.data.Account;
import com.xceptance.loadtest.api.tests.RESTTestCase;
import com.xceptance.loadtest.api.util.Context;
import com.xceptance.loadtest.api.util.SimpleRESTJSONAction;

/**
 * Simple site specific example test case for the Wikipedia API. It follows the steps below:
 *
 * 1. Choose account from site specific file (config/data/sites/<SITE>/accounts.csv) 
 * 2. Get Token from Wikipedie 
 * 3. Login into Bot Account
 *
 * @author Bernd Weigel
 *
 */
public class TWikipediaLogin extends RESTTestCase
{
    // Constante name under which the login token will be stored in the name value store of the
    // test context.
    private static final String LOGIN_TOKEN_NAME = "loginToken";

    // The exclusive account to log into the site.
    private Account account;

    /**
     * {@inheritDoc}
     */
    @Override
    public void test() throws Throwable
    {
        // Please Note: The wikimedia API demands a specific user agent. Since it may contain
        // sensitive data like an E-Mail address,
        // the configuration is put into the private-data.yaml which is not part of this test suite.
        // Please rename the "private-data.yaml.template" file to private-data.yaml and insert the
        // according values.
        // Of course this configuration can be put in any yaml configuration file.

        // First of all we need an account. This account needs to be exclusive for this specific
        // test case run, so we won't overlapp with other test case users.
        // A collection of accounts needs to be placed at config/data/sites/<SITE>/accounts.csv
        account = Context.getExclusiveAccountFromFile();

        // The wikimedia API demands a token for a specific request, so we need to retrieve one, and
        // store it for later use.
        new SimpleRESTJSONAction("GetToken")
                        .baseUrl(Context.get().configuration.baseUrl)
                        .relativeUrl("/w/api.php")
                        .header("User-Agent", Context.get().configuration.userAgent)
                        .param("action", "query")
                        .param("meta", "tokens")
                        .param("format", "json")
                        .param("type", "login")
                        .method(HttpMethod.GET)
                        .assertStatus(200)
                        .storeResponseValue("$.query.tokens.logintoken", LOGIN_TOKEN_NAME)
                        .run();

        // The real login call. Submit user, password and the previously stored token
        new SimpleRESTJSONAction("Login")
                        .baseUrl(Context.get().configuration.baseUrl)
                        .relativeUrl("/w/api.php")
                        .method(HttpMethod.POST)
                        .header("User-Agent", Context.get().configuration.userAgent)
                        .param("lgtoken", String.valueOf(
                                        Context.get().getStored(LOGIN_TOKEN_NAME)))
                        .param("format", "json")
                        .param("lgname", account.user)
                        .param("lgpassword", account.password)
                        .param("action", "login")
                        .assertStatus(200)
                        .validateEquals("Login not successfull, check login data in accounts.csv", "$.login.result", "Success")
                        .run();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    @Override
    public void tearDown()
    {
        // Put the account back, since we don't need it anymore and another test case can use it.
        Context.releaseExclusiveAccount(account);

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
