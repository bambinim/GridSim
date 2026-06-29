package org.gridsim.gui.ports

import cats.data.NonEmptyChain
import org.gridsim.dsl.{DSLBuilderError, DSLError}
import org.gridsim.dsl.scenarios.{GridScenarioCatalog, GridScenarioPreset}
import org.gridsim.dsl.simulation.SimulationBuilder
import org.gridsim.gui.model.{RunningSimulation, ScenarioRunConfig}
import org.gridsim.gui.runtime.SimulationFactory

class DslScenarioPresetLoader extends ScenarioPresetLoader[RunningSimulation]:
  def load(config: ScenarioRunConfig): Either[String, RunningSimulation] =
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
          .map { case (model, state) =>
            SimulationFactory.createSimpleSimulation(model, state)
          }
      }

  private def formatErrors(errors: NonEmptyChain[DSLError]): String =
    errors.toNonEmptyList.toList.mkString(", ")
    
    
