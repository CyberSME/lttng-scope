/*******************************************************************************
 * Copyright (c) 2013, 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.stateprovider;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;

/**
 * Test suite for the xml state providers
 *
 * TODO: instead of using one of the test traces, we should make a custom trace
 * to make sure it covers the different possibilities of the state provider
 *
 * @author Geneviève Bastien
 */
public class StateProviderTest extends XmlProviderTestBase {

    @Override
    protected @NonNull String getAnalysisNodeName() {
        return TmfXmlStrings.STATE_PROVIDER;
    }

    @Override
    protected TmfXmlTestFiles getXmlFile() {
        return TmfXmlTestFiles.VALID_FILE;
    }

    @Override
    protected @NonNull CtfTestTrace getTrace() {
        return CtfTestTrace.KERNEL;
    }

}