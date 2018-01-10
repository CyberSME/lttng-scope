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
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.views.xychart.control.XYChartControl
import com.efficios.jabberwocky.views.xychart.model.render.XYChartRender
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineCap
import org.lttng.scope.views.timeline.NavigationAreaWidget
import org.lttng.scope.views.timeline.TimelineWidget
import org.lttng.scope.views.timeline.widgets.xychart.layer.XYChartDragHandlers
import org.lttng.scope.views.timeline.widgets.xychart.layer.XYChartSelectionLayer

/**
 * Widget for the timeline view showing data in a XY-Chart. The chart will
 * display data representing the whole trace, and will display highlighted
 * rectangles representing the current visible and selection time ranges.
 */
class XYChartFullRangeWidget(control: XYChartControl, override val weight: Int) : XYChartWidget(control), NavigationAreaWidget {

    companion object {
        private const val CHART_HEIGHT = 50.0

        private val VISIBLE_RANGE_STROKE_WIDTH = 4.0
        private val VISIBLE_RANGE_ARC = 5.0
        private val VISIBLE_RANGE_STROKE_COLOR = Color.GRAY
        private val VISIBLE_RANGE_FILL_COLOR = Color.LIGHTGRAY.deriveColor(0.0, 1.2, 1.0, 0.6)
    }

    override val rootNode = BorderPane()

    override val selectionLayer = XYChartSelectionLayer.build(this, -10.0)
    override val dragHandlers = XYChartDragHandlers(this)

    private val visibleRangeRect = Rectangle().apply {
        stroke = VISIBLE_RANGE_STROKE_COLOR
        strokeWidth = VISIBLE_RANGE_STROKE_WIDTH
        strokeLineCap = StrokeLineCap.ROUND
        fill = VISIBLE_RANGE_FILL_COLOR
        arcHeight = VISIBLE_RANGE_ARC
        arcWidth = VISIBLE_RANGE_ARC

        y = 0.0
        heightProperty().bind(Bindings.subtract(chart.heightProperty(), VISIBLE_RANGE_STROKE_WIDTH))

        isVisible = false
        viewContext.currentTraceProjectProperty().addListener { _, _, newVal -> isVisible = (newVal != null) }
    }

    init {
        /* Hide the axes in the full range chart */
        with(xAxis) {
            isTickLabelsVisible = false
            opacity = 0.0
        }

        with(yAxis) {
            isTickLabelsVisible = false
            opacity = 0.0
        }

        with(chart) {
            // setTitle(getName())
            padding = Insets(-10.0, -10.0, -10.0, -10.0)
            // setStyle("-fx-padding: 1px;")

            minHeight = CHART_HEIGHT
            prefHeight = CHART_HEIGHT
            maxHeight = CHART_HEIGHT
        }

        rootNode.center = StackPane(chart,
                Pane(visibleRangeRect).apply { isMouseTransparent = true },
                selectionLayer)
    }

    override fun dispose() {
    }

    override fun getWidgetTimeRange() = viewContext.getCurrentProjectFullRange()

    // ------------------------------------------------------------------------
    // TimelineWidget
    // ------------------------------------------------------------------------

    override val name = control.renderProvider.providerName

    override val timelineWidgetUpdateTask: TimelineWidget.TimelineWidgetUpdateTask = RedrawTask()

    /** Not applicable to this widget */
    override val splitPane = null

    /* Not applicable to this widget */
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
        /* Needs + 10 to be properly aligned, not sure why... */
        val xStart = xAxis.getDisplayPosition(newVisibleRange.startTime) + 10.0
        val xEnd = xAxis.getDisplayPosition(newVisibleRange.endTime) + 10.0
        if (xStart == Double.NaN || xEnd == Double.NaN) return

        with(visibleRangeRect) {
            x = xStart
            width = xEnd - xStart
        }
    }

    private inner class RedrawTask : TimelineWidget.TimelineWidgetUpdateTask {

        private var lastTraceProject: TraceProject<*, *>? = null

        override fun run() {
            var newTraceProject = viewContext.currentTraceProject
            if (newTraceProject == lastTraceProject) return

            if (newTraceProject == null) {
                /* Replace the list of series with an empty list */
                Platform.runLater { chart.data = FXCollections.observableArrayList() }
            } else {
                val painted = repaintChart(newTraceProject)
                if (!painted) {
                    newTraceProject = null
                }
            }
            lastTraceProject = newTraceProject
        }

        /**
         * Repaint the whole chart. Should only be necessary if the active trace
         * changes.
         *
         * @return If we have produced a "real" render or not. If not, it might be
         *         because the trace is still being initialized, so we should not
         *         consider it painted.
         */
        private fun repaintChart(traceProject: TraceProject<*, *>): Boolean {
            val viewWidth = rootNode.width
            val traceFullRange = TimeRange.of(traceProject.startTime, traceProject.endTime)
            val resolution = (traceFullRange.duration / viewWidth).toLong()

            val renders = control.renderProvider.generateSeriesRenders(traceFullRange, resolution, null)
            val seriesData = renders
                    .filter { it != XYChartRender.EMPTY_RENDER }
                    .map {
                        it.data.map { XYChart.Data<Number, Number>(it.x, it.y) }
                                /* Hide the symbols */
                                .onEach {
                                    val symbol = Rectangle(0.0, 0.0)
                                    symbol.isVisible = false
                                    it.setNode(symbol)
                                }
                                .toCollection(FXCollections.observableArrayList())
                    }
                    .toList()

            if (seriesData.isEmpty()) return false

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

            return true
        }

    }

}
