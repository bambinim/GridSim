package org.gridsim.core.simulation.scheduling

import java.util.concurrent.{Executors, ScheduledFuture, TimeUnit}
import scala.concurrent.duration.FiniteDuration

/**
 * Standard implementation of [[Scheduler]].
 *
 * This scheduler runs tasks on a single dedicated thread, suitable for
 * real-time simulation tick execution.
 */
case class DefaultScheduler() extends Scheduler:
  private val scheduler = Executors.newSingleThreadScheduledExecutor()

  override def scheduleOnce(task: SimulationTask, delay: FiniteDuration): ScheduledTask =
    val runnable: Runnable = () => task()
    val future =
      scheduler.schedule(
        runnable,
        delay.toNanos,
        TimeUnit.NANOSECONDS
      )
    DefaultScheduledTask(future)

  override def stop(): Unit =
    scheduler.shutdown()

private final case class DefaultScheduledTask(future: ScheduledFuture[?]) extends ScheduledTask:
  override def cancel(): Unit =
    future.cancel(false)
