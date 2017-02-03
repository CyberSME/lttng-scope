/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.lttng.scope.lttng.kernel.core.analysis.os.handlers.internal;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.lttng.scope.lttng.kernel.core.analysis.os.StateValues;
import org.lttng.scope.lttng.kernel.core.trace.layout.ILttngKernelEventLayout;

/**
 * Soft Irq exit handler
 */
public class SoftIrqExitHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public SoftIrqExitHandler(ILttngKernelEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITmfEvent event) throws AttributeNotFoundException {
        Integer cpu = KernelEventHandlerUtils.getCpu(event);
        if (cpu == null) {
            return;
        }

        Integer softIrqId = ((Long) event.getContent().getField(getLayout().fieldVec()).getValue()).intValue();
        int currentThreadNode = KernelEventHandlerUtils.getCurrentThreadNode(cpu, ss);
        /* Put this SoftIRQ back to inactive (= -1) in the resource tree */
        int quark = ss.getQuarkRelativeAndAdd(KernelEventHandlerUtils.getNodeSoftIRQs(cpu, ss), softIrqId.toString());
        long timestamp = KernelEventHandlerUtils.getTimestamp(event);
        if (isSoftIrqRaised(ss.queryOngoingState(quark))) {
            ss.modifyAttribute(timestamp, StateValues.SOFT_IRQ_RAISED_VALUE, quark);
        } else {
            ss.modifyAttribute(timestamp, TmfStateValue.nullValue(), quark);
        }
        List<Integer> softIrqs = ss.getSubAttributes(ss.getParentAttributeQuark(quark), false);
        /* Only set status to running and no exit if ALL softirqs are exited. */
        for (Integer softIrq : softIrqs) {
            if (!ss.queryOngoingState(softIrq).isNull()) {
                return;
            }
        }
        /* Set the previous process back to running */
        KernelEventHandlerUtils.setProcessToRunning(timestamp, currentThreadNode, ss);

        /* Set the CPU status back to "busy" or "idle" */
        KernelEventHandlerUtils.cpuExitInterrupt(timestamp, cpu, ss);
    }

    /**
     * This checks if the running <stong>bit</strong> is set
     *
     * @param state
     *            the state to check
     * @return true if in a softirq. The softirq may be pre-empted by an irq
     */
    private static boolean isSoftIrqRaised(@Nullable ITmfStateValue state) {
        return (state != null &&
                !state.isNull() &&
                (state.unboxInt() & StateValues.CPU_STATUS_SOFT_IRQ_RAISED) == StateValues.CPU_STATUS_SOFT_IRQ_RAISED);
    }

}
