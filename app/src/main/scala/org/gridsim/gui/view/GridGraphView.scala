package org.gridsim.gui.view

import scalafx.scene.layout.BorderPane
import com.brunomnsilva.smartgraph.graphview.{SmartCircularSortedPlacementStrategy, SmartGraphPanel}
import scalafx.application.Platform
import scalafx.Includes.*
import org.gridsim.gui.viewmodel.GridGraphViewModel
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertexNode
import javafx.beans.binding.Bindings
import javafx.scene.text.Text
import scalafx.scene.Parent

import java.net.URL

/**
 * Mock UI view for displaying the Grid graph. Uses JavaFXSmartGraph to
 * visualize GridEntity nodes and Cable edges.
 */
class GridGraphView(viewModel: GridGraphViewModel) extends BorderPane with ViewFX:
  override def root: Parent = this

  private val graphView = new SmartGraphPanel[String, String](
    viewModel.uiGraph,
    new SmartCircularSortedPlacementStrategy()
  )

  val css: URL = getClass.getResource("/smartgraph.css")
  if css != null then this.stylesheets.add(css.toExternalForm)

  // SmartGraphPanel extends javafx.scene.layout.Pane.
  // We can assign it directly to center because of implicit conversion from JavaFX Node to ScalaFX Node.
  center = graphView

  // SmartGraphPanel requires init() to be called after the scene is visible.
  Platform.runLater {
    graphView.init()
    // Disable physics layout so positions are fixed
    graphView.setAutomaticLayout(false)

    viewModel.uiGraph.vertices().forEach { v =>
      if viewModel.isExternalGrid(v.element()) then
        val vertexNode = graphView
          .getStylableVertex(v)
          .asInstanceOf[SmartGraphVertexNode[String]]
        vertexNode.addStyleClass("external-grid")

        val text = new Text("EG")
        text.getStyleClass.add("external-grid-text")

        // Avoid cyclic bounds dependency by not binding to layoutBoundsProperty
        text
          .xProperty()
          .bind(
            Bindings.createDoubleBinding(
              () => vertexNode.centerXProperty().get() - 10,
              vertexNode.centerXProperty()
            )
          )
        text
          .yProperty()
          .bind(
            Bindings.createDoubleBinding(
              () => vertexNode.centerYProperty().get() + 5,
              vertexNode.centerYProperty()
            )
          )
        vertexNode.getChildren().add(text)
    }

    viewModel.onUpdate = () =>
      Platform.runLater {
        import org.gridsim.core.common.Flow
        viewModel.uiGraph.vertices().forEach { v =>
          val stylableNode = graphView.getStylableVertex(v)
          viewModel.entityFlow(v.element()) match
            case Some(Flow.Deficit(_)) =>
              stylableNode.setStyleInline(
                "-fx-stroke: #e74c3c; -fx-stroke-width: 3;"
              ) // red
            case _ =>
              stylableNode.setStyleInline(
                "-fx-stroke: #2ecc71; -fx-stroke-width: 3;"
              ) // green
        }

        val overloadedIds =
          viewModel.overloadedConnections().map(_.hashCode().toHexString).toSet
        viewModel.uiGraph.edges().forEach { e =>
          val stylableEdge = graphView.getStylableEdge(e)
          if overloadedIds.contains(e.element()) then
            stylableEdge.setStyleInline(
              "-fx-stroke: #e74c3c; -fx-stroke-width: 2;"
            ) // red
          else
            stylableEdge.setStyleInline(
              "-fx-stroke: black; -fx-stroke-width: 2;"
            ) // black
        }

        graphView.update()
      }

    // Add single-click actions for nodes and edges
    viewModel.uiGraph.vertices().forEach { v =>
      val node = graphView.getStylableVertex(v).asInstanceOf[javafx.scene.Node]
      node.setOnMouseClicked(ev => {
        if (ev.getClickCount == 1) {
          viewModel.nodeClicked(v.element())
        }
      })
    }

    viewModel.uiGraph.edges().forEach { e =>
      val edgeNode =
        graphView.getStylableEdge(e).asInstanceOf[javafx.scene.Node]
      edgeNode.setOnMouseClicked(ev => {
        if (ev.getClickCount == 1) {
          viewModel
            .edgeClicked(e.vertices()(0).element(), e.vertices()(1).element())
        }
      })
    }
  }

  // Prevent user from dragging nodes by consuming mouse dragged events
  import scalafx.scene.input.MouseEvent
  graphView.addEventFilter(
    MouseEvent.MouseDragged,
    (e: MouseEvent) => e.consume()
  )
