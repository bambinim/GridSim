package org.gridsim.statistics

import org.gridsim.core.model.Environment
import org.gridsim.core.observability.SimulationData.SimulationSnapshot
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import java.time.LocalDateTime
import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class SimulationTimeStatisticSpec extends AnyFlatSpec with Matchers:

  private val start = LocalDateTime.of(2026, 1, 1, 12, 0)

  private def snapshotAt(time: FiniteDuration, delta: FiniteDuration): SimulationSnapshot =
    SimulationSnapshot(Environment(start, time), Map.empty, Map.empty, Map.empty, delta)

  "SimulationTimeStatistic.empty" should "have no calendar times and a zero tick/elapsed" in :
    SimulationTimeStatistic.empty shouldBe SimulationTimeStatistic(None, None, 0L, 0.seconds)

  "SimulationTimeStatistic.fold" should "start at the empty value before any snapshot is folded" in :
    SimulationTimeStatistic.fold.initial shouldBe SimulationTimeStatistic.empty

  it should "populate both calendar times and count the first tick after one snapshot" in :
    val snapshot = snapshotAt(0.seconds, delta = 1.hour)
    val state = SimulationTimeStatistic.fold.step(SimulationTimeStatistic.fold.initial, snapshot)
    val result = SimulationTimeStatistic.fold.extract(state)

    result.tick shouldBe 1L
    result.elapsed shouldBe 1.hour
    result.startDateTime shouldBe Some(start)
    result.currentDateTime shouldBe Some(start)

  it should "increment tick and accumulate elapsed across multiple snapshots" in :
    val snapshots = List(
      snapshotAt(0.seconds, delta = 1.hour),
      snapshotAt(1.hour, delta = 1.hour),
      snapshotAt(2.hours, delta = 30.minutes)
    )
    val finalState = snapshots.foldLeft(SimulationTimeStatistic.fold.initial)(SimulationTimeStatistic.fold.step)
    val result = SimulationTimeStatistic.fold.extract(finalState)

    result.tick shouldBe 3L
    result.elapsed shouldBe 2.hours + 30.minutes

  it should "track currentDateTime from the latest snapshot's environment, not by adding elapsed itself" in :
    val snapshots = List(
      snapshotAt(0.seconds, delta = 1.hour),
      snapshotAt(3.hours, delta = 1.hour) // environment jumps ahead independently of the fold's own elapsed sum
    )
    val finalState = snapshots.foldLeft(SimulationTimeStatistic.fold.initial)(SimulationTimeStatistic.fold.step)
    val result = SimulationTimeStatistic.fold.extract(finalState)

    result.currentDateTime shouldBe Some(start.plusHours(3))
    result.elapsed shouldBe 2.hours // sum of the deltas folded in, independent of the jump above

  it should "keep startDateTime constant across ticks when the environment's start doesn't change" in :
    val snapshots = List(
      snapshotAt(0.seconds, delta = 1.hour),
      snapshotAt(1.hour, delta = 1.hour),
      snapshotAt(2.hours, delta = 1.hour)
    )
    val finalState = snapshots.foldLeft(SimulationTimeStatistic.fold.initial)(SimulationTimeStatistic.fold.step)
    SimulationTimeStatistic.fold.extract(finalState).startDateTime shouldBe Some(start)

  it should "reflect a changed calendar start if a later snapshot's environment reports one" in :
    val otherStart = LocalDateTime.of(2026, 6, 1, 0, 0)
    val snapshots = List[SimulationSnapshot](
      snapshotAt(0.seconds, delta = 1.hour),
      SimulationSnapshot(Environment(otherStart, 0.seconds), Map.empty, Map.empty, Map.empty, 1.hour)
    )
    val finalState = snapshots.foldLeft(SimulationTimeStatistic.fold.initial)(SimulationTimeStatistic.fold.step)
    SimulationTimeStatistic.fold.extract(finalState).startDateTime shouldBe Some(otherStart)
