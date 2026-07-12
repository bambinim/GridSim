package org.gridsim.statistics

import cats.syntax.monoid.*
import org.gridsim.core.common.Energy.*
import org.gridsim.core.common.{Energy, kwh}
import org.gridsim.core.model.GridEntityState
import org.gridsim.core.model.house.HouseState
import org.gridsim.core.model.storage.battery.BatteryState
import org.gridsim.core.observability.SimulationData.EntityStatesData
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BatteriesChargeStatisticSpec extends AnyFlatSpec with Matchers:

  // A non-battery, non-house entity: must never contribute to the sample.
  private case class OtherState(entityId: String) extends GridEntityState

  "BatteriesChargeStatistic.empty" should "be the identity element for combine" in :
    val stats = BatteriesChargeStatistic(3L, 10.kwh, 5.kwh)
    (stats |+| BatteriesChargeStatistic.empty) shouldBe stats
    (BatteriesChargeStatistic.empty |+| stats) shouldBe stats

  it should "sum samples/totalCharge and keep the max for maxCharge" in :
    val a = BatteriesChargeStatistic(1L, 4.kwh, 4.kwh)
    val b = BatteriesChargeStatistic(1L, 2.kwh, 2.kwh)

    val combined = a |+| b
    combined.samples shouldBe 2L
    combined.totalCharge.toDouble shouldBe 6.0
    combined.maxCharge.toDouble shouldBe 4.0

  it should "not let a zero-sample side override a populated one" in :
    // Regression guard: combine is hand-rolled (not the generic monoid combine),
    // since maxCharge isn't well defined by simply adding zero-valued fields.
    val populated = BatteriesChargeStatistic(2L, 6.kwh, 4.kwh)
    (populated |+| BatteriesChargeStatistic.empty) shouldBe populated
    (BatteriesChargeStatistic.empty |+| populated) shouldBe populated

  "averageCharge" should "be zero when no samples have been recorded" in :
    BatteriesChargeStatistic.empty.averageCharge.toDouble shouldBe 0.0

  it should "be the total charge divided by the number of samples" in :
    val stats = BatteriesChargeStatistic(4L, 20.kwh, 8.kwh)
    stats.averageCharge.toDouble shouldBe 5.0

  private def statesData(states: GridEntityState*): EntityStatesData =
    EntityStatesData(states.map(s => s.entityId -> s).toMap)

  "BatteriesChargeSampler" should "return empty when there are no battery-bearing entities" in :
    val stats = BatteriesChargeSampler.sample(statesData(OtherState("grid")))
    stats shouldBe BatteriesChargeStatistic.empty

  it should "return empty for a snapshot with no entities at all" in :
    BatteriesChargeSampler.sample(EntityStatesData(Map.empty)) shouldBe BatteriesChargeStatistic.empty

  it should "sample a single standalone battery's charge" in :
    val stats = BatteriesChargeSampler.sample(statesData(BatteryState("b1", 3.0.kwh)))
    stats.samples shouldBe 1L
    stats.totalCharge.toDouble shouldBe 3.0
    stats.maxCharge.toDouble shouldBe 3.0

  it should "sum standalone batteries into totalCharge and track the largest as maxCharge" in :
    val stats = BatteriesChargeSampler.sample(statesData(
      BatteryState("b1", 2.0.kwh),
      BatteryState("b2", 7.0.kwh),
      BatteryState("b3", 1.0.kwh)
    ))
    stats.samples shouldBe 1L
    stats.totalCharge.toDouble shouldBe 10.0
    stats.maxCharge.toDouble shouldBe 7.0

  it should "collapse a house's internal batteries into a single combined charge entry" in :
    // A house with two 3 kWh batteries should contribute one 6 kWh data point,
    // not two separate 3 kWh ones, to maxCharge/totalCharge.
    val house = HouseState("h1", List(BatteryState("b1", 3.0.kwh), BatteryState("b2", 3.0.kwh)))
    val stats = BatteriesChargeSampler.sample(statesData(house))

    stats.samples shouldBe 1L
    stats.totalCharge.toDouble shouldBe 6.0
    stats.maxCharge.toDouble shouldBe 6.0

  it should "let a standalone battery outrank a house's summed charge for maxCharge" in :
    val house = HouseState("h1", List(BatteryState("b1", 1.0.kwh), BatteryState("b2", 1.0.kwh)))
    val stats = BatteriesChargeSampler.sample(statesData(house, BatteryState("standalone", 5.0.kwh)))

    stats.totalCharge.toDouble shouldBe 7.0
    stats.maxCharge.toDouble shouldBe 5.0

  it should "ignore non-battery components inside a house" in :
    val house = HouseState("h1", List(OtherState("panel"), BatteryState("b1", 4.0.kwh)))
    val stats = BatteriesChargeSampler.sample(statesData(house))
    stats.totalCharge.toDouble shouldBe 4.0

  it should "count a battery-less house as a single zero-charge sample, not an absent one" in :
    // componentStates.collect on a house with no batteries returns an empty
    // Iterable, reduceOption yields None, getOrElse(0.kwh) -> 0.0. This still
    // produces one entry in `charges`, so the sampler must NOT report empty.
    val house = HouseState("h1", List(OtherState("panel")))
    val stats = BatteriesChargeSampler.sample(statesData(house))

    stats shouldBe BatteriesChargeStatistic(1L, Energy.Zero, Energy.Zero)
    stats should not be BatteriesChargeStatistic.empty

  it should "mix standalone batteries and houses correctly in the same tick" in :
    val house = HouseState("h1", List(BatteryState("b1", 2.0.kwh), BatteryState("b2", 4.0.kwh)))
    val stats = BatteriesChargeSampler.sample(statesData(
      house,
      BatteryState("standalone", 3.0.kwh),
      OtherState("grid")
    ))

    stats.samples shouldBe 1L
    stats.totalCharge.toDouble shouldBe 9.0 // 6 (house) + 3 (standalone)
    stats.maxCharge.toDouble shouldBe 6.0
