package org.gridsim.gui.controller

import javafx.beans.property.ObjectProperty
import org.gridsim.core.observability.SimulationData
import org.gridsim.core.simulation.{SimulationControllerState, SimulationModel, SimulationState}
import org.gridsim.gui.model.Selection
import org.gridsim.gui.view.EntityDetailsView
import scalafx.scene.Parent

class EntityDetailsPanel(
  model: SimulationModel,
  val selectionProp: ObjectProperty[Selection]                      
) extends SimulationPanel:
  
  private val presenter = EntityDetailsPresenter(model)
  private val view = EntityDetailsView()
  
  override def root: Parent = view.root

  override def renderSnapshot(
    snapshot: SimulationData.SimulationSnapshot, 
    controllerState: SimulationControllerState
  ): Unit =
    val viewState = presenter.mapStateToView(
      selectionProp.getValue, 
      snapshot.entityStates, 
      snapshot.entityFlows, 
      snapshot.environment
    )
    view.render(viewState)

  override def renderCurrent(
    state: SimulationState, 
    controllerState: SimulationControllerState
  ): Unit =
    val viewState = presenter.mapStateToView(
      selectionProp.getValue, 
      state.entityStates, 
      state.entityFlows, 
      state.environment
    )
    view.render(viewState)
