package org.gridsim.gui.ports

import org.gridsim.gui.model.ScenarioPresetId

trait ScenarioPresetRepository:
  def scenarios: Map[ScenarioPresetId, String]
