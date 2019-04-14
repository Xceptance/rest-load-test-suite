# SFRA Load and Performance Test Suite

## Overview
The SFRA Test Suite is built on the ideas of the SiteGenesis test suite but with clear reworks in many areas to make it easier to use, maintain, and separate concerns such as API for load testing and the specific load test pieces for SFRA.

## What is so different?
First, SFRA is as storefront that uses mixed concepts aka page load and API style REST data loading and later page assembly on the client side. Sadly, it does not use a proper template concept and mixes both styles, hence a full page load first and during updates, only pieces are switched out. The latter makes it really difficult to automate because it "manually" changes classes and data in the page based on CSS path and some logic based on that. 

To find a partially elegant way and avoid coding all CSS paths and logic of SFRA by hand, we use GSON to create Java data objects and [Freemarker](https://freemarker.apache.org/) to render proper pieces of HTML and switch/copy this into the page. 

Please read the [TODO](TODO.md) for unfinished areas as well as ideas of future improvements. 

## Sites, Regions, and Locale
Before we go into the inner workings, here is the high level business view of one of the big differences to the previous SG suite: It is multi-site by default.

A lot of projects nowadays come with several sites, in several languages per site, and often target regions or markets which have similar properties such as credit cards for instance.

In addition, sometimes the site is similar in language but not in functionality, such as UK and US for instance, where the same search phrases can be used (most likely) but the address format is different.

If you want to do things during runtime based on the site and its properties, you can access the current site context and ask:

```java
Context.get().data.site.id
Context.get().data.site.region
Context.get().data.site.locale
Context.get().data.site.language()
```

In case this is too ambiguous, you can easily extend the `Site` class to directly answer your questions, such as isAPAC() for instance. 

The language is automatically derived from the locale. For instance `en_CA` is `en` aka English and `fr_CA` is `fr` aka French both for Canada.

So the idea is to allow reuse based on region and locale/language while you also define everything for the site itself, if you don't want any reuse. But more later.

### File System
You will find a hierarchy under `data` where the suite is looking for information in order of the fallback (see later). This looks something like that (shortened):

```
config/data/
├── languages
│   ├── default
│   │   ├── firstnames.txt
│   │   ├── lastnames.txt
│   │   └── words.txt
│   ├── en
│   │   ├── localization.yaml
│   │   └── searchterms.txt
│   ├── en_UK
│   │   ├── firstnames.txt
│   │   └── lastnames.txt
│   ├── en_US
│   │   └── stateCodesUS.txt
│   ├── fr_FR
│   │   ├── localization.yaml
│   │   └── searchterms.txt
│   └── zh_CN
│       ├── firstnames.txt
│       ├── lastnames.txt
│       ├── localization.yaml
│       └── searchterms.txt
├── regions
│   ├── APAC
│   ├── default
│   │   ├── flash.yaml
│   │   └── site.yaml
│   ├── Europe
│   └── NorthAmerica
└── sites
    ├── sites.yaml
    ├── China
    │   └── accounts.yaml
    ├── default
    │   ├── creditcards.yaml
    │   └── filters.yaml
    ├── France
    │   └── accounts.yaml
    ├── Italy
    │   └── accounts.yaml
    ├── Japan
    │   └── accounts.yaml
    ├── UK
    │   └── accounts.yaml
    └── US
        ├── accounts.yaml
        └── flash.yaml
```

So we have regions, locales and languages, and sites. Everything except default is free to be defined as needed. This comes from information set in `data/sites/sites.yaml`. Consider this the start point of everything. This file is referenced in the `project.properties` via `general.properties.yaml.global.files` and basically gets the YAML loading started.

You can specify more files there which are loaded as initial defaults before the fallback based handling starts.

But it is important that one file of this contains the site list to use otherwise the site concept is broken. 

```yaml
 # make all sites known upfront       
sites:
    -   id: US
        active: true # in case you want to exclude it from a run 
        locale: en_US # this is the language to be used
        region: NorthAmerica # this is something like country or market
        marketshare: 10
    -   id: UK
        active: false
        locale: en_UK
        region: Europe
        marketshare: 10
```

## Properties
This test suite uses a new concept to deal with properties and test data. Please read the following paragraphs carefully and also keep an eye on everything marked important.

### General
SFRA uses in part your known properties but tries to replace most of it (might not be completely done yet) by introducing a flexible and easy to read and configure YAML based property extension.

This concept has been setup for two reasons:
* avoid boilerplate and support UTF-8
* permit simple overwrite by site, market, or region

### YAML
The YAML properties file are an extension of the existing XLT concept but are not delivered by XLT(!) but by the test suite. Hence some limitations apply. 

**Advantages**
* YAML supports UTF-8
* YAML supports classic comments with #
* YAML avoids redundancy because you specify things by indention
* YAML allows [references](https://en.wikipedia.org/wiki/YAML#Advanced_components) aka you can reuse pieces
* Less boilerplate compared to JSON and easier editable

**Disadvantages**
* Needs spaces not tabs for indentation, it is a little sensitive here 

*Important:* YAML does not like `TAB` at all. Make sure you modify your editor settings to correctly turn a TAB into spaces. YAML also likes indentions because only that make up the struture of a document. 

More about YAML and some easy rules can be found in the [Wikipedia](https://en.wikipedia.org/wiki/YAML).

### Properties Format in YAML
The test suite transforms the YAML properties under the hood into regular properties that XLT and the suite can handle, so only the storage is YAML, not the runtime behavior. Hence what you know about `Configuration.java` or `XltProperties` still applies.

Here are some examples:

**Plain**
```yaml
general:
    host: host.com
    baseUrl: justanurl
    credentials: # Comment here
        username: storefront # Comment there
        password: foobar
```
This translates into:

```
general.host = host.com
general.host.baseUrl = host.com
general.credentials.username = storefront
general.credentials.password = foobar
```


** Enumerations**

```yaml
creditcards:
    - id: Mastercard
      type: Master Card
    - id: Visa
      type: Visa
```

This translates into:

```
creditcards.0.id = Mastercard
creditcards.0.type = Master Card
creditcards.1.id = Visa
creditcards.1.type = Visa
```

*Important:* YAML handles quotes around values automatically. So if you write `foo="bar"` you will get just `bar` as value. If you need the quotes, you have to do `foo="\"bar\""`. Same if you have a single quote or backslash somewhere in the value. 

## Data
Data has been setup in a similar way to properties to permit easy reuse as well as well targeted setup per region, site, or locale/language. Good examples are search phrases for English sites

## Fallback
For data as well as properties, we use the same fallback logic and look for the file or the property until we find it. If we cannot find it, we complain.

1. from the site first (e.g. US)
2. if not found from default site,
3. if not found from region,
4. if not found from default region,
5. if not found from locale (e.g. en_US, as specified for the site)
6. if not found from language (e.g. en taken from en_US)
7. if not found from default language
8. if not found, we fail! No file no fun!

Empty file are permitted in case you specified something that does not have data in that case.

## Suite vs. XLT
Because the new normal is the loading of a lot of properties from YAML file to allow easier editing, property reuse as well as get UTF-8 support easily, it does not correspond fully with the XLT concept of properties.

*Important:* Right now, the classic dev.properties overwrite does not work anymore due to the YAML properties being applied last. This also applies to test.properites which is not longer loaded on top of everything during a load test, rather before the YAML pieces, hence it cannot overwrite what is defined in any YAML file.
 
*Important:* Because XLT sees the YAML file as data files and hence does not back this up as part of a load test run. So a recreation based on settings for a run cannot be done from the reports archive anymore.


## Page Concept
The SFRA suite features a strict (at least we try) page concept with reusable components. This will give us compiler support. We will know what is accessible where.

### Page
The concept basically tries to come up with a common page or pages, in our case  `...pages.GeneralPages` which is the template for a lot of pages with all commonly available components such as MiniCart, Search Bar, Navigation, and more. So HomepagePage, ProductListingPage, CartPage, and more extend from `GeneralPages` and add their own specific components on top, but they never take anything away. 

The Checkout for SFRA features a reduced page which does not have all elements of GeneralPages, hence we set up `CheckoutPages` to declare common elements and use this as base for OrderConfirmationPage, CheckoutEntryPage, and CheckoutPage. Please pay attention to the plural of CheckoutPages vs. CheckoutPage.

When you do not have such a common page structure concept, of course you can compose a single Page and not extend it from any common base. You are totally free here.

*Important:* Page does not really mean page in the sense of a browser loaded and rendered page. That is getting rarer and rarer. See this as screen concept. _What do I see and what can I do at this point in time._

**Base Methods**
Each Page needs to extend `Page` which implements the `PageInterface` and that forces you to provide two implementations.

Imagine the Page as access and validation to the data that currently exists as state within the HtmlUnit/XLT framework. The real DOM and the Page are separated because the XLT does not have any business notion aka what Page is it or could it be. This is what you have to do.

* validate(): Verifies that the page is what we expect. So it knows what it needs to be a page of type X. In most cases, it should call the super class to cover the elements there as well. This validation must not validate response codes or HTML structure, because it does not have proper access to that level of information. 
* is(): It is often important to figure out what page we currently see because we got a response and it could be either or. 

`Page` contains a lot of convince methods so don't have to boilerplate your implementation. So a lot of things you have to do, such as creating or getting elements from a page as well as rendering based on templates and moving it into the page, are part of the Page class.



### Component 


## Context


## Data Concept
The suite tries to employ a stricter concept of data separation as well as data maintenance. Hence we have new means to load data from external sources as well as keep data during runtime.

### Loading
Due to the new data file concept, the standard loading concept of XLT cannot longer be used because it does not know anything about site, regions, and locales. 

The new `DataFileProvider.java` has the responsibility to load files based on the fallback and the context. 

```java
public static Optional<File> dataFileBySite(final Site site, final String fileName);
```

### Access and Storage per User
The Context got a new sub-level to keep the test data separated from configuration as well as make extending it easier. `Context.data` which is an instance of `TestData.java`. This instance is exclusive per running scenario. 

Most data is made public for easier access and less boilerplate. If you need logic to hand out data, you have to add a method for fetching and it should be private again of course. 

For instance you will find the `Site` located here, because this is a per user/scenario state.

If you need something only during development time, make sure you add this to `debugData` and not `data`.

`DebugData` features an internal implementation that carries data, called  `DevelopmentDebugData` where you should put all your data in. DebugData always returns something non-sense or static, while DevelopmentDebugData does the right thing. This automatically handles the `isLoadTest` check for you. See the current implementation for more details.

## Debug Support

### Result Browser Feedback
The result browser output has been enhanced by introducing special noop actions that only are fired during development time. These actions visualize the usage of flows, the nesting of flows, and can also be used to report other data if required.

The property `general.debug.actions` can be used to enabled or disable the enhanced display of flows and flow nesting.

If you want to create your own line, you can use `DebugAction.log()` and pass a message. Please make sure you pass a lazy message concept to avoid that this part is pre-evaluated aka don't concat strings, use `MessageFormat` or `String.printf`. The method already accepts only a supplier, hence itself evaluates lazily. 

```java
DebugAction.log(
    () -> MessageFormat.format("{0} ...", name)
);
```

### Nesting Level
The flows count the nesting level automatically using `debugData` as part of the context.

Take a closer look at `Flow.java:run(String name)` to know more about how to use (if required) the `levelDepth`.

### DebugUrl
The DebugURL Action permits you to call a specific url anywhere. You can specify an absolute or relative url.

```java
new DebugUrl("/s/RefArch/womens/Spring-look-2M.html?lang=en_US").run();
```

This action will be also executed during load testing and is not limited to development only.

### loadDebugUrlOrElse
While the DebugUrl action is an action with a fixed name and does not permit to add additional validation or other things, the `loadDebugUrlOrElse` method permits to overwrite a load activity.

```java
loadDebugUrlOrElse("/s/RefArch/womens/").loadPageByClick(categoryLink);
```

You use this directly in the `doExecute` method and this applies to development only. The loadDebug url is ignored when a load test runs and the regular `loadPage` is performed. 

You can enabled or disable this for development purposes as well by using the property `general.debug.urls"`.

### AdhocFlow

## HPU Extensions
### Filtering

## FAQ
