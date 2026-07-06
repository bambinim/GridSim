package org.gridsim.gui.app

import org.gridsim.gui.app.AppEvent.{ScenarioLoaded, SimulationExited}
import org.gridsim.gui.app.Route.{ScenarioSelection, Simulation}
import org.gridsim.gui.model.RunningSimulation
import scalafx.scene.Parent
import scalafx.scene.layout.BorderPane

enum Route:
  case ScenarioSelection
  case Simulation(running: RunningSimulation)
  
  
case class AppState(
  route: Route                   
)

enum AppEvent:
  case ScenarioLoaded(running: RunningSimulation)
  case SimulationExited

class AppRouter(
  render: (Route, AppEvent => Unit) => Parent
):
  private var state = AppState(route = ScenarioSelection)
  
  private val rootPane = new BorderPane:
    center = render(state.route, dispatch)
    
  def root: Parent =
    rootPane
    
  def dispatch(event: AppEvent): Unit =
    event match
      case ScenarioLoaded(running) =>
        state = state.copy(route = Simulation(running))
        rootPane.center = render(state.route, dispatch)
      case SimulationExited =>
        state = state.copy(route = ScenarioSelection)
        rootPane.center = render(state.route, dispatch)
