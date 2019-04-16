package com.xceptance.loadtest.api.util;

import com.xceptance.xlt.api.actions.AbstractAction;
import com.xceptance.xlt.engine.SessionImpl;

/**
 * Convenience methods for actions to avoid that you have to program it yourself
 *
 * @author rschwietzke
 */
public class Actions
{
    public static void run(final String timerName, final Action action) throws Throwable
    {
        new AbstractAction(null, timerName)
        {
            @Override
            public void preValidate() throws Exception
            {
            }

            @Override
            protected void execute() throws Exception
            {
                try
                {
                    action.run(this.getTimerName());
                }
                catch (final Throwable e)
                {
                    // sadly, this is needed
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void postValidate() throws Exception
            {
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void run() throws Throwable
            {
                try
                {
                    super.run();
                }
                finally
                {
                    // add an empty "page" as the result of this action
                    SessionImpl.getCurrent().getRequestHistory().add(getTimerName());
                }
            }

        }.run();
    }

    public static <T> T get(final String timerName, final SupplierAction<T> action) throws Throwable
    {
        final AbstractActionWithResult<T> a = new AbstractActionWithResult<>(null, timerName, action);
        a.run();

        return a.result;
    }

    /**
     * Helper class to get the result out of the scope of the AbstractAction
     *
     * @author rschwietzke
     *
     * @param <T>
     *            the return type of the supplier action
     */
    static class AbstractActionWithResult<T> extends AbstractAction
    {
        public T result = null;
        private final SupplierAction<T> action;

        protected AbstractActionWithResult(final AbstractAction previousAction, final String timerName, final SupplierAction<T> action)
        {
            super(previousAction, timerName);
            this.action = action;
        }

        @Override
        public void preValidate() throws Exception
        {
        }

        @Override
        protected void execute() throws Exception
        {
            try
            {
                result = action.get(this.getTimerName());
            }
            catch (final Throwable e)
            {
                // sadly, this is needed
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void postValidate() throws Exception
        {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() throws Throwable
        {
            try
            {
                super.run();
            }
            finally
            {
                // add an empty "page" as the result of this action
                SessionImpl.getCurrent().getRequestHistory().add(getTimerName());
            }
        }
    }
}



