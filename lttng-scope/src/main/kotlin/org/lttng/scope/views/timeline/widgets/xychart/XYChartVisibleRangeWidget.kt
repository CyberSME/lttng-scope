/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.xychart

import com.efficios.jabberwocky.analysis.eventstats.EventStatsXYChartProvider
import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.context.ViewGroupContext
import com.efficios.jabberwocky.views.xychart.control.XYChartControl
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.scene.Parent
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.Label
import javafx.scene.control.SplitPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import org.lttng.scope.common.clampMin
import org.lttng.scope.project.ProjectFilters
import org.lttng.scope.views.timeline.TimelineWidget
import org.lttng.scope.views.timeline.widgets.xychart.layer.XYChartDragHandlers
import org.lttng.scope.views.timeline.widgets.xychart.layer.XYChartScrollHandlers
import org.lttng.scope.views.timeline.widgets.xychart.layer.XYChartSelectionLayer

/**
 * Widget for the timeline view showing data in a XY-Chart. The contents of the
 * chart will follow the frmework's current visible range and update its display
 * accordingly.
 */
class XYChartVisibleRangeWidget(control: XYChartControl, override val weight: Int) : XYChartWidget(control), TimelineWidget {

    override val name = control.renderProvider.providerName
    override val rootNode: Parent
    override val splitPane: SplitPane

    private val chartArea: Pane

    override val selectionLayer = XYChartSelectionLayer.build(this, 5.0)
    override val dragHandlers = XYChartDragHandlers(this)
    override val scrollHandlers = XYChartScrollHandlers(this)

    /*
     * Apply the XYChart Fitler listener to the Event Count type charts.
     * Since the filter listener is defined in the viewer, and not in the library,
     * it cannot be defined by the model provider itself.
     */
    private val filterListener: ProjectFilters.FilterListener? = if (control.renderProvider is EventStatsXYChartProvider) {
        XYChartEventCountFilterListener(viewContext, control.renderProvider)
    } else {
        null
    }

    init {
        val infoArea = BorderPane(Label(name))
        chartArea = StackPane(chart, selectionLayer)
        splitPane = SplitPane(infoArea, chartArea)
        rootNode = BorderPane(splitPane)
    }

    override fun dispose() {
    }

    override fun getWidgetTimeRange() = viewContext.visibleTimeRange

    override fun mapXPositionToTimestamp(x: Double): Long {
        val vr = viewContext.visibleTimeRange

        val viewWidth = chartPlotArea.width
        if (viewWidth < 1.0) return vr.startTime

        val posRatio = x / viewWidth
        val ts = (vr.startTime + posRatio * vr.duration).toLong()

        /* Clamp the result to the current visible time range. */
        return ts.clampToRange(vr)
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

    // TODO Bind the selection rectangles with the other timeline ones?
    override val selectionRectangle = null
    override val ongoingSelectionRectangle = null

    // ------------------------------------------------------------------------
    // XYChartView
    // ------------------------------------------------------------------------

    override fun clear() {
        /* Nothing to do, the redraw task will remove all series if the trace is null. */
    }

    override fun seekVisibleRange(newVisibleRange: TimeRange) {
        /*
         * Nothing special to do regarding the data, the redraw task will repopulate
         * the charts with the correct data.
         *
         * However we need to redraw the selection rectangle since it probably moved.
         */
        drawSelection(viewContext.selectionTimeRange)
    }

    private inner class RedrawTask : TimelineWidget.TimelineWidgetUpdateTask {

        private var previousVisibleRange = ViewGroupContext.UNINITIALIZED_RANGE

        override fun run() {
            /* Skip redraws if we are in a project-switching operation. */
            if (viewContext.listenerFreeze) return

            val newVisibleRange = viewContext.visibleTimeRange
            if (newVisibleRange == previousVisibleRange) return

            /* Paint a new chart */
            val viewWidth = chartArea.width
            val visibleRange = newVisibleRange.duration
            val resolution = ((visibleRange / viewWidth) * 10L).toLong().clampMin(1)

            val renders = control.renderProvider.generateSeriesRenders(newVisibleRange, resolution, null)
            if (renders.isEmpty()) return

            val seriesData = renders
                    .map {
                        it.data
                                .map { XYChart.Data<Number, Number>(it.x, it.y) }
                                .toCollection(FXCollections.observableArrayList())
                    }
                    .toList()

            /* Determine start and end times of the display range. */
            val start = renders.map { it.range.startTime }.min()!!
            val end = renders.map { it.range.endTime }.max()!!
            val range = TimeRange.of(start, end)

            Platform.runLater {
                chart.data = FXCollections.observableArrayList()
                seriesData.forEach { chart.data.add(XYChart.Series(it)) }

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
