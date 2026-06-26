package org.gridsim.gui.ports

import org.gridsim.dsl.scenarios.GridScenarioCatalog
import org.gridsim.gui.model.ScenarioPresetId

class DslScenarioPresetRepository extends ScenarioPresetRepository:
  def scenarios: Map[ScenarioPresetId, String] =
    GridScenarioCatalog
      .all
      .map(s => ScenarioPresetId(s.id) -> s.name).toMap
