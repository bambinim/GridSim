package org.gridsim.statistics

import cats.syntax.monoid.*
import org.gridsim.core.common.Energy.*
import org.gridsim.core.common.{Energy, Flow, kwh}
import org.gridsim.core.model.Environment
import org.gridsim.core.observability.SimulationData.{EntityFlowsData, SimulationSnapshot}
import org.gridsim.statistics.{FlowSampler, FlowStatistic}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FlowStatisticSpec extends AnyFlatSpec with Matchers:

  "SimulationStatistics.empty" should "be the identity element for combine" in:
    val stats = FlowStatistic(3L, 10.0.kwh, 4.0.kwh, 6.0.kwh, 2.0.kwh)
    (stats |+| FlowStatistic.empty) shouldBe stats
    (FlowStatistic.empty |+| stats) shouldBe stats

  it should "sum ticks/totals while keeping the max for peaks" in:
    val a = FlowStatistic(1L, 2.0.kwh, 1.0.kwh, 2.0.kwh, 1.0.kwh)
    val b = FlowStatistic(1L, 1.0.kwh, 5.0.kwh, 1.0.kwh, 5.0.kwh)

    val combined = a |+| b
    combined.ticks shouldBe 2L
    combined.totalImported.toDouble shouldBe 3.0
    combined.totalExported.toDouble shouldBe 6.0
    combined.peakImport.toDouble shouldBe 2.0
    combined.peakExport.toDouble shouldBe 5.0

  "averageNetFlow" should "be zero when no ticks have been recorded" in:
    FlowStatistic.empty.averageNetFlow shouldBe 0.0

  it should "be the mean net flow across all ticks" in:
    val stats = FlowStatistic(4L, 4.0.kwh, 12.0.kwh, 0.0.kwh, 0.0.kwh)
    stats.averageNetFlow shouldBe 2.0 // (12 - 4) / 4

  private val env = Environment(1.minute)

  private def snapshotWithFlows(flows: Map[String, Flow[Energy]]): SimulationSnapshot =
    SimulationSnapshot(env, Map.empty, flows, Map.empty)

  "FlowSampler" should "record a net surplus as exported energy" in :
    val snapshot = snapshotWithFlows(Map("house" -> Flow.Deficit(2.0.kwh), "panel" -> Flow.Surplus(5.0.kwh)))
    val stats = FlowSampler.sample(EntityFlowsData(snapshot.entityFlows))

    stats.ticks shouldBe 1L
    stats.totalExported.toDouble shouldBe 3.0
    stats.totalImported.toDouble shouldBe 0.0

  it should "record a net deficit as imported energy" in :
    val snapshot = snapshotWithFlows(Map("house" -> Flow.Deficit(8.0.kwh), "panel" -> Flow.Surplus(3.0.kwh)))
    val stats = FlowSampler.sample(EntityFlowsData(snapshot.entityFlows))

    stats.totalImported.toDouble shouldBe 5.0
    stats.totalExported.toDouble shouldBe 0.0

  it should "record a perfectly balanced tick as zero on both sides" in :
    val snapshot = snapshotWithFlows(Map("house" -> Flow.Deficit(4.0.kwh), "panel" -> Flow.Surplus(4.0.kwh)))
    val stats = FlowSampler.sample(EntityFlowsData(snapshot.entityFlows))

    stats.totalImported.toDouble shouldBe 0.0
    stats.totalExported.toDouble shouldBe 0.0

  it should "handle an empty snapshot as a zero-valued single tick" in :
    val stats = FlowSampler.sample(EntityFlowsData(snapshotWithFlows(Map.empty).entityFlows))
    stats shouldBe FlowStatistic(1L, Energy.Zero, Energy.Zero, Energy.Zero, Energy.Zero)
