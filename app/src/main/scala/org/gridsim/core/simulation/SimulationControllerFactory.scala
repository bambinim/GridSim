package org.gridsim.core.simulation

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.gridsim.core.behaviour.DefaultEntityEvolutionHandlers.given
import org.gridsim.core.observability.{DataDispatcher, Fs2DataDispatcher, Observer}
import org.gridsim.core.solver.{KirchhoffPowerFlowSolver, SimplePowerFlowSolver}
import org.gridsim.core.simulation.scheduling.DefaultScheduler

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object SimulationControllerFactory:
  /**
   * Constructs and wires a SimulationController with the provided observers.
   *
   * @param model the grid topology and configuration of the simulation
   * @param state the initial conditions of the grid entities and environment
   * @param observers the observers that want to subscribe to simulation events
   * @param tickInterval the interval between ticks
   * @return the wired SimulationController
   */
  def create(
      model: SimulationModel,
      state: SimulationState,
      observers: List[Observer[IO]] = Nil,
      tickInterval: FiniteDuration = 2.seconds
  ): SimulationController =
    val dispatcher = Fs2DataDispatcher[IO](observers).unsafeRunSync()
    val engine = DefaultSimulationEngine(model, KirchhoffPowerFlowSolver(model.grid))
    DefaultSimulationController(
      engine,
      state,
      DefaultScheduler(),
      tickInterval,
      dispatcher = Some(dispatcher)
    )
