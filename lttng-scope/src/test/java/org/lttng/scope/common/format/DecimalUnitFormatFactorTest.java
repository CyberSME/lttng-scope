/*******************************************************************************
 * Copyright (c) 2016 EfficiOS inc, Michael Jeanson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.common.format;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.text.Format;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Test the {@link DecimalUnitFormat} class
 *
 * @author Michael Jeanson
 */
@RunWith(Parameterized.class)
public class DecimalUnitFormatFactorTest {

    private final @NotNull Format fFormat;

    private final @NotNull Number fNumValue;
    private final @NotNull String fExpected;

    /**
     * Constructor
     *
     * @param value
     *            The numeric value to format
     * @param expected
     *            The expected formatted result
     * @param factor
     *            The multiplication factor to apply before formatting
     */
    public DecimalUnitFormatFactorTest(@NotNull Number value, @NotNull String expected, @NotNull Double factor) {
        fNumValue = value;
        fExpected = expected;
        fFormat = new DecimalUnitFormat(factor);
    }

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { 0, "0", 10.0 },
                { 3, "300", 100.0 },
                { 975, "97.5", 0.1 },
                { 1000, "1 k", 1.0 },
                { 4000, "40", 0.01 },
                { -4000, "-40", 0.01 },
                { -0.04, "-4", 100.0 },
                { 0.002, "20", 10000.0 },
                { 0.0555, "5.5 k", 100000.0 },
                { 0.0004928373928, "49.3 n", 0.0001 },
                { 0.000000251, "251 p", 0.001 },
                { Double.POSITIVE_INFINITY, "∞", 0.001 },
                { Double.MAX_VALUE, "4", Double.MIN_NORMAL},
        });
    }

    /**
     * Test the {@link Format#format(Object)} method
     */
    @Test
    public void testFormat() {
        assertEquals("format value", fExpected, fFormat.format(fNumValue));
    }
}
