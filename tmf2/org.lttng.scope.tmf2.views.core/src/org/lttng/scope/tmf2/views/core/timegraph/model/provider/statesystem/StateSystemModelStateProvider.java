/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.core.timegraph.model.provider.statesystem;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.function.Function;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.MathUtils;
import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.config.ConfigOption;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.states.TimeGraphModelStateProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.LineThickness;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.StateDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.BasicTimeGraphStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.MultiStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;

import com.google.common.collect.Iterables;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.StateSystemDisposedException;
import ca.polymtl.dorsal.libdelorean.interval.ITmfStateInterval;

/**
 * Basic implementation of a {@link TimeGraphModelStateProvider} backed by a state
 * system.
 *
 * @author Alexandre Montplaisir
 */
public class StateSystemModelStateProvider extends TimeGraphModelStateProvider {

    /**
     * The context of a single state interval. Should contain all the
     * information required to generate the state interval in the render (name,
     * color, etc.)
     */
    protected static final class StateIntervalContext {

        /** State system */
        public final ITmfStateSystem ss;
        /** Base tree element */
        public final StateSystemTimeGraphTreeElement baseTreeElement;
        /** Source interval */
        public final ITmfStateInterval sourceInterval;

        /**
         * Full state system query at the start of the interval
         *
         * FIXME Remove this!
         */
        public final List<ITmfStateInterval> fullQueryAtIntervalStart;

        /**
         * Constructor
         *
         * @param ss
         *            State system
         * @param baseTreeElement
         *            Tree element for which the data should be fetched. It may
         *            not correspond directly to the state's tree element, a
         *            relative path may be used, for example for additional data
         *            stored in a separate attribute.
         * @param sourceInterval
         *            The state system interval which will be represented by the
         *            model state interval
         * @param fullQueryAtIntervalStart
         *            Full query at the start of the interval. FIXME Remove
         *            this! This should only be queried on-demand, not for every
         *            single interval
         */
        public StateIntervalContext(ITmfStateSystem ss,
                StateSystemTimeGraphTreeElement baseTreeElement,
                ITmfStateInterval sourceInterval,
                List<ITmfStateInterval> fullQueryAtIntervalStart) {
            this.ss = ss;
            this.baseTreeElement = baseTreeElement;
            this.sourceInterval = sourceInterval;
            this.fullQueryAtIntervalStart = fullQueryAtIntervalStart;
        }
    }

    private final String fStateSystemModuleId;
    private final Function<StateIntervalContext, TimeGraphStateInterval> fIntervalMappingFunction;

    /**
     * This state system here is not necessarily the same as the one in the
     * {@link StateSystemModelProvider}!
     */
    private transient @Nullable ITmfStateSystem fStateSystem = null;

    /**
     * Constructor
     *
     * TODO Maybe merge the various Functions into a single class?
     *
     * @param stateDefinitions
     *            The state definitions used in this provider
     * @param stateSystemModuleId
     *            The ID of the state system from which to fetch the information
     * @param stateNameMappingFunction
     *            Mapping function from state interval context to state name
     * @param labelMappingFunction
     *            Mapping function from state interval context to state label
     * @param colorMappingFunction
     *            Mapping function from state interval context to state color
     * @param lineThicknessMappingFunction
     *            Mapping function from state interval context to line thickness
     * @param propertiesMappingFunction
     *            Mapping function from state interval context to properties
     */
    public StateSystemModelStateProvider(
            List<StateDefinition> stateDefinitions,
            String stateSystemModuleId,
            Function<StateIntervalContext, String> stateNameMappingFunction,
            Function<StateIntervalContext, @Nullable String> labelMappingFunction,
            Function<StateIntervalContext, ConfigOption<ColorDefinition>> colorMappingFunction,
            Function<StateIntervalContext, ConfigOption<LineThickness>> lineThicknessMappingFunction,
            Function<StateIntervalContext, Map<String, String>> propertiesMappingFunction) {

        super(stateDefinitions);

        fStateSystemModuleId = stateSystemModuleId;

        fIntervalMappingFunction = ssCtx -> {
            return new BasicTimeGraphStateInterval(
                    ssCtx.sourceInterval.getStartTime(),
                    ssCtx.sourceInterval.getEndTime(),
                    ssCtx.baseTreeElement,
                    stateNameMappingFunction.apply(ssCtx),
                    labelMappingFunction.apply(ssCtx),
                    colorMappingFunction.apply(ssCtx),
                    lineThicknessMappingFunction.apply(ssCtx),
                    propertiesMappingFunction.apply(ssCtx));
        };

        /*
         * Change listener which will take care of keeping the target state
         * system up to date.
         */
        traceProperty().addListener((obs, oldValue, newValue) -> {
            ITmfTrace trace = newValue;
            if (trace == null) {
                fStateSystem = null;
                return;
            }

            // FIXME Remove the extra thread once we move to Jabberwocky
            Thread thread = new Thread(() -> {
                fStateSystem = TmfStateSystemAnalysisModule.getStateSystem(trace, fStateSystemModuleId);
            });
            thread.start();
        });

    }

