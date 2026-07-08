package org.gridsim.gui.app

import org.gridsim.gui.app.AppEvent.{ScenarioLoaded, SimulationExited}
import org.gridsim.gui.app.Route.{ScenarioSelection, Simulation}
import org.gridsim.gui.model.RunningSimulation
import scalafx.scene.Parent
import scalafx.scene.layout.BorderPane

/**
 * Represents the current navigational route/screen in the application.
 */
enum Route:
  /** The scenario list selection view. */
  case ScenarioSelection
  /** The active running simulation dashboard view. */
  case Simulation(running: RunningSimulation)

/**
 * Representation of the application navigation state.
 *
 * @param route the active route/screen
 */
case class AppState(
  route: Route                   
)

/**
 * Global application events that trigger navigational route changes.
 */
enum AppEvent:
  /** Emitted when a scenario is loaded and the simulation page should open. */
  case ScenarioLoaded(running: RunningSimulation)
  /** Emitted when the user exits the simulation and wants to go back to the home selector. */
  case SimulationExited

/**
 * Coordinates screen routing and dispatching navigational events to transition states.
 *
 * @param render function that accepts the current route and an event dispatch callback, returning the root UI component
 */
class AppRouter(
  render: (Route, AppEvent => Unit) => Parent
):
  private var state = AppState(route = ScenarioSelection)
  
  private val rootPane = new BorderPane:
    center = render(state.route, dispatch)
    
  /**
   * The root parent component of the routing layout.
   */
  def root: Parent =
    rootPane
    
  /**
   * Dispatches a navigation event, transitioning app state and rendering the new route.
   *
   * @param event the navigation event triggering the state transition
   */
  def dispatch(event: AppEvent): Unit =
    event match
      case ScenarioLoaded(running) =>
        state = state.copy(route = Simulation(running))
        rootPane.center = render(state.route, dispatch)
      case SimulationExited =>
        state = state.copy(route = ScenarioSelection)
        rootPane.center = render(state.route, dispatch)
