package org.gridsim.core.behaviour.battery

import org.gridsim.core.common.Units.*
import org.gridsim.core.common.Units.Flow.*
import org.gridsim.core.model.battery.{BatterySpecification, BatteryState}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.junit.JUnitRunner
import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class BatteryStrategySpec extends AnyFlatSpec with Matchers with TableDrivenPropertyChecks {

  val spec = BatterySpecification(
    capacity = 10.kwh,
    maxPowerCharge = 5.kw,
    maxPowerDischarge = 5.kw,
    minSoC = 0.2
  )

  given delta: FiniteDuration = 1.hour

  "StandardBatteryStrategy" should "handle charging scenarios correctly" in {
    val chargeScenarios = Table(
      ("Initial Charge", "Offered", "Final Charge", "Residue"),
      (0.kwh,            5.kwh,     5.kwh,        Balanced),
      (0.kwh,            10.kwh,    5.kwh,        Surplus(5.kwh)),
      (9.kwh,            5.kwh,     10.kwh,       Surplus(4.kwh)),
      (10.kwh,           5.kwh,     10.kwh,       Surplus(5.kwh))
    )

    forAll(chargeScenarios) { (initial, offered, expectedFinal, expectedResidue) =>
      val (finalState, residue) = StandardBatteryStrategy.charge(offered, spec).run(BatteryState(initial)).value
      finalState.currentCharge shouldBe expectedFinal
      residue shouldBe expectedResidue
    }
  }

  it should "handle discharging scenarios correctly" in {
    val dischargeScenarios = Table(
      ("Initial Charge", "Needed",  "Final Charge", "Residue"),
      (10.kwh,           5.kwh,     5.kwh,        Balanced),
      (10.kwh,           10.kwh,    5.kwh,        Deficit(5.kwh)),
      (3.kwh,            5.kwh,     2.kwh,        Deficit(4.kwh)),
      (2.kwh,            5.kwh,     2.kwh,        Deficit(5.kwh))
    )

    forAll(dischargeScenarios) { (initial, needed, expectedFinal, expectedResidue) =>
      val (finalState, residue) = StandardBatteryStrategy.discharge(needed, spec).run(BatteryState(initial)).value
      finalState.currentCharge shouldBe expectedFinal
      residue shouldBe expectedResidue
    }
  }

  it should "scale power constraints based on time delta" in {
    given shortDelta: FiniteDuration = 30.minutes

    val (finalState, residue) = StandardBatteryStrategy
      .charge(5.kwh, spec)(using shortDelta)
      .run(BatteryState(0.kwh)).value

    finalState.currentCharge shouldBe 2.5.kwh
    residue shouldBe Surplus(2.5.kwh)
  }
}
