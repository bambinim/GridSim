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

/** Abstraction for scheduling delayed tasks. */
trait Scheduler:
  /**
   * Schedules a task for one execution after `delay`.
   *
   * @param task the unit of work to execute.
   * @param delay the time to wait before executing the task.
   */
  def scheduleOnce(task: SimulationTask, delay: FiniteDuration): ScheduledTask

  /**
   * Stops the scheduler and releases any underlying resources.
   */
  def stop(): Unit
