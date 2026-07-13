package org.gridsim.gui.viewmodel

import cats.implicits.toShow
import org.gridsim.core.common.{Energy, Flow, kwh}
import org.gridsim.statistics.FlowStatistic
import scalafx.beans.property.StringProperty

class FlowStatisticViewModel:

  private object Labels:
    val Current = "Current"
    val AvgNetFlow = "Average"
    val Imported = "Total imported"
    val Exported = "Total exported"
    val PeakImport = "Peak imported"
    val PeakExport = "Peak exported"

  val currentNetFlow = StringProperty(s"${Labels.Current}: ${Flow.balanced.show}")
  val avgNetFlowText = StringProperty(s"${Labels.AvgNetFlow}: ${Energy.Zero.show}")
  val importedText = StringProperty(s"${Labels.Imported}: ${Energy.Zero.show}")
  val exportedText = StringProperty(s"${Labels.Exported}: ${Energy.Zero.show}")
  val peakImportText = StringProperty(s"${Labels.PeakImport}: ${Energy.Zero.show}")
  val peakExportText = StringProperty(s"${Labels.PeakExport}: ${Energy.Zero.show}")

  def update(stats: FlowStatistic): Unit =
    currentNetFlow.value = s"${Labels.Current}: ${stats.current.show}"
    avgNetFlowText.value = s"${Labels.AvgNetFlow}: ${stats.averageNetFlow.show}"
    importedText.value = s"${Labels.Imported}: ${stats.totalImported.show}"
    exportedText.value = s"${Labels.Exported}: ${stats.totalExported.show}"
    peakImportText.value = s"${Labels.PeakImport}: ${stats.peakImport.show}"
    peakExportText.value = s"${Labels.PeakExport}: ${stats.peakExport.show}"
