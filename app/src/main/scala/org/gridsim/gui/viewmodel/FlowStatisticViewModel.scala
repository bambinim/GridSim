package org.gridsim.gui.viewmodel

import cats.implicits.toShow
import org.gridsim.core.common.{Energy, kwh}
import org.gridsim.statistics.FlowStatistic
import scalafx.beans.property.StringProperty

class FlowStatisticViewModel:

  private object Labels:
    val Imported = "Imported"
    val Exported = "Exported"
    val PeakImport = "Peak import"
    val PeakExport = "Peak export"
    val AvgNetFlow = "Avg net flow"

  val importedText = StringProperty(s"${Labels.Imported}: ${Energy.Zero.show}")
  val exportedText = StringProperty(s"${Labels.Exported}: ${Energy.Zero.show}")
  val peakImportText = StringProperty(s"${Labels.PeakImport}: ${Energy.Zero.show}")
  val peakExportText = StringProperty(s"${Labels.PeakExport}: ${Energy.Zero.show}")
  val avgNetFlowText = StringProperty(s"${Labels.AvgNetFlow}: ${Energy.Zero.show}")

  def update(stats: FlowStatistic): Unit =
    importedText.value = s"${Labels.Imported}: ${stats.totalImported.show}"
    exportedText.value = s"${Labels.Exported}: ${stats.totalExported.show}"
    peakImportText.value = s"${Labels.PeakImport}: ${stats.peakImport.show}"
    peakExportText.value = s"${Labels.PeakExport}: ${stats.peakExport.show}"
    avgNetFlowText.value = s"${Labels.AvgNetFlow}: ${stats.averageNetFlow.show}" // Energy.showEnergy.show(stats.averageNetFlow.kwh)
