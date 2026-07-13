package org.gridsim.gui.ports

import org.gridsim.dsl.scenarios.GridScenarioCatalog
import org.gridsim.gui.model.ScenarioPresetId

/** Implementation of [[ScenarioPresetRepository]] retrieving presets registered in the DSL scenario catalog. */
class DslScenarioPresetRepository extends ScenarioPresetRepository:
  /**
   * Retrieves all available scenario presets.
   *
   * @return a map of [[ScenarioPresetId]] to scenario preset name
   */
  def scenarios: Map[ScenarioPresetId, String] =
    GridScenarioCatalog
      .all
      .map(s => ScenarioPresetId(s.id) -> s.name).toMap
