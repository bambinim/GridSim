package org.gridsim.gui.view

import javafx.scene.chart as jfxsc
import org.gridsim.gui.viewmodel.NetFlowChartViewModel
import scalafx.collections.ObservableBuffer
import scalafx.scene.Parent
import scalafx.scene.chart.{LineChart, CategoryAxis, NumberAxis}

class NetFlowChartView(viewModel: NetFlowChartViewModel) extends LineChart[String, Number](
  CategoryAxis("Time"),
  NumberAxis("Net flow (kWh)")
) with ViewFX:
  override def root: Parent = this

  styleClass += "flow-chart"

  title = "Net flow history (recent ticks)"
  createSymbols = false
  animated = false

  private val series = new jfxsc.XYChart.Series[String, Number]()
  series.setName("Net flow")
  series.setData(viewModel.dataPoints)

  data = ObservableBuffer(series)
