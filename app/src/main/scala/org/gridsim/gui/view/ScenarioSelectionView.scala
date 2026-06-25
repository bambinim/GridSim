package org.gridsim.gui.view

import org.gridsim.gui.controller.ScenarioSelectionController
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Parent
import scalafx.scene.control.{Button, Label, ListView, TextField}
import scalafx.scene.control.ControlIncludes.jfxMultipleSelectionModel2sfx
import scalafx.scene.layout.{HBox, Priority, VBox}

import scala.concurrent.duration.DurationInt

class ScenarioSelectionView[A](
  controller: ScenarioSelectionController[A],
  onScenarioLoaded: A => Unit = (_: A) => ()
) extends VBox(16) with ViewFX:
  override def root: Parent = this

  padding = Insets(24)
  alignment = Pos.TopLeft
  styleClass += "scenario-selection"

  private val scenarios = controller.availableScenarios.toSeq.sortBy(_._2)

  private var state =
    controller.initialScenario

  private val titleLabel =
    new Label("GridSim"):
      styleClass += "title"

  private val subtitleLabel =
    new Label("Select a predefined scenario and start the simulation."):
      styleClass += "subtitle"

  private val scenariosList = new ListView[String](
    ObservableBuffer.from(scenarios.map(_._2))
  ):
    prefHeight = 140
    minHeight = 120
    styleClass += "scenario-list"

  private val tickField = new TextField:
    text = state.tickDuration.toMinutes.toString
    prefWidth = 80
    maxWidth = 80

  private val startButton = new Button("Start"):
    disable = scenarios.isEmpty
    defaultButton = true
    styleClass += "primary-button"
    onAction = _ => startScenario()

  private val messageLabel = new Label("Select a scenario to continue."):
    wrapText = true
    styleClass += "muted-text"

  private val selectedScenarioLabel = new Label("No scenario selected"):
    styleClass += "scenario-name"

  private val selectedScenarioHint = new Label("The scenario topology is fixed by the project DSL."):
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
    messageLabel
  )

  scenariosList.selectionModel().selectedIndex.onChange { (_, _, selectedIndex) =>
    if selectedIndex.intValue >= 0 then
      val (id, name) = scenarios(selectedIndex.intValue)
      selectedScenarioLabel.text = name
      selectedScenarioHint.text =
        s"Preset id: ${id.value}. Parameters are fixed; only tick duration can be changed here."
      messageLabel.text = "Ready to start."
  }

  if scenarios.nonEmpty then
    scenariosList.selectionModel().select(0)

  private def startScenario(): Unit =
    val result =
      for
        _ <- selectCurrentScenario()
        _ <- updateTickDuration()
        loaded <- controller.startSelectedScenario(state)
      yield loaded

    result match
      case Right(loaded) =>
        setMessageStyle("success-message")
        messageLabel.text = "Scenario loaded."
        onScenarioLoaded(loaded)
      case Left(error) =>
        setMessageStyle("error-message")
        messageLabel.text = error

  private def selectCurrentScenario(): Either[String, Unit] =
    val selectedIndex = scenariosList.selectionModel().getSelectedIndex

    if selectedIndex < 0 then
      Left("No scenario selected")
    else
      val selectedId = scenarios(selectedIndex)._1

      controller.selectScenario(state, selectedId).map{ nextState =>
        state = nextState
      }

  private def updateTickDuration(): Either[String, Unit] =
    tickField.text.value.trim.toIntOption match
      case Some(value) =>
        controller.updateTickDuration(state, value.minutes).map { nextState =>
          state = nextState
        }
      case _ =>
        Left("Not a valid tick duration inserted")

  private def sectionLabel(text: String): Label =
    new Label(text):
      styleClass += "section-label"

  private def setMessageStyle(style: String): Unit =
    messageLabel.styleClass.removeAll("muted-text", "success-message", "error-message")
    messageLabel.styleClass += style
