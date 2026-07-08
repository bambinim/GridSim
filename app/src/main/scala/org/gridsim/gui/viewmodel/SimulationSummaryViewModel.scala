package org.gridsim.gui.viewmodel

import scalafx.beans.property.{ObjectProperty, StringProperty}
import org.gridsim.core.simulation.{SimulationControllerState, SimulationModel}
import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.model.Environment
import org.gridsim.gui.ports.SummaryExtractor

class SimulationSummaryViewModel(model: SimulationModel, extractor: SummaryExtractor = SummaryExtractor()):

  // Observable properties exposed to the View
  val netFlowText: StringProperty = StringProperty("0.00 kWh Balanced")
  val entitiesText: StringProperty = StringProperty("Entities: 0")
  val cablesText: StringProperty = StringProperty("Cables: 0")
  val timeText: StringProperty = StringProperty("Hour of day: 0")
  val controllerState: ObjectProperty[SimulationControllerState] = 
    ObjectProperty(SimulationControllerState.PAUSED)

  def update(
    entityFlows: Map[String, Flow[Energy]],
    env: Environment,
    state: SimulationControllerState
  ): Unit =
    val extracted = extractor.extract(model, entityFlows, env)
    
    netFlowText.value = formatFlow(extracted.netFlowKind, extracted.netFlowKwh)
    entitiesText.value = s"Entities: ${extracted.entityCount}"
    cablesText.value = s"Cables: ${extracted.cableCount}"
    timeText.value = s"Hour of day: ${extracted.hourOfDay}"
    controllerState.value = state

  private def formatFlow(flow: Flow[Energy], rawKwh: Double): String =
    val dir = flow match
      case Flow.Surplus(_) => "Exporting"
      case Flow.Deficit(_) => "Importing"
      case Flow.Balanced   => "Balanced"
    f"$rawKwh%.2f kWh $dir"
