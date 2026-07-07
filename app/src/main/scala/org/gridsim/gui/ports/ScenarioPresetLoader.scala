package org.gridsim.gui.ports

import org.gridsim.gui.model.ScenarioRunConfig

/**
 * Port interface for loading scenario presets.
 *
 * @tparam A the output type containing the constructed simulation context
 */
trait ScenarioPresetLoader[A]:
  /**
   * Loads and builds a simulation preset.
   *
   * @param config the configuration mapping preset parameters and tick length
   * @return Either a string detailing validation/DSL errors or the loaded simulation context
   */
  def load(config: ScenarioRunConfig): Either[String, A]
