package org.gridsim.gui.view

import scalafx.scene.Parent

/**
 * Interface/trait that must be implemented by any JavaFX View component in the application.
 * Exposes a common `root` parent node for scene layout integration.
 */
trait ViewFX:
  /**
   * The root parent component of the view.
   *
   * @return the ScalaFX/JavaFX Parent node
   */
  def root: Parent
