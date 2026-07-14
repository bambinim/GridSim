package org.gridsim.gui.view.layouts

import scalafx.scene.Node

/**
 * Strategy interface for arranging the SimulationView components.
 * 
 * Implementations of this trait define a specific UI layout arrangement 
 * (e.g., using Tabs, SplitPanes) for the core simulation visualization areas.
 */
trait SimulationLayoutStrategy:
  
  /**
   * Constructs a JavaFX/ScalaFX Node containing the arranged subviews.
   *
   * @param graphNode the Node representing the GridGraph visualization
   * @param statsNode the Node representing the Statistics visualization
   * @return a single layout Node containing both subviews
   */
  def build(graphNode: Node, statsNode: Node): Node
