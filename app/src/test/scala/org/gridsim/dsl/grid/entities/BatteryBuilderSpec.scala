package org.gridsim.dsl.grid.entities

import org.gridsim.core.common.{Energy, Power}
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
    val b = battery capacity (Energy(100.0))
    b.id shouldBe None
    b.maxCapacity shouldBe Some(Energy(100.0))
    b.maxPowerCharge shouldBe None
    b.maxPowerDischarge shouldBe None
    b.minSoC shouldBe None

  it should "set only max charge power when max charge power is set" in:
    val b = battery maxChargingPower (Power(100.0))
    b.id shouldBe None
    b.maxCapacity shouldBe None
    b.maxPowerCharge shouldBe Some(Power(100.0))
    b.maxPowerDischarge shouldBe None
    b.minSoC shouldBe None

  it should "set only max discharge power when max discharge power is set" in:
    val b = battery maxDischargingPower (Power(100.0))
    b.id shouldBe None
    b.maxCapacity shouldBe None
    b.maxPowerCharge shouldBe None
    b.maxPowerDischarge shouldBe Some(Power(100.0))
    b.minSoC shouldBe None

  it should "set only min soc when min soc is set" in:
    val b = battery minSoC (0.2)
    b.id shouldBe None
    b.maxCapacity shouldBe None
    b.maxPowerCharge shouldBe None
    b.maxPowerDischarge shouldBe None
    b.minSoC shouldBe Some(0.2)

  it should "be invalid when maxCapacity is not set" in:
    val b = battery id "b1" maxChargingPower (Power(
      100.0
    )) maxDischargingPower (Power(100.0)) minSoC (0.2)
    b.build().isValid shouldBe false

  it should "be invalid when maxChargingPower is not set" in:
    val b = battery id "b1" capacity (Energy(100.0)) maxDischargingPower (Power(
      100.0
    )) minSoC (0.2)
    b.build().isValid shouldBe false

  it should "be invalid when maxDischargingPower is not set" in:
    val b = battery id "b1" capacity (Energy(100.0)) maxChargingPower (Power(
      100.0
    )) minSoC (0.2)
    b.build().isValid shouldBe false

  it should "be invalid when minSoC is not set" in:
    val b = battery id "b1" capacity (Energy(100.0)) maxChargingPower (Power(
      100.0
    )) maxDischargingPower (Power(100.0))
    b.build().isValid shouldBe false

  it should "build a valid battery and state when all fields are set" in:
    val builder =
      battery id "b1" capacity (Energy(100.0)) maxChargingPower (Power(
        100.0
      )) maxDischargingPower (Power(100.0)) minSoC (0.2)
    val valid = builder.build()
    valid.isValid shouldBe true
    val (b, s) = valid.getOrElse(
      (throw Exception("Invalid battery"), throw Exception("Invalid state"))
    )
    b shouldBe Battery(
      "b1",
      BatteryModel.Standard,
      Energy(100.0),
      Power(100.0),
      Power(100.0),
      0.2
    )
    s shouldBe BatteryState("b1", Energy.Zero)

  it should "use a random UUID when id is not set" in:
    val builder =
      battery id "b1" capacity (Energy(100.0)) maxChargingPower (Power(
        100.0
      )) maxDischargingPower (Power(100.0)) minSoC (0.2)
    val valid = builder.build()
    valid.isValid shouldBe true
