package org.gridsim.gui.app

import org.gridsim.core.simulation.SimulationController
import org.gridsim.gui.app.AppEvent.ScenarioLoaded
import org.gridsim.gui.app.Route.{ScenarioSelection, Simulation}
import org.gridsim.gui.controller.{ScenarioSelectionController, SimulationGuiController}
import org.gridsim.gui.ports.{ScenarioPresetLoader, ScenarioPresetRepository}
import org.gridsim.gui.view.{ScenarioSelectionView, SimulationView}
import scalafx.scene.Parent
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox

class SceneBuilder(
  scenarioRepo: ScenarioPresetRepository,
  scenarioLoader: ScenarioPresetLoader[SimulationController]
):
  def render(route: Route, dispatch: AppEvent => Unit): Parent =
    route match
      case ScenarioSelection =>
        ScenarioSelectionView(
          controller = ScenarioSelectionController(
            scenarioRepo = scenarioRepo,
            loader = scenarioLoader
          ),
          onScenarioLoaded = { simController =>
            dispatch(ScenarioLoaded(simController))
          }
        )
      case Simulation(controller) =>
        SimulationView(
          controller = SimulationGuiController(controller)
        )


