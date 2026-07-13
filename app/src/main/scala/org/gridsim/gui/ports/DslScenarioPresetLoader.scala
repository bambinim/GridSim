package org.gridsim.gui.ports

import cats.data.NonEmptyChain
import org.gridsim.core.model.Environment
import org.gridsim.dsl.DSLError
import org.gridsim.dsl.scenarios.GridScenarioCatalog
import org.gridsim.gui.model.{RunningSimulation, ScenarioRunConfig}
import org.gridsim.gui.runtime.RunningSimulationFactory

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
          .build(config.tickDelta)
          .build()
          .toEither
          .left
          .map(formatErrors)
          .map { case (model, state) =>
            val seededState = state.copy(environment = Environment(config.startDateTime))
            RunningSimulationFactory.createSimpleSimulation(
              preset.name,
              model,
              seededState,
              config.tickDelta
            )
          }
      }

  private def formatErrors(errors: NonEmptyChain[DSLError]): String =
    import cats.syntax.show.*
    import org.gridsim.dsl.given
    errors.toNonEmptyList.toList.map(_.show).mkString(", ")
