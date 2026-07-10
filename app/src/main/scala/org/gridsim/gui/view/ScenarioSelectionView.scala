package org.gridsim.gui.view

import org.gridsim.gui.model.TickDurationUnit
import org.gridsim.gui.viewmodel.ScenarioSelectionViewModel
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Parent
import scalafx.scene.control.{Button, ComboBox, DatePicker, Label, ListView, Spinner, TextField}
import scalafx.scene.control.ControlIncludes.jfxMultipleSelectionModel2sfx
import scalafx.scene.layout.{HBox, Priority, VBox}
import scalafx.util.StringConverter

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * View panel for selecting and loading preset scenarios.
 *
 * This panel displays a list of available scenarios, shows detailed descriptions
 * for the selected scenario, allows configuring parameters like tick duration, and initiates the simulation.
 *
 * @tparam A the type of the loaded simulation returned when a scenario is successfully started
 * @param viewModel the viewmodel driving this scenario selection view
 * @param onScenarioLoaded callback executed once a scenario has been successfully loaded and started
 */
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

  private val tickAmountField = new TextField:
    text <==> viewModel.tickAmountText
    prefWidth = 60

  private val tickUnitCombo = new ComboBox[TickDurationUnit](ObservableBuffer(TickDurationUnit.values*)):
    value <==> viewModel.tickUnit
    converter = new StringConverter[TickDurationUnit]:
      override def toString(unit: TickDurationUnit): String =
        if unit == null then "" else unit.label
      override def fromString(text: String): TickDurationUnit =
        TickDurationUnit.values.find(_.label == text).orNull
    prefWidth = 160

  private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  private val startDatePicker = new DatePicker:
    promptText = "yyyy-MM-dd"
    converter = new StringConverter[LocalDate]:
      override def toString(date: LocalDate): String =
        if date == null then "" else dateFormatter.format(date)

      override def fromString(text: String): LocalDate =
        if text == null || text.isBlank then null
        else scala.util.Try(LocalDate.parse(text.trim, dateFormatter)).getOrElse(null)
    value <==> viewModel.startDate
    prefWidth = 130

  private val hourField = new TextField:
    text <==> viewModel.startHourText
    prefWidth = 40

  private val minuteField = new TextField:
    text <==> viewModel.startMinuteText
    prefWidth = 40

  private val secondField = new TextField:
    text <==> viewModel.startSecondText
    prefWidth = 40

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
        new Label("Step duration:"),
        tickAmountField,
        tickUnitCombo
      ),
    new HBox(10):
      alignment = Pos.CenterLeft
      children = Seq(
        new Label("Start time:"),
        startDatePicker,
        hourField,
        new Label("h"),
        minuteField,
        new Label("m"),
        secondField,
        new Label("s"),
      ),
    messageLabel,
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
