package org.gridsim.gui.viewmodel

import org.gridsim.statistics.CablesOverloadStatistic
import scalafx.beans.property.StringProperty

class CableOverloadStatisticViewModel:

  private object Labels:
    val OverloadedCablesCount = "Total Overloaded Cables"

  val overloadedText = StringProperty(s"${Labels.OverloadedCablesCount}: 0")

  def update(stats: CablesOverloadStatistic): Unit =
    overloadedText.value = s"${Labels.OverloadedCablesCount}: ${stats.overloadedCableSamples}"
