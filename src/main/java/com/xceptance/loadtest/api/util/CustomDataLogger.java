package com.xceptance.loadtest.api.util;

import java.util.function.Supplier;

import com.xceptance.xlt.api.engine.CustomData;
import com.xceptance.xlt.api.engine.Session;

public class CustomDataLogger
{
    // the instance that has a running timer attached
    private final CustomData customData;

    /**
     * Don't make it accessible from the outside
     *
     * @param name
     *            the name to use later
     */
    private CustomDataLogger(final String name)
    {
        this.customData = new CustomData(name);
    }

    /**
     * Start a new logger
     *
     * @param name
     *            the name to use
     * @return this logger with a ticking clock
     */
    public static CustomDataLogger start(final String name)
    {
        return new CustomDataLogger(name);
    }

    /**
     * Stop this logger and report the runtime
     *
     * @return the runtime
     */
    public long stop()
    {
        this.customData.setRunTime();
        this.customData.setFailed(false);

        Session.getCurrent().getDataManager().logDataRecord(this.customData);

        return this.customData.getRunTime();
    }

    /**
     *
     * @param name
     * @param runtime
     */
    public static void log(final String name, final long runtime)
    {
        final CustomData data = new CustomData();
        data.setName(name);
        data.setRunTime(runtime);
        data.setFailed(false);

        Session.getCurrent().getDataManager().logDataRecord(data);
    }

    /**
     * Functional interface for logging custom data runtimes. Just more elegant in the code but not
     * suitable for everything due to the scope of the code block.
     *
     * @param name
     *            the name of the custom data
     * @param task
     *            the task to measure
     */
    public static void log(final String name, final Runnable task)
    {
        final CustomData cd = new CustomData(name);

        try
        {
            task.run();
        }
        catch (final Error e)
        {
            cd.setFailed(false);
            throw e;
        }
        finally
        {
            cd.setRunTime();
            Session.getCurrent().getDataManager().logDataRecord(cd);
        }
    }

    /**
     * Functional interface for logging custom data runtimes. Just more elegant in the code but not
     * suitable for everything due to the scope of the code block.
     *
     * @param name
     *            the name of the custom data
     * @param task
     *            the task to measure
     * @return the result of the task, because it is a supplier
     */
    public static <R> R log(final String name, final Supplier<R> task) throws Throwable
    {
        final CustomData cd = new CustomData(name);

        try
        {
            return task.get();
        }
        catch (final Error e)
        {
            cd.setFailed(false);
            throw e;
        }
        finally
        {
            cd.setRunTime();
            Session.getCurrent().getDataManager().logDataRecord(cd);
        }
    }
}