    // ------------------------------------------------------------------------
    // Render generation methods
    // ------------------------------------------------------------------------

    @Override
    public TimeGraphStateRender getStateRender(TimeGraphTreeElement treeElement,
            TimeRange timeRange, long resolution, @Nullable FutureTask<?> task) {

        ITmfStateSystem ss = fStateSystem;
        /*
         * Sometimes ss is null with uninitialized or empty views, just keep the model
         * empty.
         */
        if (ss == null
                || (task != null && task.isCancelled())
                /* "Title" entries should be ignored */
                || !(treeElement instanceof StateSystemTimeGraphTreeElement)) {

            return TimeGraphStateRender.EMPTY_RENDER;
        }
        StateSystemTimeGraphTreeElement treeElem = (StateSystemTimeGraphTreeElement) treeElement;

        /* Prepare the state intervals */
        List<TimeGraphStateInterval> intervals;
        try {
            intervals = queryHistoryRange(ss, treeElem,
                    timeRange.getStart(), timeRange.getEnd(), resolution, task);
        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            intervals = Collections.emptyList();
        }

        return new TimeGraphStateRender(timeRange, treeElement, intervals);
    }

    private List<TimeGraphStateInterval> queryHistoryRange(ITmfStateSystem ss,
            StateSystemTimeGraphTreeElement treeElem,
            final long t1, final long t2, final long resolution,
            @Nullable FutureTask<?> task)
            throws AttributeNotFoundException, StateSystemDisposedException {

        /* Validate the parameters. */
        if (t2 < t1 || resolution <= 0) {
            throw new IllegalArgumentException(ss.getSSID() + " Start:" + t1 + ", End:" + t2 + ", Resolution:" + resolution); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        final List<TimeGraphStateInterval> modelIntervals = new LinkedList<>();
        final int attributeQuark = treeElem.getSourceQuark();
        ITmfStateInterval lastAddedInterval = null;

        /* Actual valid start/end time of the range query. */
        long tStart = Math.max(t1, ss.getStartTime());
        long tEnd = Math.min(t2, ss.getCurrentEndTime());

        /*
         * First, iterate over the "resolution points" and keep all matching
         * state intervals.
         */
        for (long ts = tStart; ts <= tEnd - resolution; ts += resolution) {
            /*
             * Skip queries if the corresponding interval was already included
             */
            if (lastAddedInterval != null && lastAddedInterval.getEndTime() >= ts) {
                long nextTOffset = MathUtils.roundToClosestHigherMultiple(lastAddedInterval.getEndTime() - tStart, resolution);
                long nextTs = tStart + nextTOffset;
                if (nextTs == ts) {
                    /*
                     * The end time of the last interval happened to be exactly
                     * equal to the next resolution point. We will go to the
                     * resolution point after that then.
                     */
                    ts = nextTs;
                } else {
                    /* 'ts' will get incremented at next loop */
                    ts = nextTs - resolution;
                }
                continue;
            }

            ITmfStateInterval stateSystemInterval = ss.querySingleState(ts, attributeQuark);

            /*
             * Only pick the interval if it fills the current resolution range,
             * from 'ts' to 'ts + resolution' (or 'ts2').
             */
            long ts2 = ts + resolution;
            if (stateSystemInterval.getStartTime() <= ts && stateSystemInterval.getEndTime() >= ts2) {
                TimeGraphStateInterval interval = ssIntervalToModelInterval(ss, treeElem, stateSystemInterval);
                modelIntervals.add(interval);
                lastAddedInterval = stateSystemInterval;
            }
        }

        /*
         * For the very last interval, we'll use ['tEnd - resolution', 'tEnd']
         * as a range condition instead.
         */
        long ts = Math.max(tStart, tEnd - resolution);
        long ts2 = tEnd;
        if (lastAddedInterval != null && lastAddedInterval.getEndTime() >= ts) {
            /* Interval already included */
        } else {
            ITmfStateInterval stateSystemInterval = ss.querySingleState(ts, attributeQuark);
            if (stateSystemInterval.getStartTime() <= ts && stateSystemInterval.getEndTime() >= ts2) {
                TimeGraphStateInterval interval = ssIntervalToModelInterval(ss, treeElem, stateSystemInterval);
                modelIntervals.add(interval);
            }
        }

        /*
         * 'modelIntervals' now contains all the "real" intervals we will want
         * to display in the view. Poly-filla the holes in between each using
         * multi-state intervals.
         */
        if (modelIntervals.size() < 2) {
            return modelIntervals;
        }

        List<TimeGraphStateInterval> filledIntervals = new LinkedList<>();
        /*
         * Add the first real interval. There might be a multi-state at the
         * beginning.
         */
        long firstRealIntervalStartTime = modelIntervals.get(0).getStartTime();
        if (firstRealIntervalStartTime > tStart) {
            filledIntervals.add(new MultiStateInterval(tStart, firstRealIntervalStartTime - 1, treeElem));
        }
        filledIntervals.add(modelIntervals.get(0));

        for (int i = 1; i < modelIntervals.size(); i++) {
            TimeGraphStateInterval interval1 = modelIntervals.get(i - 1);
            TimeGraphStateInterval interval2 = modelIntervals.get(i);
            long bound1 = interval1.getEndTime();
            long bound2 = interval2.getStartTime();

            /* (we've already inserted 'interval1' on the previous loop.) */
            if (bound1 + 1 != bound2) {
                TimeGraphStateInterval multiStateInterval = new MultiStateInterval(bound1 + 1, bound2 - 1, treeElem);
                filledIntervals.add(multiStateInterval);
            }
            filledIntervals.add(interval2);
        }

        /* Add a multi-state at the end too, if needed */
        long lastRealIntervalEndTime = Iterables.getLast(modelIntervals).getEndTime();
        if (lastRealIntervalEndTime < t2) {
            filledIntervals.add(new MultiStateInterval(lastRealIntervalEndTime + 1, t2, treeElem));
        }

        return filledIntervals;
    }

    private TimeGraphStateInterval ssIntervalToModelInterval(ITmfStateSystem ss,
            StateSystemTimeGraphTreeElement treeElem, ITmfStateInterval interval) {
        List<ITmfStateInterval> fullState;
        try {
            // TODO Big performance improvement low-hanging fruit here
            fullState = ss.queryFullState(interval.getStartTime());
        } catch (StateSystemDisposedException e) {
            fullState = Collections.emptyList();
            e.printStackTrace();
        }
        StateIntervalContext siCtx = new StateIntervalContext(ss, treeElem, interval, fullState);
        return fIntervalMappingFunction.apply(siCtx);
    }

}
