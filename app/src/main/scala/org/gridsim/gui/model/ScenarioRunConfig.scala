package org.gridsim.gui.model

import scala.concurrent.duration.FiniteDuration

case class ScenarioPresetId(value: String)

case class ScenarioRunConfig(
    presetId: ScenarioPresetId,
    tickDurationMinutes: FiniteDuration
)
