package org.gridsim.gui.ports

import org.gridsim.gui.model.ScenarioRunConfig

trait ScenarioPresetLoader[A]:
  def load(config: ScenarioRunConfig): Either[String, A]
