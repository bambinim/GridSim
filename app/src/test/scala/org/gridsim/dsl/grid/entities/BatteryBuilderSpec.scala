package org.gridsim.dsl.grid.entities

import org.gridsim.core.common.{Energy, Power, kwh, kw}
import org.gridsim.core.model.storage.battery.{BatteryModel, BatteryState}
import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import org.gridsim.dsl.grid.entities.BatteryBuilder.battery
import org.gridsim.core.model.storage.battery.Battery

@RunWith(classOf[JUnitRunner])
class BatteryBuilderSpec extends AnyFlatSpec with Matchers:
  "A BatteryBuilder" should "be inizialized as" in:
    val b = battery
    b.id shouldBe None
    b.maxCapacity shouldBe None
    b.maxPowerCharge shouldBe None
    b.maxPowerDischarge shouldBe None
    b.minSoC shouldBe None

  it should "set only id when id is set" in:
    val b = battery id "b1"
    b.id shouldBe Some("b1")
    b.maxCapacity shouldBe None
    b.maxPowerCharge shouldBe None
    b.maxPowerDischarge shouldBe None
    b.minSoC shouldBe None

  it should "set only capacity when capacity is set" in:
    val b = battery capacity 100.kwh
    b.id shouldBe None
    b.maxCapacity shouldBe Some(100.kwh)
    b.maxPowerCharge shouldBe None
    b.maxPowerDischarge shouldBe None
    b.minSoC shouldBe None

  it should "set only max charge power when max charge power is set" in:
    val b = battery maxChargingPower (100.kw)
    b.id shouldBe None
    b.maxCapacity shouldBe None
    b.maxPowerCharge shouldBe Some(100.kw)
    b.maxPowerDischarge shouldBe None
    b.minSoC shouldBe None

  it should "set only max discharge power when max discharge power is set" in:
    val b = battery maxDischargingPower (100.kw)
    b.id shouldBe None
    b.maxCapacity shouldBe None
    b.maxPowerCharge shouldBe None
    b.maxPowerDischarge shouldBe Some(100.kw)
    b.minSoC shouldBe None

  it should "set only min soc when min soc is set" in:
    val b = battery minSoC 0.2
    b.id shouldBe None
    b.maxCapacity shouldBe None
    b.maxPowerCharge shouldBe None
    b.maxPowerDischarge shouldBe None
    b.minSoC shouldBe Some(0.2)

  it should "be invalid when maxCapacity is not set" in:
    val b =
      battery id "b1" maxChargingPower 100.kw maxDischargingPower 100.kw minSoC 0.2
    b.build().isValid shouldBe false

  it should "be invalid when maxChargingPower is not set" in:
    val b =
      battery id "b1" capacity 100.kwh maxDischargingPower 100.kw minSoC 0.2
    b.build().isValid shouldBe false

  it should "be invalid when maxDischargingPower is not set" in:
    val b =
      battery id "b1" capacity 100.kwh maxChargingPower 100.kw minSoC 0.2
    b.build().isValid shouldBe false

  it should "be invalid when minSoC is not set" in:
    val b =
      battery id "b1" capacity 100.kwh maxChargingPower 100.kw maxDischargingPower 100.kw
    b.build().isValid shouldBe false

  it should "build a valid battery and state when all fields are set" in:
    val builder =
      battery id "b1" capacity 100.kwh maxChargingPower 100.kw maxDischargingPower 100.kw minSoC 0.2
    val valid = builder.build()
    valid.isValid shouldBe true
    val (b, s) = valid.getOrElse(
      (throw Exception("Invalid battery"), throw Exception("Invalid state"))
    )
    b shouldBe Battery(
      "b1",
      BatteryModel.Standard,
      100.kwh,
      100.kw,
      100.kw,
      0.2
    )
    s shouldBe BatteryState("b1", Energy.Zero)

  it should "use a random UUID when id is not set" in:
    val builder =
      battery capacity 100.kwh maxChargingPower 100.kw maxDischargingPower 100.kw minSoC 0.2
    builder.id shouldBe None
    val valid = builder.build()
    valid.isValid shouldBe true

  it should "build a valid battery with its state" in:
    val b =
      battery id "b1" capacity 100.kwh maxChargingPower 100.kw maxDischargingPower 100.kw minSoC 0.2
    val batt = b
      .build()
      .getOrElse(
        (throw Exception("Invalid battery"), throw Exception("Invalid state"))
      )
    batt._1 shouldBe Battery(
      "b1",
      BatteryModel.Standard,
      100.kwh,
      100.kw,
      100.kw,
      0.2
    )
    batt._2 shouldBe BatteryState("b1", Energy.Zero)

  it should "fail when capacity is not set" in:
    val b =
      battery id "b1" maxChargingPower 100.kw maxDischargingPower 100.kw minSoC 0.2
    b.build().isValid shouldBe false

  it should "fail when maxChargingPower is not set" in:
    val b =
      battery id "b1" capacity 100.kwh maxDischargingPower 100.kw minSoC 0.2
    b.build().isValid shouldBe false

  it should "fail when maxDischargingPower is not set" in:
    val b =
      battery id "b1" capacity 100.kwh maxChargingPower 100.kw minSoC 0.2
    b.build().isValid shouldBe false

  it should "fail when minSoC is not set" in:
    val b =
      battery id "b1" capacity 100.kwh maxChargingPower 100.kw maxDischargingPower 100.kw
    b.build().isValid shouldBe false
