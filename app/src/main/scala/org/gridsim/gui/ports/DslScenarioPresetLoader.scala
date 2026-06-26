package org.gridsim.gui.ports

import cats.data.NonEmptyChain
import org.gridsim.core.simulation.{SimulationModel, SimulationState}
import org.gridsim.dsl.{DSLBuilderError, DSLError}
import org.gridsim.dsl.scenarios.{GridScenarioCatalog, GridScenarioPreset}
import org.gridsim.dsl.simulation.SimulationBuilder
import org.gridsim.gui.model.ScenarioRunConfig

class DslScenarioPresetLoader extends ScenarioPresetLoader[(SimulationModel, SimulationState)]:
  def load(config: ScenarioRunConfig): Either[String, (SimulationModel, SimulationState)] =
    GridScenarioCatalog
      .byId(config.presetId.value)
      .toRight(s"Unknown scenario: ${config.presetId.value}")
      .flatMap { preset =>
        preset
          .build(config.tickDurationMinutes)
          .build()
          .toEither
          .left
          .map(formatErrors)
      }

  private def formatErrors(errors: NonEmptyChain[DSLError]): String =
    errors.toNonEmptyList.toList.mkString(", ")
    
    
