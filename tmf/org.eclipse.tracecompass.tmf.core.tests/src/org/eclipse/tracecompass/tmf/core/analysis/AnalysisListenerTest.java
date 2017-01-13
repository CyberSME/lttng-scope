/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis;

import static org.junit.Assert.assertEquals;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.tests.stubs.analysis.NewModuleListenerStub;
import org.eclipse.tracecompass.tmf.core.tests.stubs.analysis.TestAnalysis;
import org.junit.Test;

/**
 * Test the analysis listener extension point
 *
 * @author Geneviève Bastien
 */
public class AnalysisListenerTest {

    private static final @NonNull String MODULE_GENERIC_ID = "test.id";
    private static final @NonNull String MODULE_GENERIC_NAME = "Test analysis";

    /**
     * Test if the listener was created by using a manually created module
     */
    @Test
    public void testNewModuleListener() {
        TestAnalysis module = new TestAnalysis();

        module.setName(MODULE_GENERIC_NAME);
        module.setId(MODULE_GENERIC_ID);

        int countBefore = NewModuleListenerStub.getModuleCount();
        TmfAnalysisManager.analysisModuleCreated(module);
        /*
         * The listener should have run on this module and the count increment
         * by 1
         */
        assertEquals(countBefore + 1, NewModuleListenerStub.getModuleCount());

        module.dispose();
    }

}