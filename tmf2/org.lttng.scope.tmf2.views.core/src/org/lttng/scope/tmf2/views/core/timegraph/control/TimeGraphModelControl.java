/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.core.timegraph.control;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelRenderProvider;
import org.lttng.scope.tmf2.views.core.timegraph.view.TimeGraphModelView;

import com.google.common.annotations.VisibleForTesting;

public final class TimeGraphModelControl {

    /** Value representing uninitialized timestamps */
    public static final long UNINITIALIZED = -1;

    private final ITimeGraphModelRenderProvider fRenderProvider;
    private final SignallingContext fSignallingContext;

    private @Nullable TimeGraphModelView fView = null;
    private @Nullable ITmfTrace fCurrentTrace = null;

    private long fFullTimeGraphStartTime = 1;
    private long fFullTimeGraphEndTime = 1;

    private long fLatestVisibleRangeStartTime = UNINITIALIZED;
    private long fLatestVisibleRangeEndTime = UNINITIALIZED;

    public TimeGraphModelControl(ITimeGraphModelRenderProvider renderProvider) {
        fRenderProvider = renderProvider;
        fSignallingContext = new SignallingContext(this);
    }

    public void attachView(TimeGraphModelView view) {
        fView = view;
    }

    @Nullable TimeGraphModelView getView() {
        return fView;
    }

