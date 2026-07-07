package org.gridsim.gui.view

import org.gridsim.gui.viewmodel.EntityDetailsViewModel
import org.gridsim.gui.model.DetailsEntity
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Parent
import scalafx.scene.control.{Label, Separator, ScrollPane}
import scalafx.scene.layout.{GridPane, HBox, VBox}

/**
 * View panel for displaying detailed information about a selected grid entity.
 *
 * This panel displays various properties of the entity (like ID, name, current power,
 * components) dynamically updating as the simulation state changes.
 *
 * @param viewModel the viewmodel driving this entity details view
 */
class EntityDetailsView(viewModel: EntityDetailsViewModel) extends ScrollPane with ViewFX:

  fitToWidth = true
  styleClass += "entity-details-scroll-pane"

  private val contentContainer = new VBox(12) {
    styleClass += "entity-details-view"
  }

  content = contentContainer

  /**
   * The root parent component of this view.
   */
  override def root: Parent = this
  
  viewModel.detailsEntityProperty.onChange { (_, _, newState) =>
    render(newState)
  }
  
  render(viewModel.detailsEntityProperty.value)

  private def render(state: DetailsEntity): Unit =
    contentContainer.children.clear()

    if state.id.isEmpty && state.title == "No selection" then
      val placeholder = new VBox:
        alignment = Pos.Center
        minHeight = 150
        styleClass += "details-placeholder"
        children = Seq(
          new Label("No Selection") {
            styleClass += "details-placeholder-title"
          },
          new Label("Select a node or cable in the simulation view to see details.") {
            styleClass += "details-placeholder-subtitle"
            wrapText = true
          }
        )
      contentContainer.children.add(placeholder)
    else
      val card = createEntityCard(state, isNested = false)
      contentContainer.children.add(card)

  private def createEntityCard(entity: DetailsEntity, isNested: Boolean): VBox =
    new VBox(10) {
      styleClass += (if isNested then "entity-card-nested" else "entity-card-main")

      val header: HBox = new HBox(8) {
        alignment = Pos.CenterLeft
        val titleLabel: Label = new Label(entity.title) {
          styleClass += (if isNested then "entity-title-nested" else "entity-title-main")
        }
        children = Seq(titleLabel)
      }

      val separator: Separator = new Separator {
        styleClass += "details-separator"
      }

      val grid: GridPane = new GridPane {
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
