/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.lami.aspect;

import org.jetbrains.annotations.Nullable;
import org.lttng.scope.lami.module.LamiTableEntry;
import org.lttng.scope.lami.types.LamiData;
import org.lttng.scope.lami.types.LamiProcess;

import java.util.Comparator;

/**
 * Aspect for process names
 *
 * @author Philippe Proulx
 */
public class LamiProcessNameAspect extends LamiTableEntryAspect {

    private final int fColIndex;

    /**
     * Constructor
     *
     * @param colName
     *            Column name
     * @param colIndex
     *            Column index
     */
    public LamiProcessNameAspect(String colName, int colIndex) {
        super(colName + " (" + Messages.LamiAspect_Name +')', null); //$NON-NLS-1$
        fColIndex = colIndex;
    }

    @Override
    public boolean isContinuous() {
        return false;
    }

    @Override
    public boolean isTimeStamp() {
        return false;
    }

    @Override
    public @Nullable String resolveString(LamiTableEntry entry) {
        LamiData data = entry.getValue(fColIndex);
        if (data instanceof LamiProcess) {
            return ((LamiProcess) data).getName();
        }
        /* Could be null, unknown, etc. */
        return data.toString();
    }

    @Override
    public @Nullable Number resolveNumber(LamiTableEntry entry) {
        return null;
    }

    @Override
    public Comparator<LamiTableEntry> getComparator() {
        return (o1, o2) -> {
            String s1 = resolveString(o1);
            String s2 = resolveString(o2);

            if (s1 == null && s2 == null) {
                return 0;
            }
            if (s1 == null) {
                return 1;
            }

            if (s2 == null) {
                return -1;
            }

            return s1.compareTo(s2);
        };
    }
}
