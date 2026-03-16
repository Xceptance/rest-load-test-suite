package com.xceptance.loadtest.rest.util.helpers;

@FunctionalInterface
public interface SupplierAction<T>
{
    public abstract T get(String timerName) throws Throwable;
}
