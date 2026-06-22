package org.gridsim.core.simulation.scheduling

import java.util.concurrent.{Executors, TimeUnit}
import scala.concurrent.duration.FiniteDuration

/**
 * Standard implementation of [[Scheduler]].
 *
 * This scheduler runs tasks on a single dedicated thread, suitable for
 * real-time simulation tick execution.
 */
case class DefaultScheduler() extends Scheduler:
  private val scheduler = Executors.newSingleThreadScheduledExecutor()

  override def schedule(task: SimulationTask, interval: FiniteDuration): Unit =
    scheduler.scheduleAtFixedRate(
      () => task(),
      0L,
      interval.toMillis,
      TimeUnit.MILLISECONDS
    )

  override def stop(): Unit =
    scheduler.shutdown()
