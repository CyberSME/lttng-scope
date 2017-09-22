/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope

import javafx.application.Application.launch
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.stage.Stage

fun main(args: Array<String>) {
    launch(ScopeApplication::class.java, *args)
}

/**
 * Main application launcher
 */
class ScopeApplication : Application() {

    override fun start(primaryStage: Stage?) {
        primaryStage ?: return

        /* Create the application window */
        val root = ScopeMainWindow()

        with (primaryStage) {
            scene = Scene(root)
            title = "LTTng Scope"

            /* Ensure initial window has proper size and subdivisions. */
            width = 1500.0
            setOnShown { root.onShownCB() }

            setOnCloseRequest {
                Platform.exit()
                System.exit(0)
            }

            show()
        }

    }

}