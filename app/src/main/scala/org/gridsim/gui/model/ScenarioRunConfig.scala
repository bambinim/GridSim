package org.gridsim.gui.model

import java.time.LocalDateTime
import scala.concurrent.duration.FiniteDuration

/** Value class wrapper representing the unique identifier of a scenario preset. */
case class ScenarioPresetId(value: String)

/**
 * Configuration payload representing the parameters chosen to run a simulation scenario.
 *
 * @param presetId the ID of the scenario preset
 * @param tickDelta the real-world duration that each simulation tick represents
 */
case class ScenarioRunConfig(
    presetId: ScenarioPresetId,
    tickDelta: FiniteDuration,
    startDateTime: LocalDateTime
)
