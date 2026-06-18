package org.gridsim.core.behaviour.house

import org.gridsim.core.behaviour.shaping.DemandShaper

/**
 * House-specific dependencies used inside a generic
 * [[org.gridsim.core.behaviour.EvolutionContext]].
 *
 * @param resolver converts a consumption strategy and hour into an energy flow
 * @param shaper applies deterministic or stochastic shaping to consumption demand
 */
final case class HouseEvolutionDependencies(
  resolver: ConsumptionResolver,
  shaper: DemandShaper
)

object HouseEvolutionDependencies:
  /**
   * Builds the house dependency group from the smaller givens commonly declared
   * by tests and simulations.
   */
  given fromGivens(using
    resolver: ConsumptionResolver,
    shaper: DemandShaper
  ): HouseEvolutionDependencies =
    HouseEvolutionDependencies(resolver, shaper)
