package org.gridsim.core.simulation.scheduling

import scala.concurrent.duration.FiniteDuration

/**
 * Type alias representing a discrete unit of work in the simulation.
 */
type SimulationTask = () => Unit

/**
 * Handle returned by a scheduler for a submitted task.
 */
trait ScheduledTask:
  /**
   * Cancels future executions of the scheduled task.
   */
  def cancel(): Unit

/**
 * Abstraction for scheduling periodic tasks.
 *
 * Implementations are responsible for managing the execution lifecycle
 * and timing of simulation ticks.
 */
trait Scheduler:
  /**
   * Schedules a task to be executed periodically.
   *
   * @param task the unit of work to execute.
   * @param interval the time between task executions.
   */
  def schedule(task: SimulationTask, interval: FiniteDuration): ScheduledTask

  /**
   * Stops the scheduler and releases any underlying resources.
   */
  def stop(): Unit
