package org.gridsim.gui.viewmodel

import javafx.scene.chart as jfxsc

import org.gridsim.statistics.NetFlowHistoryStatistic
import org.gridsim.util.Formatting

import scalafx.collections.ObservableBuffer

class NetFlowChartStatisticViewModel:
  val dataPoints: ObservableBuffer[jfxsc.XYChart.Data[String, Number]] = ObservableBuffer.empty

  def update(history: NetFlowHistoryStatistic): Unit =
    val points = history.samples.map { sample =>
      jfxsc.XYChart.Data[String, Number](sample.dateTime.format(Formatting.DateTimeFormatting), sample.netFlowKwh)
    }
    dataPoints.setAll(points*)
