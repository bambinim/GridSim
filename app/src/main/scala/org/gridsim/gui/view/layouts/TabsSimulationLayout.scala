package org.gridsim.gui.view.layouts

import scalafx.scene.Node
import scalafx.scene.control.{Tab, TabPane}

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

    new TabPane:
      tabs = Seq(graphTab, statsTab)
