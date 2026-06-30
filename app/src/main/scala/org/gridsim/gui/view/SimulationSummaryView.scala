package org.gridsim.gui.view

import org.gridsim.gui.model.{FlowDirection, SummaryViewState}
import scalafx.scene.Parent
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox

class SimulationSummaryView extends VBox with ViewFX:
  override def root: Parent = this

  private val netFlowLabel =
    new Label("Total flow"):
      styleClass += "title"
      
  private val numEntities =
    new Label("Entities: 0")

  private val numCables =
    new Label("Cables: 0")
    
  private val simHours =
    new Label("Hour of day: 0")

  children = Seq(
    netFlowLabel,
    numEntities,
    numCables,
    simHours
  )
  
  def render(state: SummaryViewState): Unit =
    renderFlowLabel(state.netFlowKwh, state.netFlowKind)
    renderNumEntities(state.entityCount)
    renderNumCables(state.cableCount)
    renderSimHours(state.hourOfDay)
    
  private def renderFlowLabel(flow: Double, direction: FlowDirection): Unit =
    val dir = direction.toString
    netFlowLabel.text = f"$flow%.2f kWh $dir"
    
  private def renderNumEntities(count: Int): Unit =
    numEntities.text = s"Entities: $count"

  private def renderNumCables(count: Int): Unit =
    numCables.text = s"Cables: $count"
    
  private def renderSimHours(hour: Int): Unit =
    simHours.text = s"Hour of day: ${hour}"
  
  
