package org.gridsim.gui.view.layouts

import scalafx.scene.Node
import scalafx.scene.control.SplitPane
import scalafx.geometry.Orientation

/**
 * A layout strategy that arranges the SimulationView components in a vertical SplitPane.
 */
class SplitSimulationLayout extends SimulationLayoutStrategy:

  override def build(graphNode: Node, statsNode: Node): Node =
    new SplitPane:
      styleClass += "sym-split"
      orientation = Orientation.Vertical
      items ++= Seq(graphNode, statsNode)
