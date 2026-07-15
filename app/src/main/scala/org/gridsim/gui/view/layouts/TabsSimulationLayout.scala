package org.gridsim.gui.view.layouts

import scalafx.scene.Node
import scalafx.scene.control.{Tab, TabPane}
import scalafx.application.Platform

/**
 * A layout strategy that arranges the SimulationView components in a TabPane.
 */
class TabsSimulationLayout extends SimulationLayoutStrategy:

  override def build(graphNode: Node, statsNode: Node): Node =
    val graphTab = new Tab:
      text = "Graph"
      closable = false
      content = graphNode

    val statsTab = new Tab:
      text = "Statistics"
      closable = false
      content = statsNode

    val tabPane = new TabPane:
      tabs = Seq(graphTab, statsTab)

    def layoutTree(node: javafx.scene.Node): Unit =
      node.applyCss()
      node.autosize()
      node match
        case parent: javafx.scene.Parent =>
          parent.getChildrenUnmodifiable.forEach(child => layoutTree(child))
          parent.requestLayout()
          parent.layout()
        case _ => ()

    def refresh(node: Node): Unit =
      Platform.runLater {
        // The selected tab content is attached by the TabPane skin only after
        // the selection event. Run the real layout now, including controls
        // (notably LineChart) that were updated while their tab was hidden.
        layoutTree(tabPane.delegate)
        layoutTree(node.delegate)
        javafx.application.Platform.requestNextPulse()
      }

    tabPane
      .selectionModel()
      .selectedItemProperty()
      .addListener((_, _, selectedTab) =>
        if selectedTab == graphTab.delegate then refresh(graphNode)
        else if selectedTab == statsTab.delegate then refresh(statsNode)
      )

    // Also lay out the initially selected graph tab after the TabPane skin has
    // been attached to the scene.
    refresh(graphNode)
    tabPane
