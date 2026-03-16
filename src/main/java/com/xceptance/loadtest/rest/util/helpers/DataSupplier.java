package com.xceptance.loadtest.rest.util.helpers;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.loadtest.api.data.supplier.DataLineSupplier;

/**
 * Data Handling class, to read test data from predefined files.
 *
 * @author Rene Schwietzke
 */
public class DataSupplier
{
    /**
     * Returns a text composed of several sentences.
     *
     * @param sentenceCount
     *            the number of sentences to use
     * @param removeWhitespace
     *            whether or not any whitespace is to be removed from the string
     * @return a text
     */
    public static String getText(final int sentenceCount, final boolean removeWhitespace)
    {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < sentenceCount; i++)
        {
            if (i > 0)
            {
                b.append(' ');
            }

            b.append(DataLineSupplier.getRandomLine("sentences.txt"));
        }
        return removeWhitespace ? StringUtils.deleteWhitespace(b.toString()) : b.toString();
    }

}
