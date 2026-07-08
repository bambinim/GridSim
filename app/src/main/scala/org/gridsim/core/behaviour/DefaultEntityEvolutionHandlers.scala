package org.gridsim.core.behaviour

import org.gridsim.core.behaviour.house.{
  ConsumptionResolver,
  HouseEvolutionDependencies,
  HouseEvolutionHandler
}
import org.gridsim.core.behaviour.producer.SolarPanelEvolutionHandler
import org.gridsim.core.behaviour.shaping.DemandShaper

/**
 * Builds the default entity evolution handler set used by GridSim.
 *
 * This object is application composition: it decides which concrete entity
 * families are available by default, while [[DefaultEntityEvolutionDispatcher]]
 * remains closed to concrete type changes.
 */
object DefaultEntityEvolutionHandlers:

  /**
   * Creates the default ordered handler collection.
   */
  def apply(using
    resolver: ConsumptionResolver,
    shaper: DemandShaper
  ): EntityEvolutionHandlers =
    EntityEvolutionHandlers(
      List(
        HouseEvolutionHandler(HouseEvolutionDependencies(resolver, shaper)),
        SolarPanelEvolutionHandler
      )
    )

  /**
   * Supplies default handlers for code paths that use contextual dispatcher
   * construction.
   */
  given default(using
    resolver: ConsumptionResolver,
    shaper: DemandShaper
  ): EntityEvolutionHandlers =
    apply
