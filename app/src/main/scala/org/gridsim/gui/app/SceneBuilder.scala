package org.gridsim.gui.app

import org.gridsim.gui.app.AppEvent.{ScenarioLoaded, SimulationExited}
import org.gridsim.gui.app.Route.{ScenarioSelection, Simulation}
import org.gridsim.gui.viewmodel.{ScenarioSelectionViewModel, SimulationCoordinator}
import org.gridsim.gui.model.RunningSimulation
import org.gridsim.gui.ports.{ScenarioPresetLoader, ScenarioPresetRepository}
import org.gridsim.gui.view.{ScenarioSelectionView, SimulationView}
import scalafx.scene.Parent

/**
 * Factory class responsible for instantiating the UI View components corresponding to the active route.
 *
 * @param scenarioRepo the repository containing the available presets
 * @param scenarioLoader the loader to construct the running simulation context
 */
class SceneBuilder(
  scenarioRepo: ScenarioPresetRepository,
  scenarioLoader: ScenarioPresetLoader[RunningSimulation]
):
  /**
   * Renders the View component matching the current navigation Route.
   *
   * @param route the current route to render
   * @param dispatch event dispatching callback used by views to trigger state transitions
   * @return the rendered parent node for the requested route
   */
  def render(route: Route, dispatch: AppEvent => Unit): Parent =
    route match
      case ScenarioSelection =>
        ScenarioSelectionView(
          viewModel = ScenarioSelectionViewModel(
            scenarioRepo = scenarioRepo,
            loader = scenarioLoader
          ),
          onScenarioLoaded = { running =>
            dispatch(ScenarioLoaded(running))
          }
        )
      case Simulation(running) =>
        SimulationView(
          coordinator = SimulationCoordinator(
            running = running,
            onExit = () => dispatch(SimulationExited)
          )
        )
