package org.gridsim.gui.view

import org.gridsim.gui.controller.EntityDetailsViewModel
import org.gridsim.gui.model.DetailsEntity
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Parent
import scalafx.scene.control.{Label, Separator, ScrollPane}
import scalafx.scene.layout.{GridPane, HBox, VBox}

class EntityDetailsView(viewModel: EntityDetailsViewModel) extends ScrollPane with ViewFX:

  fitToWidth = true
  style = "-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;"

  private val contentContainer = new VBox(12) {
    styleClass += "entity-details-view"
  }

  content = contentContainer

  override def root: Parent = this

  // Bind to ViewModel changes reactively
  viewModel.detailsEntityProperty.onChange { (_, _, newState) =>
    render(newState)
  }

  // Initial render
  render(viewModel.detailsEntityProperty.value)

  private def render(state: DetailsEntity): Unit =
    contentContainer.children.clear()

    if state.id.isEmpty && state.title == "No selection" then
      val placeholder = new VBox:
        alignment = Pos.Center
        minHeight = 150
        style = "-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-border-style: dashed; -fx-padding: 20px;"
        children = Seq(
          new Label("No Selection") {
            style = "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #9ca3af;"
          },
          new Label("Select a node or cable in the simulation view to see details.") {
            style = "-fx-font-size: 12px; -fx-text-fill: #9ca3af; -fx-wrap-text: true; -fx-text-alignment: center;"
          }
        )
      contentContainer.children.add(placeholder)
    else
      val card = createEntityCard(state, isNested = false)
      contentContainer.children.add(card)

  private def createEntityCard(entity: DetailsEntity, isNested: Boolean): VBox =
    new VBox(10) {
      styleClass += (if isNested then "entity-card-nested" else "entity-card-main")

      val header = new HBox(8) {
        alignment = Pos.CenterLeft
        val titleLabel = new Label(entity.title) {
          styleClass += (if isNested then "entity-title-nested" else "entity-title-main")
        }
        children = Seq(titleLabel)
      }

      val separator = new Separator {
        style = "-fx-padding: 2px 0 2px 0;"
      }

      val grid = new GridPane {
        hgap = 16
        vgap = 6
        padding = Insets(4, 0, 4, 0)
      }

      entity.fields.zipWithIndex.foreach { case (field, idx) =>
        val nameLabel = new Label(field.field) {
          styleClass += "entity-field-name"
        }
        val valLabel = new Label(field.value) {
          styleClass += "entity-field-value"
        }
        grid.add(nameLabel, 0, idx)
        grid.add(valLabel, 1, idx)
      }

      children = Seq(header, separator, grid)

      if entity.components.nonEmpty then
        val compHeader = new Label("INSTALLED COMPONENTS") {
          styleClass += "components-header"
        }
        val compList = new VBox(8) {
          children = entity.components.map(comp => createEntityCard(comp, isNested = true))
        }
        children.addAll(compHeader, compList)
    }
