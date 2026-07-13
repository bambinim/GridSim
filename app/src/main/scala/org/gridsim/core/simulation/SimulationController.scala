package org.gridsim.core.simulation

import scala.concurrent.duration.FiniteDuration

/**
 * Runtime controller for a simulation.
 *
 * A runner owns the effectful lifecycle of the simulation loop: starting,
 * pausing, resuming, stopping and exposing the latest computed snapshot.
 * Domain evolution remains delegated to [[SimulationEngine]], which performs
 * a single pure state transition.
 */
trait SimulationController:

  /**
   * Returns the latest simulation snapshot known by this runner.
   *
   * @return the current immutable simulation state
   */
  def currentState: SimulationState

  /**
   * Returns the current lifecycle state of the runner.
   *
   * @return whether the runner is actively ticking or paused
   */
  def simulationControllerState: SimulationControllerState

  /** Current configuration used by future simulation ticks. */
  def configuration: SimulationConf

  /**
   * Starts the periodic simulation loop.
   *
   * Calling this method while the runner is already running should not create
   * an additional independent loop.
   */
  def start(): Unit

  /** Stops the runner and releases its scheduling resources. */
  def stop(): Unit

  /** Resumes a paused simulation loop. */
  def resume(): Unit

  /** Pauses the periodic simulation loop without changing the current state. */
  def pause(): Unit

  /** Set a new delta duration of the Simulation */
  def setTick(delta: FiniteDuration): Unit

  /**
   * Set the simulation speed
   * @param speed the new time speed of the simulation
   */
  def setSpeed(speed: SimulationSpeed): Unit

  /**
   * Advances the simulation by exactly one tick.
   *
   * @return the new current state produced by the engine
   */
  def stepOnce(): SimulationState