    public void dispose() {
        if (fView != null) {
            fView.dispose();
        }
        fSignallingContext.dispose();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Nullable ITmfTrace getCurrentTrace() {
        return fCurrentTrace;
    }

    public long getFullTimeGraphStartTime() {
        return fFullTimeGraphStartTime;
    }

    public long getFullTimeGraphEndTime() {
        return fFullTimeGraphEndTime;
    }

    public long getVisibleTimeRangeStart() {
        return fLatestVisibleRangeStartTime;
    }

    public long getVisibleTimeRangeEnd() {
        return fLatestVisibleRangeEndTime;
    }

    public ITimeGraphModelRenderProvider getModelRenderProvider() {
        return fRenderProvider;
    }

    // ------------------------------------------------------------------------
    // Control -> View operations
    // ------------------------------------------------------------------------

    public synchronized void initializeForTrace(@Nullable ITmfTrace trace) {
        fCurrentTrace = trace;
        fRenderProvider.setTrace(trace);

        TimeGraphModelView view = fView;
        if (view != null) {
            view.clear();
        }

        if (trace == null) {
            /* View will remain cleared, good */
            return;
        }

        long traceStartTime = trace.getStartTime().toNanos();
        long traceEndTime = trace.getEndTime().toNanos();

        /* The window's start time is the same as the trace's start time initially */
        long windowStartTime = traceStartTime;
        long windowEndtime = Math.min(traceEndTime, traceStartTime + trace.getInitialRangeOffset().toNanos());

        setTimeGraphAreaRange(traceStartTime, traceEndTime);
        seekVisibleRange(windowStartTime, windowEndtime);
    }

    public void repaintCurrentArea() {
        ITmfTrace trace = fCurrentTrace;
        long start = fLatestVisibleRangeStartTime;
        long end = fLatestVisibleRangeEndTime;
        if (trace == null || start == UNINITIALIZED || end == UNINITIALIZED) {
            return;
        }

        TimeGraphModelView view = fView;
        if (view != null) {
            view.clear();
            view.seekVisibleRange(fLatestVisibleRangeStartTime, fLatestVisibleRangeEndTime);
        }
    }

    void seekVisibleRange(long visibleWindowStartTime, long visibleWindowEndTime) {
        checkWindowTimeRange(visibleWindowStartTime, visibleWindowEndTime);
        updateLatestVisibleRange(visibleWindowStartTime, visibleWindowEndTime);

        TimeGraphModelView view = fView;
        if (view != null) {
            view.seekVisibleRange(visibleWindowStartTime, visibleWindowEndTime);
        }
    }

    void drawSelection(long selectionStartTime, long selectionEndTime) {
        checkWindowTimeRange(selectionStartTime, selectionEndTime);

        TimeGraphModelView view = fView;
        if (view != null) {
            view.drawSelection(selectionStartTime, selectionEndTime);
        }
    }

    void updateLatestVisibleRange(long visibleWindowStartTime, long visibleWindowEndTime) {
        fLatestVisibleRangeStartTime = visibleWindowStartTime;
        fLatestVisibleRangeEndTime = visibleWindowEndTime;
    }

    /**
     * Recompute the total virtual size of the time graph area, and assigns the
     * given timestamps as the start and end positions.
     *
     * All subsquent operations (seek, paint, etc.) that use timestamp expect
     * these timestamps to be within the range passed here!
     *
     * Should be called when the trace changes, or the trace's total time range
     * is updated (while indexing, or in live cases).
     */
    void setTimeGraphAreaRange(long fullAreaStartTime, long fullAreaEndTime) {
        checkTimeRange(fullAreaStartTime, fullAreaEndTime);

        if (fFullTimeGraphStartTime == fullAreaStartTime &&
                fFullTimeGraphEndTime == fullAreaEndTime) {
            /* No need to update */
            return;
        }

        fFullTimeGraphStartTime = fullAreaStartTime;
        fFullTimeGraphEndTime = fullAreaEndTime;
    }

    // ------------------------------------------------------------------------
    // View -> Control operations (Control external API)
    // ------------------------------------------------------------------------

    public void updateTimeRangeSelection(long startTime, long endTime) {
        fSignallingContext.sendTimeRangeSelectionUpdate(startTime, endTime);
    }

    public void updateVisibleTimeRange(long startTime, long endTime, boolean echo) {
        checkTimeRange(startTime, endTime);
        fSignallingContext.sendVisibleWindowRangeUpdate(startTime, endTime, echo);
    }

    // ------------------------------------------------------------------------
    // Utils
    // ------------------------------------------------------------------------

    private static void checkTimeRange(long rangeStart, long rangeEnd) {
        if (rangeStart > rangeEnd) {
            throw new IllegalArgumentException("Time range start " + rangeStart + " is after its end time " + rangeEnd); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (rangeStart < 0 || rangeEnd < 0) {
            throw new IllegalArgumentException("One of the time range bounds is negative"); //$NON-NLS-1$
        }
        if (rangeStart == Long.MAX_VALUE) {
            throw new IllegalArgumentException("You are trying to make me believe the range starts at " + //$NON-NLS-1$
                    rangeStart + ". I do not believe you."); //$NON-NLS-1$
        }
        if (rangeEnd == Long.MAX_VALUE) {
            throw new IllegalArgumentException("You are trying to make me believe the range ends at " + //$NON-NLS-1$
                    rangeEnd + ". I do not believe you."); //$NON-NLS-1$
        }
    }

    private void checkWindowTimeRange(long windowStartTime, long windowEndTime) {
        checkTimeRange(windowStartTime, windowEndTime);

        if (windowStartTime < fFullTimeGraphStartTime) {
            throw new IllegalArgumentException("Requested window start time: " + windowStartTime +
                    " is smaller than trace start time " + fFullTimeGraphStartTime);
        }
        if (windowEndTime > fFullTimeGraphEndTime) {
            throw new IllegalArgumentException("Requested window end time: " + windowEndTime +
                    " is greater than trace end time " + fFullTimeGraphEndTime);
        }
    }

    // ------------------------------------------------------------------------
    // Test utils
    // ------------------------------------------------------------------------

    @VisibleForTesting
    public void prepareWaitForNextSignal() {
        fSignallingContext.prepareWaitForNextSignal();
    }

    @VisibleForTesting
    public void waitForNextSignal() {
        fSignallingContext.waitForNextSignal();
    }
}
