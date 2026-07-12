package org.gridsim.gui.viewmodel

import scalafx.beans.property.{ObjectProperty, StringProperty}
import org.gridsim.core.simulation.{SimulationControllerState, SimulationModel}
import org.gridsim.gui.ports.SummaryExtractor

class SimulationSummaryViewModel(model: SimulationModel, extractor: SummaryExtractor = SummaryExtractor()):

  private object Labels:
    val Entities = "Entities"
    val Cables = "Cables"
  
  val entitiesText: StringProperty = StringProperty(s"${Labels.Entities}: 0")
  val cablesText: StringProperty = StringProperty(s"${Labels.Cables}: 0")
  val controllerState: ObjectProperty[SimulationControllerState] =
    ObjectProperty(SimulationControllerState.PAUSED)

  def update(state: SimulationControllerState): Unit =
    val extracted = extractor.extract(model)

    entitiesText.value = s"${Labels.Entities}: ${extracted.entityCount}"
    cablesText.value = s"${Labels.Cables}: ${extracted.cableCount}"
    controllerState.value = state
