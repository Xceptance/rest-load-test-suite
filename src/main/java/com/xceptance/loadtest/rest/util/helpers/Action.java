package com.xceptance.loadtest.rest.util.helpers;

@FunctionalInterface
public interface Action
{
    public abstract void run(String timerNamer) throws Throwable;
}
