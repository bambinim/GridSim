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

  override def schedule(task: SimulationTask, interval: FiniteDuration): ScheduledTask =
    val future =
      scheduler.scheduleAtFixedRate(
        () => task(),
        0L,
        interval.toMillis,
        TimeUnit.MILLISECONDS
      )
    DefaultScheduledTask(future)

  override def stop(): Unit =
    scheduler.shutdown()

private final case class DefaultScheduledTask(future: ScheduledFuture[?]) extends ScheduledTask:
  override def cancel(): Unit =
    future.cancel(false)
