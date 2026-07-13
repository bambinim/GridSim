package org.gridsim.gui.app

import org.gridsim.gui.ports.{DslScenarioPresetLoader, DslScenarioPresetRepository}
import scalafx.application.JFXApp3
import scalafx.scene.Scene

/**
 * Main application entry point for the GridSim JavaFX Graphical User Interface.
 *
 * Configures the primary stage, instantiates the repository, loader, router, and wires
 * global CSS stylesheets.
 */
object GuiApp extends JFXApp3:
  /** Initializes the UI routing engine and configures the main window/stage dimensions. */
  override def start(): Unit =
    val renderer = new SceneBuilder(
      scenarioRepo = new DslScenarioPresetRepository,
      scenarioLoader = new DslScenarioPresetLoader
    )
    val router = new AppRouter(
      render = renderer.render
    )

    stage = new JFXApp3.PrimaryStage:
      title = "GridSim"
      scene = new Scene(1300, 700):
        stylesheets.add(getClass.getResource("/gui/gridsim.css").toExternalForm)
        root = router.root
      onCloseRequest = _ => router.stopActiveSimulation()
