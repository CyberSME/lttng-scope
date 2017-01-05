/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.latency;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.scatter.AbstractSegmentStoreScatterGraphViewer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternAnalysis;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Displays the latency analysis data in a scatter graph
 *
 * @author Jean-Christian Kouame
 */
public class PatternScatterGraphViewer extends AbstractSegmentStoreScatterGraphViewer {

    private String fAnalysisId;

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite
     * @param title
     *            The view title
     * @param xLabel
     *            The x axis label
     * @param yLabel
     *            The y axis label
     */
    public PatternScatterGraphViewer(@NonNull Composite parent, @NonNull String title, @NonNull String xLabel, @NonNull String yLabel) {
        super(parent, title, xLabel, yLabel);
    }

    @Override
    protected @Nullable ISegmentStoreProvider getSegmentStoreProvider(@NonNull ITmfTrace trace) {
        return fAnalysisId != null ? TmfTraceUtils.getAnalysisModuleOfClass(trace, XmlPatternAnalysis.class, fAnalysisId) : null;
    }

    /**
     * Set the analysis ID and update the view
     *
     * @param analysisId
     *            The analysis ID
     */
    public void updateViewer(String analysisId) {
        if (analysisId != null) {
            fAnalysisId = analysisId;
            initializeDataSource();
        }
    }
}
