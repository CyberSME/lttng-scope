/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.lttng.scope.tmf2.views.core.timegraph.model.provider.TimeGraphModelProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

class StubModelProvider extends TimeGraphModelProvider {

    public static final String ENTRY_NAME_PREFIX = "Entry #";

    private static final int NB_ENTRIES = 20;

    private static final TimeGraphTreeRender TREE_RENDER;

    static {
        List<TimeGraphTreeElement> treeElements = IntStream.range(1, NB_ENTRIES)
            .mapToObj(i -> new TimeGraphTreeElement(ENTRY_NAME_PREFIX + i, Collections.emptyList()))
            .collect(Collectors.toList());
        TREE_RENDER = new TimeGraphTreeRender(treeElements);
    }

    protected StubModelProvider() {
        super("Test", null, null, new StubModelStateProvider(), null);
    }

    @Override
    public TimeGraphTreeRender getTreeRender() {
        return TREE_RENDER;
    }

}
