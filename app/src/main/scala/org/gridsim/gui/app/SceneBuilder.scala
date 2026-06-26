package org.gridsim.gui.app

import org.gridsim.core.simulation.{SimulationModel, SimulationState}
import org.gridsim.gui.app.AppEvent.ScenarioLoaded
import org.gridsim.gui.app.Route.{ScenarioSelection, Simulation}
import org.gridsim.gui.controller.ScenarioSelectionController
import org.gridsim.gui.ports.{ScenarioPresetLoader, ScenarioPresetRepository}
import org.gridsim.gui.runtime.SimulationFactory
import org.gridsim.gui.view.ScenarioSelectionView
import scalafx.scene.Parent
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox

class SceneBuilder(
  scenarioRepo: ScenarioPresetRepository,
  scenarioLoader: ScenarioPresetLoader[(SimulationModel, SimulationState)],
  simulationFactory: SimulationFactory.type
):
  def render(route: Route, dispatch: AppEvent => Unit): Parent =
    route match
      case ScenarioSelection =>
        ScenarioSelectionView(
          controller = ScenarioSelectionController(
            scenarioRepo = scenarioRepo,
            loader = scenarioLoader
          ),
          onScenarioLoaded = {
            case (model, state) =>
              val simController = simulationFactory.createSimpleSimulation(model, state)
              dispatch(ScenarioLoaded(simController))
          }
        )
      case Simulation(controller) =>
        new VBox:
          children = Seq(
            new Label("Simulation loaded")
          )


