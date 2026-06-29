package org.gridsim.gui.view

import org.gridsim.gui.model.{FlowDirection, GridNodeViewData, SimulationDashboardState}
import scalafx.scene.Parent
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox

class SimulationSummaryView extends VBox with ViewFX:
  override def root: Parent = this

  private val netFlowLabel =
    new Label("Total flow"):
      styleClass += "title"
      
  private val numEntities =
    new Label("Number of entities")
    
  private val simHours =
    new Label("Simulated hour")

  children = Seq(
    netFlowLabel,
    numEntities,
    simHours
  )
  
  def render(state: SimulationDashboardState): Unit = 
    renderFlowLabel(state.netFlowKwh, state.netFlowKind)
    renderNumEntities(state.nodes)
    renderSimHours(state.hourOfDay)
    
  private def renderFlowLabel(flow: Double, direction: FlowDirection): Unit =
    val dir = direction.toString
    netFlowLabel.text = s"${flow} kWh ${dir}"
    
  private def renderNumEntities(entities: Seq[GridNodeViewData]): Unit =
    numEntities.text = s"Number of entities: ${entities.size}"
    
  private def renderSimHours(hour: Int): Unit =
    simHours.text = s"Hour of day: ${hour}"
  
  
