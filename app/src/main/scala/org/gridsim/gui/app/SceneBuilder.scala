package org.gridsim.gui.app

import org.gridsim.gui.app.AppEvent.ScenarioLoaded
import org.gridsim.gui.app.Route.{ScenarioSelection, Simulation}
import org.gridsim.gui.controller.{ScenarioSelectionController, SimulationCoordinator}
import org.gridsim.gui.model.RunningSimulation
import org.gridsim.gui.ports.{ScenarioPresetLoader, ScenarioPresetRepository}
import org.gridsim.gui.view.{ScenarioSelectionView, SimulationView}
import scalafx.scene.Parent

class SceneBuilder(
  scenarioRepo: ScenarioPresetRepository,
  scenarioLoader: ScenarioPresetLoader[RunningSimulation]
):
  def render(route: Route, dispatch: AppEvent => Unit): Parent =
    route match
      case ScenarioSelection =>
        ScenarioSelectionView(
          controller = ScenarioSelectionController(
            scenarioRepo = scenarioRepo,
            loader = scenarioLoader
          ),
          onScenarioLoaded = { running =>
            dispatch(ScenarioLoaded(running))
          }
        )
      case Simulation(running) =>
        SimulationView(
          coordinator = SimulationCoordinator(running)
        )

