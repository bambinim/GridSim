package org.gridsim.gui.app

import org.gridsim.gui.controller.ScenarioSelectionController
import org.gridsim.gui.ports.{InMemoryScenarioPresetRepository, MockScenarioPresetLoader}
import org.gridsim.gui.view.ScenarioSelectionView

import scalafx.application.JFXApp3
import scalafx.scene.Scene

object GuiApp extends JFXApp3:
  override def start(): Unit =
    val repository = new InMemoryScenarioPresetRepository
    val loader = new MockScenarioPresetLoader
    val controller = new ScenarioSelectionController(repository, loader)
    val selectionView = new ScenarioSelectionView(controller)

    stage = new JFXApp3.PrimaryStage:
      title = "GridSim"
      scene = new Scene(500, 400):
        stylesheets.add(getClass.getResource("/gui/gridsim.css").toExternalForm)
        root = selectionView.root
