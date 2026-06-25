package org.gridsim.gui.ports

import org.gridsim.gui.model.ScenarioPresetId

class InMemoryScenarioPresetRepository extends ScenarioPresetRepository:
  def scenarios: Map[ScenarioPresetId, String] = Map(
    ScenarioPresetId("base-neighborhood") -> "Quartiere base",
    ScenarioPresetId("advanced-neighborhood") -> "Quartiere avanzato"
  )
