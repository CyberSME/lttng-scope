///*******************************************************************************
// * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
// *
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *******************************************************************************/
//
//package org.lttng.scope.lttng.ust.core.trace;
//
//import org.eclipse.jdt.annotation.NonNullByDefault;
//import org.eclipse.jdt.annotation.Nullable;
//import org.eclipse.tracecompass.ctf.core.event.IEventDeclaration;
//import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
//import org.eclipse.tracecompass.ctf.tmf.core.event.CtfTmfEvent;
//import org.eclipse.tracecompass.ctf.tmf.core.trace.CtfTmfTrace;
//import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;
//import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfSourceLookup;
//import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
//import org.lttng.scope.lttng.ust.core.analysis.debuginfo.aspect.UstDebugInfoSourceAspect;
//
///**
// * Event type for use in LTTng-UST traces.
// *
// * @author Alexandre Montplaisir
// */
//@NonNullByDefault
//public class LttngUstEvent extends CtfTmfEvent implements ITmfSourceLookup {
//
//    /**
//     * Default constructor. Only for use by extension points, should not be
//     * called directly.
//     */
//    @Deprecated
//    public LttngUstEvent() {
//        super();
//    }
//
//    /**
//     * Constructor
//     *
//     * @param trace
//     *            The trace to which this event belongs
//     * @param rank
//     *            The rank of the event
//     * @param timestamp
//     *            The timestamp
//     * @param channel
//     *            The CTF channel of this event
//     * @param cpu
//     *            The event's CPU
//     * @param declaration
//     *            The event declaration
//     * @param eventDefinition
//     *            The event definition
//     */
//    protected LttngUstEvent(CtfTmfTrace trace, long rank, ITmfTimestamp timestamp,
//            String channel, int cpu, IEventDeclaration declaration, IEventDefinition eventDefinition) {
//        super(trace, rank, timestamp, channel, cpu, declaration, eventDefinition);
//    }
//
//    @Override
//    public @Nullable ITmfCallsite getCallsite() {
//        return UstDebugInfoSourceAspect.INSTANCE.resolve(this);
//    }
//}
