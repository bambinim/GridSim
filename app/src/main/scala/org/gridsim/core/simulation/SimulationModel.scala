package org.gridsim.core.simulation

import org.gridsim.core.model.network.GridGraph

import scala.concurrent.duration.FiniteDuration

/**
 * Immutable configuration of a simulation.
 *
 * The model contains only data that remain constant while the simulation is
 * running. Runtime values such as entity states and cable loads belong to
 * [[SimulationState]].
 *
 * @param grid the static entities and cable topology of the simulated micro-grid
 * @param delta the amount of simulated time represented by one execution step
 */
final case class SimulationModel(grid: GridGraph, delta: FiniteDuration)
