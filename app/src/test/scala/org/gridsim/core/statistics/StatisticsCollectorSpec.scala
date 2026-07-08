package org.gridsim.core.statistics

import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.common.Energy.*
import org.gridsim.core.common.kwh
import org.gridsim.core.model.Environment
import org.gridsim.core.observability.SimulationData.SimulationSnapshot
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class StatisticsCollectorSpec extends AnyFlatSpec with Matchers:

  private val env = Environment(1.minute)

  private def snapshotWithFlows(flows: Map[String, Flow[Energy]]): SimulationSnapshot =
    SimulationSnapshot(env, Map.empty, flows, Map.empty)

  "StatisticsCollector" should "record a net surplus as exported energy" in:
    val snapshot = snapshotWithFlows(Map("house" -> Flow.Deficit(2.0.kwh), "panel" -> Flow.Surplus(5.0.kwh)))
    val stats = StatisticsCollector.collect(snapshot)

    stats.ticks shouldBe 1L
    stats.totalExported.toDouble shouldBe 3.0
    stats.totalImported.toDouble shouldBe 0.0

  it should "record a net deficit as imported energy" in:
    val snapshot = snapshotWithFlows(Map("house" -> Flow.Deficit(8.0.kwh), "panel" -> Flow.Surplus(3.0.kwh)))
    val stats = StatisticsCollector.collect(snapshot)

    stats.totalImported.toDouble shouldBe 5.0
    stats.totalExported.toDouble shouldBe 0.0

  it should "record a perfectly balanced tick as zero on both sides" in:
    val snapshot = snapshotWithFlows(Map("house" -> Flow.Deficit(4.0.kwh), "panel" -> Flow.Surplus(4.0.kwh)))
    val stats = StatisticsCollector.collect(snapshot)

    stats.totalImported.toDouble shouldBe 0.0
    stats.totalExported.toDouble shouldBe 0.0

  it should "handle an empty snapshot as a zero-valued single tick" in:
    val stats = StatisticsCollector.collect(snapshotWithFlows(Map.empty))
    stats shouldBe SimulationStatistics(1L, Energy.Zero, Energy.Zero, Energy.Zero, Energy.Zero)
