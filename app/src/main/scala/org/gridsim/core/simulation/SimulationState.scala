package org.gridsim.core.simulation

import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.model.network.Cable
import org.gridsim.core.model.{Environment, GridEntityState}

/**
 * Immutable snapshot of the simulation at a discrete point in time.
 *
 * A simulation step consumes one snapshot and produces a new one, leaving the
 * previous value unchanged. Past snapshots can therefore be stored directly in
 * a simulation history.
 *
 * @param tick number of execution steps completed to produce this snapshot
 * @param environment external conditions and simulated time of this snapshot
 * @param entityStates dynamic state of each simulated entity
 * @param entityFlows net energy flow of each grid entity, indexed by entity ID;
 *                    this also includes the flow exchanged with the external grid
 * @param cableLoads energy transported by each cable during this tick
 */
final case class SimulationState(
  tick: Long,
  environment: Environment,
  entityStates: Iterable[GridEntityState],
  entityFlows: Map[String, Flow[Energy]],
  cableLoads: Map[Cable, Energy],
)
