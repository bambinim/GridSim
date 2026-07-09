package org.gridsim.gui.viewmodel

import javafx.scene.chart as jfxsc
import org.gridsim.core.statistics.NetFlowHistory
import scalafx.collections.ObservableBuffer

class NetFlowChartViewModel:
  val dataPoints: ObservableBuffer[jfxsc.XYChart.Data[Number, Number]] = ObservableBuffer.empty

  def update(history: NetFlowHistory): Unit =
    val points = history.samples.map { sample =>
      jfxsc.XYChart.Data[Number, Number](sample.tick.toSeconds.toDouble, sample.netFlowKwh)
    }
    dataPoints.setAll(points*)
