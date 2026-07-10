package org.gridsim.gui.viewmodel

import javafx.scene.chart as jfxsc
import org.gridsim.core.statistics.NetFlowHistory
import scalafx.collections.ObservableBuffer

import java.time.format.DateTimeFormatter

class NetFlowChartViewModel:
  val dataPoints: ObservableBuffer[jfxsc.XYChart.Data[String, Number]] = ObservableBuffer.empty

  def update(history: NetFlowHistory): Unit =
    val points = history.samples.map { sample =>
      jfxsc.XYChart.Data[String, Number](sample.dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), sample.netFlowKwh)
    }
    dataPoints.setAll(points*)
