/*
 * Copyright (C) 2017 EfficiOS Inc.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.toolbar;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the package
 *
 * @noreference Messages class
 */
@NonNullByDefault({})
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String arrowSeriesMenuButtonName;

    public static String sfFilterModeMenuButtonName;
    public static String sfSortingModeMenuButtonName;

    public static String sfZoomInActionDescription;
    public static String sfZoomOutActionDescription;
    public static String sfZoomToFullRangeActionDescription;
    public static String sfZoomToSelectionActionDescription;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
