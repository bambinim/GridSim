package org.gridsim.gui.viewmodel

import cats.implicits.toShow
import scalafx.beans.property.{ObjectProperty, StringProperty}
import org.gridsim.core.simulation.{SimulationControllerState, SimulationModel}
import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.model.Environment
import org.gridsim.gui.ports.SummaryExtractor
import org.gridsim.util.Formatting

class SimulationSummaryViewModel(model: SimulationModel, extractor: SummaryExtractor = SummaryExtractor()):

  private object Labels:
    val Entities = "Entities"
    val Cables = "Cables"
    val Time = "Time"

  val netFlowText: StringProperty = StringProperty(s"${Energy.Zero.show} Balanced")
  val entitiesText: StringProperty = StringProperty(s"${Labels.Entities}: 0")
  val cablesText: StringProperty = StringProperty(s"${Labels.Cables}: 0")
  val timeText: StringProperty = StringProperty(s"${Labels.Time}: 0")
  val controllerState: ObjectProperty[SimulationControllerState] =
    ObjectProperty(SimulationControllerState.PAUSED)

  def update(
    entityFlows: Map[String, Flow[Energy]],
    env: Environment,
    state: SimulationControllerState
  ): Unit =
    val extracted = extractor.extract(model, entityFlows, env)

    netFlowText.value = formatFlow(extracted.netFlowKind, extracted.netFlowKwh)
    entitiesText.value = s"${Labels.Entities}: ${extracted.entityCount}"
    cablesText.value = s"${Labels.Cables}: ${extracted.cableCount}"
    timeText.value = s"${Labels.Time}: ${extracted.dateTime.format(Formatting.DateTimeFormatting)}"
    controllerState.value = state

  private def formatFlow(flow: Flow[Energy], rawKwh: Double): String =
    val dir = flow match
      case Flow.Surplus(_) => "Exporting"
      case Flow.Deficit(_) => "Importing"
      case Flow.Balanced   => "Balanced"
    f"${Energy(rawKwh).show} $dir"
