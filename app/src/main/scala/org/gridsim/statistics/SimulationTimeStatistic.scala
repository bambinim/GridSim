package org.gridsim.statistics

import org.gridsim.core.observability.SimulationData.SimulationSnapshot

import java.time.LocalDateTime
import scala.concurrent.duration.{DurationInt, FiniteDuration}

final case class SimulationTimeStatistic(
                                          startDateTime: Option[LocalDateTime],
                                          currentDateTime: Option[LocalDateTime],
                                          tick: Long,
                                          elapsed: FiniteDuration
                                        )

object SimulationTimeStatistic:
  val empty: SimulationTimeStatistic = SimulationTimeStatistic(None, None, 0L, 0.seconds)

  val fold: Fold[SimulationSnapshot, SimulationTimeStatistic] =
    Fold.unfold(empty)(
      (acc, snapshot) => SimulationTimeStatistic(
        Some(snapshot.environment.startDateTime),
        Some(snapshot.environment.currentDateTime),
        acc.tick + 1,
        acc.elapsed + snapshot.delta)
    )
