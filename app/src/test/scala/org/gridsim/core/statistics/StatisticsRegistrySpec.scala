package org.gridsim.core.statistics

import org.gridsim.core.common.Energy.*
import org.gridsim.core.common.{Energy, Flow, kwh}
import org.gridsim.core.model.Environment
import org.gridsim.core.observability.SimulationData.SimulationSnapshot
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import java.time.LocalDateTime
import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class StatisticsRegistrySpec extends AnyFlatSpec with Matchers:

  private val start = LocalDateTime.of(2026, 1, 1, 12, 0)

  private def snapshotAt(second: Int, flows: Map[String, Flow[Energy]]): SimulationSnapshot =
    SimulationSnapshot(Environment(start, second.seconds), Map.empty, flows, Map.empty, 1.hour)

  private val snapshots = List(
    snapshotAt(0, Map("house" -> Flow.Surplus(5.0.kwh))),
    snapshotAt(1, Map("house" -> Flow.Deficit(2.0.kwh))),
    snapshotAt(2, Map("house" -> Flow.Surplus(9.0.kwh)))
  )

  private def runThroughEngine(snaps: List[SimulationSnapshot]): StatsBoard =
    val finalState = snaps.foldLeft(StatisticsRegistry.engine.initial)(StatisticsRegistry.engine.step)
    StatisticsRegistry.engine.extract(finalState)

  "StatisticsRegistry.engine" should "start both statistics at their own empty/zero state" in:
    val board = StatisticsRegistry.engine.extract(StatisticsRegistry.engine.initial)
    board.get(StatKey.SimStats) shouldBe FlowStatistic.empty
    board.get(StatKey.NetFlowHist).samples shouldBe Vector.empty

  it should "fold FlowStatistic under StatKey.SimStats exactly as FlowSampler would, tick by tick" in:
    val board = runThroughEngine(snapshots)
    val flowStats = board.get(StatKey.SimStats)

    flowStats.ticks shouldBe 3L
    flowStats.totalExported.toDouble shouldBe 14.0
    flowStats.totalImported.toDouble shouldBe 2.0
    flowStats.peakExport.toDouble shouldBe 9.0
    flowStats.peakImport.toDouble shouldBe 2.0

  it should "record one NetFlowSample per snapshot under StatKey.NetFlowHist, preserving simulation time" in:
    val board = runThroughEngine(snapshots)
    val history = board.get(StatKey.NetFlowHist)

    history.samples.map(_.netFlowKwh) shouldBe Vector(5.0, -2.0, 9.0)
    history.samples.map(_.dateTime) shouldBe Vector(start, start.plusSeconds(1), start.plusSeconds(2))

  it should "keep both statistics correct when interleaved on the same stream" in:
    // Regression guard for the "engine built twice" / "observer registered twice"
    // class of bug: each snapshot must be folded exactly once into each statistic.
    val board = runThroughEngine(snapshots)
    board.get(StatKey.SimStats).ticks shouldBe 3L
    board.get(StatKey.NetFlowHist).samples.size shouldBe 3
