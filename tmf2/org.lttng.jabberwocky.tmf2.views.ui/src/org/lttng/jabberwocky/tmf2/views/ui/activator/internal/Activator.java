/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.jabberwocky.tmf2.views.ui.activator.internal;

import org.lttng.jabberwocky.common.ui.JabberwockyUIActivator;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends JabberwockyUIActivator {

    private static final String PLUGIN_ID = "org.lttng.jabberwocky.tmf2.views.ui"; //$NON-NLS-1$

    /**
     * Return the singleton instance of this activator.
     *
     * @return The singleton instance
     */
    public static Activator instance() {
        return (Activator) JabberwockyUIActivator.getInstance(PLUGIN_ID);
    }

    /**
     * Constructor
     */
    public Activator() {
        super(PLUGIN_ID);
    }

    @Override
    protected void startActions() {
    }

    @Override
    protected void stopActions() {
    }

}