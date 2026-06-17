package org.gridsim.core.behaviour.battery

import org.gridsim.core.common.Flow.*
import org.gridsim.core.common.Energy.*
import org.gridsim.core.common.*
import org.gridsim.core.model.battery.{Battery, BatteryState}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.junit.JUnitRunner
import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class BatteryStrategySpec extends AnyFlatSpec with Matchers with TableDrivenPropertyChecks {

  private val strategy: BatteryStrategy = StandardBatteryStrategy
  import strategy.*
  
  given delta: FiniteDuration = 1.hour

  private val battery = Battery(
    id = "B1",
    maxCapacity = 10.kwh,
    maxPowerCharge = 5.kw,
    maxPowerDischarge = 5.kw,
    minSoC = 0.2
  )

  "BatteryState" should "handle charging scenarios correctly via extension method" in {
    val chargeScenarios = Table[Energy, Energy, Energy, Flow[Energy]](
      ("Initial Charge", "Offered", "Final Charge", "Residue"),
      (0.kwh,            5.kwh,     5.kwh,        Balanced),
      (0.kwh,            10.kwh,    5.kwh,        Surplus(5.kwh)),
      (9.kwh,            5.kwh,     10.kwh,       Surplus(4.kwh)),
      (10.kwh,           5.kwh,     10.kwh,       Surplus(5.kwh))
    )

    forAll(chargeScenarios) { (initial: Energy, offered: Energy, expectedFinal: Energy, expectedResidue: Flow[Energy]) =>
      val (finalState, residue) = BatteryState("B1", initial).charge(offered, battery)

      finalState.currentCharge.toDouble shouldBe expectedFinal.toDouble
      residue shouldBe expectedResidue
    }
  }

  it should "handle discharging scenarios correctly via extension method" in {
    val dischargeScenarios = Table[Energy, Energy, Energy, Flow[Energy]](
      ("Initial Charge", "Needed",  "Final Charge", "Residue"),
      (10.kwh,           5.kwh,     5.kwh,        Balanced),
      (10.kwh,           10.kwh,    5.kwh,        Deficit(5.kwh)),
      (3.kwh,            5.kwh,     2.kwh,        Deficit(4.kwh)),
      (2.kwh,            5.kwh,     2.kwh,        Deficit(5.kwh))
    )

    forAll(dischargeScenarios) { (initial: Energy, needed: Energy, expectedFinal: Energy, expectedResidue: Flow[Energy]) =>
      val (finalState, residue) = BatteryState("B1", initial).discharge(needed, battery)

      finalState.currentCharge.toDouble shouldBe expectedFinal.toDouble
      residue shouldBe expectedResidue
    }
  }

  it should "scale power constraints based on time delta" in {
    given shortDelta: FiniteDuration = 30.minutes

    val (finalState, residue) = BatteryState("B1", 0.kwh).charge(5.kwh, battery)(using shortDelta)

    finalState.currentCharge.toDouble shouldBe 2.5
    residue shouldBe Surplus(2.5.kwh)
  }
}
