package org.gridsim.gui.viewmodel

import org.gridsim.statistics.SimulationTimeStatistic
import org.gridsim.util.Formatting
import scalafx.beans.property.StringProperty

class SimulationTimeStatisticViewModel:

  private object Labels:
    val Tick = "Tick"
    val Start = "Initial"
    val Simulated = "Elapsed"

  private val UnknownDate = "unknown"

  val tickText = StringProperty(s"${Labels.Tick}: 0 ($UnknownDate)")
  val initialTimeText = StringProperty(s"${Labels.Start}: $UnknownDate")
  val elapsedTimeText = StringProperty(s"${Labels.Simulated}: 0d 0h 0m")

  def update(stats: SimulationTimeStatistic): Unit =
    val totalSeconds = stats.elapsed.toSeconds
    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    val minutes = (totalSeconds % 3600) / 60
    tickText.value = s"${Labels.Tick}: ${stats.tick} (${stats.currentDateTime.map(_.format(Formatting.DateTimeFormatting)).getOrElse(UnknownDate)})"
    initialTimeText.value = s"${Labels.Start}: ${stats.startDateTime.map(_.format(Formatting.DateTimeFormatting)).getOrElse(UnknownDate)}"
    elapsedTimeText.value = s"${Labels.Simulated}: ${days}d ${hours}h ${minutes}m"
