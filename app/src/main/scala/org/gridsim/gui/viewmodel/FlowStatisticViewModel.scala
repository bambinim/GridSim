package org.gridsim.gui.viewmodel

import org.gridsim.statistics.FlowStatistic
import scalafx.beans.property.StringProperty

class FlowStatisticViewModel:
  val importedText = StringProperty("Imported: 0.00 kWh")
  val exportedText = StringProperty("Exported: 0.00 kWh")
  val peakImportText = StringProperty("Peak import: 0.00 kWh")
  val peakExportText = StringProperty("Peak export: 0.00 kWh")
  val avgNetFlowText = StringProperty("Avg net flow: 0.00 kWh")

  def update(stats: FlowStatistic): Unit =
    importedText.value = f"Imported: ${stats.totalImported.toDouble}%.2f kWh"
    exportedText.value = f"Exported: ${stats.totalExported.toDouble}%.2f kWh"
    peakImportText.value = f"Peak import: ${stats.peakImport.toDouble}%.2f kWh"
    peakExportText.value = f"Peak export: ${stats.peakExport.toDouble}%.2f kWh"
    avgNetFlowText.value = f"Avg net flow: ${stats.averageNetFlow}%.2f kWh"
