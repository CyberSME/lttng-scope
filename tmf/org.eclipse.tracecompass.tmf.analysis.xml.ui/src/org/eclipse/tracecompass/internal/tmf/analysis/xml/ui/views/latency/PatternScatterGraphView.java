/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.latency;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.TmfXmlUiStrings;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.XmlLatencyViewInfo;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;

/**
 * The scatter graph view for pattern latency
 *
 * @author Jean-Christian Kouame
 */
public class PatternScatterGraphView extends TmfChartView {

    /** The view's ID */
    public static final @NonNull String ID = "org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.scattergraph"; //$NON-NLS-1$

    private final XmlLatencyViewInfo fViewInfo = new XmlLatencyViewInfo(ID);

    private PatternScatterGraphViewer fViewer;

    /**
     * Constructor
     */
    public PatternScatterGraphView() {
        super(ID);
        this.addPartPropertyListener(new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getProperty().equals(TmfXmlUiStrings.XML_LATENCY_OUTPUT_DATA)) {
                    Object newValue = event.getNewValue();
                    if (newValue instanceof String) {
                        String data = (String) newValue;
                        fViewInfo.setViewData(data);
                        loadLatencyView();
                    }
                }
            }
        });
    }

    private void loadLatencyView() {
        if (fViewer != null) {
            fViewer.updateViewer(fViewInfo.getViewAnalysisId());
        }
    }

    @Override
    protected TmfXYChartViewer createChartViewer(Composite parent) {
        PatternScatterGraphViewer viewer = new PatternScatterGraphViewer(checkNotNull(parent), checkNotNull(Messages.PatternLatencyViews_ScatterGraphTitle), checkNotNull(Messages.PatternLatencyViews_ScatterGraphXLabel), checkNotNull(Messages.PatternLatencyViews_ScatterGraphYLabel));
        fViewer = viewer;
        loadLatencyView();
        return viewer;
    }
}
