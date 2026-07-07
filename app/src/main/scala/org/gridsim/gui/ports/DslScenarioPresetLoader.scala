package org.gridsim.gui.ports

import cats.data.NonEmptyChain
import org.gridsim.dsl.{DSLBuilderError, DSLError}
import org.gridsim.dsl.scenarios.{GridScenarioCatalog, GridScenarioPreset}
import org.gridsim.dsl.simulation.SimulationBuilder
import org.gridsim.gui.model.{RunningSimulation, ScenarioRunConfig}
import org.gridsim.gui.runtime.SimulationFactory

/**
 * Implementation of [[ScenarioPresetLoader]] that builds a running simulation using the DSL scenario catalog.
 */
class DslScenarioPresetLoader extends ScenarioPresetLoader[RunningSimulation]:
  /**
   * Loads a simulation preset by ID and constructs a [[RunningSimulation]] instance.
   *
   * @param config configuration details including the preset ID and tick duration
   * @return Either a string detailing validation/DSL errors or the loaded RunningSimulation
   */
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
    import cats.syntax.show.*
    import org.gridsim.dsl.given
    errors.toNonEmptyList.toList.map(_.show).mkString(", ")
    
    
