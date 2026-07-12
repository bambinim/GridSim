package org.gridsim.statistics

import cats.syntax.monoid.*
import org.gridsim.core.common.{Energy, kw, kwh}
import org.gridsim.core.model.Environment
import org.gridsim.core.model.network.{Cable, CableConnections}
import org.gridsim.core.observability.SimulationData.SimulationSnapshot
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration.FiniteDuration

@RunWith(classOf[JUnitRunner])
class CablesOverloadStatisticSpec extends AnyFlatSpec with Matchers:

  "CablesOverloadStatistic.empty" should "be the identity element for combine" in :
    val stats = CablesOverloadStatistic(3L, 2L)
    (stats |+| CablesOverloadStatistic.empty) shouldBe stats
    (CablesOverloadStatistic.empty |+| stats) shouldBe stats

  it should "sum both samples and overloadedCableSamples on combine" in :
    val a = CablesOverloadStatistic(2L, 1L)
    val b = CablesOverloadStatistic(3L, 2L)
    (a |+| b) shouldBe CablesOverloadStatistic(5L, 3L)

  private val env = Environment(1.minute)

  private def mkCable(name: String, maxCapacityKw: Double) =
    Cable(CableConnections(name, "hub"), maxCapacityKw.kw)

  private def snapshot(loads: Map[Cable, Energy], delta: FiniteDuration = 1.hour): SimulationSnapshot =
    SimulationSnapshot(env, Map.empty, Map.empty, loads, delta)

  "CablesOverloadSampler" should "count zero overloaded cables when the snapshot has none" in :
    val stats = CablesOverloadSampler.sample(snapshot(Map.empty))
    stats shouldBe CablesOverloadStatistic(1L, 0L)

  it should "not count a cable running under its capacity" in :
    val cable = mkCable("c1", 10.0)
    val stats = CablesOverloadSampler.sample(snapshot(Map(cable -> 5.0.kwh), delta = 1.hour))
    stats shouldBe CablesOverloadStatistic(1L, 0L)

  it should "not count a cable running exactly at its capacity (strictly greater-than check)" in :
    val cable = mkCable("c1", 10.0)
    val stats = CablesOverloadSampler.sample(snapshot(Map(cable -> 10.0.kwh), delta = 1.hour))
    stats shouldBe CablesOverloadStatistic(1L, 0L)

  it should "count a cable running over its capacity" in :
    val cable = mkCable("c1", 10.0)
    val stats = CablesOverloadSampler.sample(snapshot(Map(cable -> 10.1.kwh), delta = 1.hour))
    stats shouldBe CablesOverloadStatistic(1L, 1L)

  it should "count each overloaded cable independently, ignoring healthy ones" in :
    val healthy = mkCable("c1", 10.0)
    val overloadedA = mkCable("c2", 5.0)
    val overloadedB = mkCable("c3", 2.0)
    val stats = CablesOverloadSampler.sample(snapshot(Map(
      healthy -> 4.0.kwh,
      overloadedA -> 6.0.kwh,
      overloadedB -> 9.0.kwh
    ), delta = 1.hour))

    stats shouldBe CablesOverloadStatistic(1L, 2L)

  it should "convert energy to instant power using the snapshot's own delta, not a fixed hour" in :
    // Same 10 kWh load: over a full hour that's 10 kW (fine on a 10 kW cable),
    // but over a 30-minute tick that's 20 kW (overloaded on the same cable).
    val cable = mkCable("c1", 10.0)
    val overHour = CablesOverloadSampler.sample(snapshot(Map(cable -> 10.0.kwh), delta = 1.hour))
    val overHalfHour = CablesOverloadSampler.sample(snapshot(Map(cable -> 10.0.kwh), delta = 30.minutes))

    overHour.overloadedCableSamples shouldBe 0L
    overHalfHour.overloadedCableSamples shouldBe 1L

  it should "treat every sample call as exactly one tick regardless of cable count" in :
    val cables = Map[Cable, Energy](
      mkCable("c1", 10.0) -> 1.0.kwh,
      mkCable("c2", 10.0) -> 1.0.kwh,
      mkCable("c3", 10.0) -> 1.0.kwh
    )
    CablesOverloadSampler.sample(snapshot(cables)).samples shouldBe 1L
