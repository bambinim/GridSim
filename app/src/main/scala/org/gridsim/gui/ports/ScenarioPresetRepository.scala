package org.gridsim.gui.ports

import org.gridsim.gui.model.ScenarioPresetId

/** Port interface for retrieving registered scenario presets. */
trait ScenarioPresetRepository:
  /**
   * Returns a map of all available scenario presets.
   *
   * @return map of [[ScenarioPresetId]] to name
   */
  def scenarios: Map[ScenarioPresetId, String]
