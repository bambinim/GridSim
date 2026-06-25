package org.gridsim.gui.ports

import org.gridsim.gui.model.ScenarioRunConfig

class MockScenarioPresetLoader extends ScenarioPresetLoader[Unit]:
  def load(config: ScenarioRunConfig): Either[String, Unit] =
    Right(())
    
