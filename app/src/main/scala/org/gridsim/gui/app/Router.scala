package org.gridsim.gui.app

import org.gridsim.core.simulation.SimulationController
import org.gridsim.gui.app.AppEvent.ScenarioLoaded
import org.gridsim.gui.app.Route.{ScenarioSelection, Simulation}
import scalafx.scene.Parent
import scalafx.scene.layout.BorderPane

enum Route:
  case ScenarioSelection
  case Simulation(controller: SimulationController)
  
case class AppState(
  route: Route                   
)

enum AppEvent:
  case ScenarioLoaded(simController: SimulationController)

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
      case ScenarioLoaded(simController) => 
        state = state.copy(route = Simulation(simController))
        rootPane.center = render(state.route, dispatch)
      
    
