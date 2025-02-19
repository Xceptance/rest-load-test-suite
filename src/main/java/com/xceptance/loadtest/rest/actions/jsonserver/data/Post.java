package com.xceptance.loadtest.rest.actions.jsonserver.data;

import com.google.gson.Gson;

/**
 * Our Post POJO
 *
 * @author rschwietzke
 */
public class Post
{
    public String id;
    public String userId;
    public String author;
    public String title;
    public String body;

    @Override
    public String toString()
    {
        return new Gson().toJson(this);
    }
}
