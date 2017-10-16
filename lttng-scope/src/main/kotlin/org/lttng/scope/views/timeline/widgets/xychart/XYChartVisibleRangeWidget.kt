/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.xychart

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.context.ViewGroupContext
import com.efficios.jabberwocky.views.xychart.control.XYChartControl
import com.efficios.jabberwocky.views.xychart.view.XYChartView
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.scene.Parent
import javafx.scene.chart.AreaChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.Label
import javafx.scene.control.SplitPane
import javafx.scene.layout.BorderPane
import org.lttng.scope.views.timeline.TimelineWidget

/**
 * Widget for the timeline view showing data in a XY-Chart. The contents of the
 * chart will follow the frmework's current visible range and update its display
 * accordingly.
 */
class XYChartVisibleRangeWidget(override val control: XYChartControl, override val weight: Int) : XYChartView, TimelineWidget {

    private val modelProvider = control.renderProvider

    override val name = control.renderProvider.providerName
    override val rootNode: Parent
    override val splitPane: SplitPane

    private val chartArea: BorderPane
    private val chart: XYChart<Number, Number>

    init {
        val xAxis = NumberAxis().apply {
            isAutoRanging = false
            isTickMarkVisible = false
            tickUnit = 0.0
        }
        val yAxis = NumberAxis().apply {
            isAutoRanging = true
            isTickLabelsVisible = true
        }

        chart = AreaChart(xAxis, yAxis, null).apply {
            title = null
            isLegendVisible = false
            animated = false
        }

        val infoArea = BorderPane(Label(name))
        chartArea = BorderPane(chart)
        splitPane = SplitPane(infoArea, chartArea)
        rootNode = BorderPane(splitPane)
    }

    override fun dispose() {
    }

    // ------------------------------------------------------------------------
    // TimelineWidget
    // ------------------------------------------------------------------------

    override val timelineWidgetUpdateTask: TimelineWidget.TimelineWidgetUpdateTask = RedrawTask()

    /**
     * Even though the chart updates its data according to the time range, it is not
     * done by using a scroll pane.
     */
    override val timeBasedScrollPane = null

    // TODO
    override val selectionRectangle = null

    // TODO
    override val ongoingSelectionRectangle = null

    // ------------------------------------------------------------------------
    // XYChartView
    // ------------------------------------------------------------------------

    override fun clear() {
        /* Nothing to do, the redraw task will remove all series if the trace is null. */
    }

    override fun drawSelection(selectionRange: TimeRange) {
        // TODO
    }

    override fun seekVisibleRange(newVisibleRange: TimeRange) {
        /*
         * Nothing special to do here, the redraw task will repopulate
         * the charts with the correct data.
         */
    }

    private inner class RedrawTask : TimelineWidget.TimelineWidgetUpdateTask {

        private var previousVisibleRange = ViewGroupContext.UNINITIALIZED_RANGE

        override fun run() {
            val newVisibleRange = viewContext.currentVisibleTimeRange
            if (newVisibleRange == previousVisibleRange) return

            /* Paint a new chart */
            val viewWidth = chartArea.width
            val visibleRange = newVisibleRange.duration
            val resolution = ((visibleRange / viewWidth) * 10L).toLong()
            val render = modelProvider.generateRender(modelProvider.series[0], newVisibleRange, resolution, null)

            val data = render.data
                    .map { XYChart.Data<Number, Number>(it.x, it.y) }
                    .toCollection(FXCollections.observableArrayList())

            Platform.runLater {
                chart.data = FXCollections.observableArrayList()
                val series = XYChart.Series(data)
                chart.data.add(series)

                val range = render.range
                with(chart.xAxis as NumberAxis) {
                    tickUnit = range.duration.toDouble()
                    lowerBound = range.startTime.toDouble()
                    upperBound = range.endTime.toDouble()
                }
            }

            previousVisibleRange = newVisibleRange
        }

    }

}