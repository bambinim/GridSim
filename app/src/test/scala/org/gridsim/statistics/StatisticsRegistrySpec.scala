package org.gridsim.statistics

import org.gridsim.core.common.Energy.*
import org.gridsim.core.common.{Energy, Flow, kw, kwh}
import org.gridsim.core.model.Environment
import org.gridsim.core.model.GridEntityState
import org.gridsim.core.model.network.{Cable, CableConnections}
import org.gridsim.core.model.storage.battery.BatteryState
import org.gridsim.core.observability.SimulationData.SimulationSnapshot
import org.gridsim.statistics.{FlowStatistic, StatKey, StatisticsRegistry, StatsBoard}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import java.time.LocalDateTime
import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class StatisticsRegistrySpec extends AnyFlatSpec with Matchers:

  private val start = LocalDateTime.of(2026, 1, 1, 12, 0)
  private val overloadedCable = Cable(CableConnections("grid", "house"), 10.0.kw)

  private def snapshotAt(
                          second: Int,
                          flows: Map[String, Flow[Energy]],
                          states: Map[String, GridEntityState] = Map.empty,
                          cableLoads: Map[Cable, Energy] = Map.empty
                        ): SimulationSnapshot =
    SimulationSnapshot(Environment(start, second.seconds), states, flows, cableLoads, 1.hour)

  private val snapshots = List(
    snapshotAt(0, Map("house" -> Flow.Surplus(5.0.kwh)),
      states = Map("b1" -> BatteryState("b1", 3.0.kwh)),
      cableLoads = Map(overloadedCable -> 5.0.kwh)),
    snapshotAt(1, Map("house" -> Flow.Deficit(2.0.kwh)),
      states = Map("b1" -> BatteryState("b1", 6.0.kwh)),
      cableLoads = Map(overloadedCable -> 12.0.kwh)),
    snapshotAt(2, Map("house" -> Flow.Surplus(9.0.kwh)),
      states = Map("b1" -> BatteryState("b1", 4.0.kwh)),
      cableLoads = Map(overloadedCable -> 3.0.kwh))
  )

  private def runThroughEngine(snaps: List[SimulationSnapshot]): StatsBoard =
    val finalState = snaps.foldLeft(StatisticsRegistry.engine.initial)(StatisticsRegistry.engine.step)
    StatisticsRegistry.engine.extract(finalState)

  "StatisticsRegistry.engine" should "start every registered statistic at its own empty/zero state" in:
    val board = StatisticsRegistry.engine.extract(StatisticsRegistry.engine.initial)
    board.get(StatKey.FlowStatKey) shouldBe FlowStatistic.empty
    board.get(StatKey.NetFlowHistoryStatKey).samples shouldBe Vector.empty
    board.get(StatKey.BatteryChargeStatKey) shouldBe BatteriesChargeStatistic.empty
    board.get(StatKey.CableOverloadStatKey) shouldBe CablesOverloadStatistic.empty
    board.get(StatKey.SimTimeStatKey) shouldBe SimulationTimeStatistic.empty

  it should "fold FlowStatistic under StatKey.SimStats exactly as FlowSampler would, tick by tick" in:
    val board = runThroughEngine(snapshots)
    val flowStats = board.get(StatKey.FlowStatKey)

    flowStats.samples shouldBe 3L
    flowStats.totalExported.toDouble shouldBe 14.0
    flowStats.totalImported.toDouble shouldBe 2.0
    flowStats.peakExport.toDouble shouldBe 9.0
    flowStats.peakImport.toDouble shouldBe 2.0

  it should "record one NetFlowSample per snapshot under StatKey.NetFlowHist, preserving simulation time" in:
    val board = runThroughEngine(snapshots)
    val history = board.get(StatKey.NetFlowHistoryStatKey)

    history.samples.map(_.netFlowKwh) shouldBe Vector(5.0, -2.0, 9.0)
    history.samples.map(_.dateTime) shouldBe Vector(start, start.plusSeconds(1), start.plusSeconds(2))

  it should "fold BatteriesChargeStatistic under StatKey.BatteryChargeStatKey, tick by tick" in:
    val board = runThroughEngine(snapshots)
    val batteryStats = board.get(StatKey.BatteryChargeStatKey)

    batteryStats.samples shouldBe 3L
    batteryStats.totalCharge.toDouble shouldBe 13.0 // 3 + 6 + 4
    batteryStats.maxCharge.toDouble shouldBe 6.0

  it should "fold CablesOverloadStatistic under StatKey.CableOverloadStatKey, tick by tick" in:
    val board = runThroughEngine(snapshots)
    val cableStats = board.get(StatKey.CableOverloadStatKey)

    // Only the second tick's 12 kWh over a 1h delta (12 kW) exceeds the 10 kW cable.
    cableStats.samples shouldBe 3L
    cableStats.overloadedCableSamples shouldBe 1L

  it should "fold SimulationTimeStatistic under StatKey.SimTimeStatKey, tracking ticks and calendar time" in:
    val board = runThroughEngine(snapshots)
    val timeStats = board.get(StatKey.SimTimeStatKey)

    timeStats.tick shouldBe 3L
    timeStats.startDateTime shouldBe Some(start)
    timeStats.currentDateTime shouldBe Some(start.plusSeconds(2))
    timeStats.elapsed shouldBe 3.hours // sum of each snapshot's own 1-hour delta

  it should "keep every statistic correct when interleaved on the same stream" in:
    // Regression guard for the "engine built twice" / "observer registered twice"
    // class of bug: each snapshot must be folded exactly once into each statistic.
    val board = runThroughEngine(snapshots)
    board.get(StatKey.FlowStatKey).samples shouldBe 3L
    board.get(StatKey.NetFlowHistoryStatKey).samples.size shouldBe 3
    board.get(StatKey.BatteryChargeStatKey).samples shouldBe 3L
    board.get(StatKey.CableOverloadStatKey).samples shouldBe 3L
    board.get(StatKey.SimTimeStatKey).tick shouldBe 3L
