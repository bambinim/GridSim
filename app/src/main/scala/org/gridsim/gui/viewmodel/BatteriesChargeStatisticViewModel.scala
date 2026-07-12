package org.gridsim.gui.viewmodel

import cats.implicits.toShow
import org.gridsim.core.common.Energy
import org.gridsim.statistics.BatteriesChargeStatistic
import scalafx.beans.property.StringProperty

class BatteriesChargeStatisticViewModel:

  private object Labels:
    val AvgCharge = "Average"
    val TotalCharge = "Total"
    val MaxCharge = "Max"

  val averageText = StringProperty(s"${Labels.AvgCharge}: ${Energy.Zero.show}")
  val totalText = StringProperty(s"${Labels.TotalCharge}: ${Energy.Zero.show}")
  val maxText = StringProperty(s"${Labels.MaxCharge}: ${Energy.Zero.show}")

  def update(stats: BatteriesChargeStatistic): Unit =
    averageText.value = s"${Labels.AvgCharge}: ${stats.averageCharge.show}"
    totalText.value = s"${Labels.TotalCharge}: ${stats.totalCharge.show}"
    maxText.value = s"${Labels.MaxCharge}: ${stats.maxCharge.show}"
