package org.gridsim.core.behaviour.house

import org.gridsim.core.common.*
import org.gridsim.core.behaviour.GridEvolution
import org.gridsim.core.behaviour.StorageEnergyExchanger.*
import org.gridsim.core.model.*
import org.gridsim.core.model.house.{House, HouseState}
import org.gridsim.core.model.storage.{Storage, StorageState}

/**
 * Evolves a house for one simulation tick.
 *
 * The current flow order is:
 * 1. resolve the house consumption;
 * 2. combine it with component production;
 * 3. let storage components handle the resulting residue.
 *
 * Non-storage component evolution is not implemented yet, so production is
 * currently neutral. Future producer components should update `productionFlow`
 * before storage exchange runs.
 */
object HouseEvolution extends GridEvolution[HouseState, House, HouseEvolutionContext]:
  extension (state: HouseState)
    /**
     * Evolves all house component states and returns the residual flow after storage exchange.
     */
    def evolve(h: House, env: Environment)(using context: HouseEvolutionContext): (HouseState, Flow[Energy]) =
      val consumptionFlow = context.resolver.resolve(env.time.hour, h.strategy)(using context.delta, context.shaper)
      val productionFlow = Flow.Balanced
      val residueBeforeStorage = productionFlow + consumptionFlow
      val componentById = h.components.map(c => c.id -> c).toMap

      val (finalResidue, updatedComponentStates) =
        state.componentStates.foldLeft((residueBeforeStorage, List.empty[GridState])) {
          case ((currentFlow, updatedStates), componentState) =>
            (componentState, componentById(componentState.entityId)) match
              case (storageState: StorageState, storage: Storage) =>
                val (nextState, nextResidue) =
                  storageState.exchange(storage, currentFlow, env)(using context.delta)
                (nextResidue, updatedStates :+ nextState)

              case _ =>
                (currentFlow, updatedStates :+ componentState)
        }

      val newHouseState = HouseState(state.entityId, updatedComponentStates)
      (newHouseState, finalResidue)
