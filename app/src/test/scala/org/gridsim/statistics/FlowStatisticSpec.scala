package org.gridsim.statistics

import cats.syntax.monoid.*
import org.gridsim.core.common.Energy.*
import org.gridsim.core.common.{Energy, Flow, kwh}
import org.gridsim.core.common.Energy.toFlow
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
    val stats = FlowStatistic(3L, 10.kwh.toFlow, 4.kwh, 6.kwh, 2.kwh, 5.kwh)
    (stats |+| FlowStatistic.empty) shouldBe stats.copy(current = Flow.balanced)
    (FlowStatistic.empty |+| stats) shouldBe stats

  it should "sum ticks/totals while keeping the max for peaks" in:
    val a = FlowStatistic(1L, 2.kwh.toFlow, 1.kwh, 2.kwh, 1.kwh, 1.kwh)
    val b = FlowStatistic(1L, 1.kwh.toFlow, 5.kwh, 1.kwh, 5.kwh, 1.kwh)

    val combined = a |+| b
    combined.samples shouldBe 2L
    combined.current shouldBe 1.kwh.toFlow
    combined.totalImported.toDouble shouldBe 6.0
    combined.totalExported.toDouble shouldBe 3.0
    combined.peakImport.toDouble shouldBe 5.0
    combined.peakExport.toDouble shouldBe 1.0

  "averageNetFlow" should "be zero when no ticks have been recorded" in:
    FlowStatistic.empty.averageNetFlow.toDouble shouldBe 0.0

  it should "be the mean net flow across all ticks" in:
    val stats = FlowStatistic(4L, 12.kwh.toFlow, 4.kwh, 12.kwh, 0.kwh, 0.kwh)
    stats.averageNetFlow.toDouble shouldBe 2.0 // (12 - 4) / 4

  private val env = Environment(1.minute)

  private def snapshotWithFlows(flows: Map[String, Flow[Energy]]): SimulationSnapshot =
    SimulationSnapshot(env, Map.empty, flows, Map.empty, 1.hour)

  "FlowSampler" should "record a net surplus as exported energy" in :
    val snapshot = snapshotWithFlows(Map("house" -> Flow.Deficit(2.0.kwh), "panel" -> Flow.Surplus(5.0.kwh)))
    val stats = FlowSampler.sample(EntityFlowsData(snapshot.entityFlows))

    stats.samples shouldBe 1L
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
    stats shouldBe FlowStatistic(1L, Flow.balanced, Energy.Zero, Energy.Zero, Energy.Zero, Energy.Zero)
