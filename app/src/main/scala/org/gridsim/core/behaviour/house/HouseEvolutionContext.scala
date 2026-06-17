package org.gridsim.core.behaviour.house

import org.gridsim.core.behaviour.shaping.DemandShaper

import scala.concurrent.duration.FiniteDuration

/**
 * Dependencies required to evolve a house for one simulation tick.
 *
 * Grouping these values keeps [[org.gridsim.core.behaviour.GridEvolution]]
 * generic while still allowing house evolution to depend on consumption-specific
 * behavior.
 *
 * @param delta the duration represented by one simulation tick
 * @param resolver converts a consumption strategy and hour into an energy flow
 * @param shaper applies deterministic or stochastic shaping to consumption demand
 */
final case class HouseEvolutionContext(
  delta: FiniteDuration,
  resolver: ConsumptionResolver,
  shaper: DemandShaper
)

object HouseEvolutionContext:
  /**
   * Builds a context automatically from the smaller givens commonly declared by tests and simulations.
   */
  given fromGivens(using
    delta: FiniteDuration,
    resolver: ConsumptionResolver,
    shaper: DemandShaper
  ): HouseEvolutionContext =
    HouseEvolutionContext(delta, resolver, shaper)
