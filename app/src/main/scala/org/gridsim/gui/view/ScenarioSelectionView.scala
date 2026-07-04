package org.gridsim.gui.view

import org.gridsim.gui.viewmodel.ScenarioSelectionViewModel
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Parent
import scalafx.scene.control.{Button, Label, ListView, TextField}
import scalafx.scene.control.ControlIncludes.jfxMultipleSelectionModel2sfx
import scalafx.scene.layout.{HBox, Priority, VBox}

class ScenarioSelectionView[A](
  viewModel: ScenarioSelectionViewModel[A],
  onScenarioLoaded: A => Unit = (_: A) => ()
) extends VBox(16) with ViewFX:
  override def root: Parent = this

  padding = Insets(24)
  alignment = Pos.TopLeft
  styleClass += "scenario-selection"

  private val titleLabel =
    new Label("GridSim"):
      styleClass += "title"

  private val subtitleLabel =
    new Label("Select a predefined scenario and start the simulation."):
      styleClass += "subtitle"

  private val scenariosList = new ListView[String](viewModel.scenariosNames):
    prefHeight = 140
    minHeight = 120
    styleClass += "scenario-list"

  private val tickField = new TextField:
    text <==> viewModel.tickDurationText
    prefWidth = 80
    maxWidth = 80

  private val startButton = new Button("Start"):
    disable <== viewModel.isStartDisabled
    defaultButton = true
    styleClass += "primary-button"
    onAction = _ => viewModel.startScenario().foreach(onScenarioLoaded)

  private val messageLabel = new Label():
    text <== viewModel.messageText
    wrapText = true
    styleClass += "muted-text"

  private val selectedScenarioLabel = new Label():
    text <== viewModel.selectedScenarioName
    styleClass += "scenario-name"

  private val selectedScenarioHint = new Label():
    text <== viewModel.selectedScenarioHint
    wrapText = true
    styleClass += "muted-text"

  children = Seq(
    titleLabel,
    subtitleLabel,
    new HBox(16):
      alignment = Pos.TopLeft
      children = Seq(
        new VBox(8):
          prefWidth = 220
          children = Seq(
            sectionLabel("Available scenarios"),
            scenariosList
          ),
        new VBox(8):
          hgrow = Priority.Always
          prefWidth = 260
          styleClass += "details-panel"
          children = Seq(
            sectionLabel("Scenario details"),
            selectedScenarioLabel,
            selectedScenarioHint
          )
      ),
    new HBox(10):
      alignment = Pos.CenterLeft
      children = Seq(
        new Label("Tick duration (minutes)"),
        tickField
      ),
    new HBox:
      alignment = Pos.CenterRight
      children = Seq(startButton),
  )

  scenariosList.selectionModel().selectedIndex.onChange { (_, _, selectedIndex) =>
    viewModel.selectScenario(selectedIndex.intValue)
  }

  viewModel.messageStyleClass.onChange { (_, _, newStyle) =>
    messageLabel.styleClass.removeAll("muted-text", "success-message", "error-message")
    messageLabel.styleClass += newStyle
  }

  if viewModel.scenarios.nonEmpty then
    scenariosList.selectionModel().select(0)

  private def sectionLabel(text: String): Label =
    new Label(text):
      styleClass += "section-label"
