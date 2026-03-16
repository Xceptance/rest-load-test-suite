package com.xceptance.loadtest.rest.tests.wikipedia;

import java.util.Optional;

import org.htmlunit.HttpMethod;

import com.xceptance.loadtest.api.data.Account;
import com.xceptance.loadtest.rest.tests.LoadTestCase;
import com.xceptance.loadtest.rest.util.Context;
import com.xceptance.loadtest.rest.util.helpers.SimpleRESTJSONAction;

/**
 * Simple site specific example test case for the Wikipedia API. It follows the steps below:
 *
 * 1. Choose account from site specific file (config/data/sites/<SITE>/accounts.csv) 
 * 2. Get Token from Wikipedia 
 * 3. Login into Bot Account
 *
 * @author Bernd Weigel
 *
 */
public class TWikipediaLogin extends LoadTestCase
{
    // Constant name under which the login token will be stored in the name value store of the
    // test context.
    private static final String LOGIN_TOKEN_NAME = "loginToken";

    // The exclusive account to log into the site.
    private Optional<Account> account;

    /**
     * {@inheritDoc}
     */
    @Override
    public void test() throws Throwable
    {
        // Please Note: The Wikimedia API demands a specific user agent. Since it may contain
        // sensitive data like an E-Mail address,
        // the configuration is put into the private-data.yaml which is not part of this test suite.
        // Please rename the "private-data.yaml.template" file to private-data.yaml and insert the
        // according values.
        // Of course this configuration can be put in any yaml configuration file.

        // First of all we need an account. This account needs to be exclusive for this specific
        // test case run, so we won't overlap with other test case users.
        // A collection of accounts needs to be placed at config/data/sites/<SITE>/accounts.csv
        Context.get().testData.attachAccountFromFile(true);
        account = Context.get().testData.getAccount();

        // The Wikimedia API demands a token for a specific request, so we need to retrieve one, and
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
                        .param("lgname", account.get().email)
                        .param("lgpassword", account.get().password)
                        .param("action", "login")
                        .assertStatus(200)
                        .validateEquals("Login not successfull, check login data in accounts.csv", "$.login.result", "Success")
                        .run();
    }
}
