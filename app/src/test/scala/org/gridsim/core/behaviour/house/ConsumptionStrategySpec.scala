package org.gridsim.core.behaviour.house

import org.gridsim.core.behaviour.house.ConsumptionProfile.*
import org.gridsim.core.common.Units.*
import org.gridsim.core.common.Units.Flow.Deficit
import org.gridsim.core.model.house.Occupancy.*
import org.gridsim.core.model.house.{House, Size}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration.{DurationInt, FiniteDuration}

@RunWith(classOf[JUnitRunner])
class ConsumptionStrategySpec extends AnyFlatSpec with Matchers with TableDrivenPropertyChecks {

  val testHour = 19
  given delta: FiniteDuration = 2.hour

  "ConsumptionStrategy" should "calculate the correct deficit flow for any House configuration" in {
    val testCases = Table(
      ("Size",       "Occupancy",   "Expected Energy"),
      (Size.Small,   Traditional,   16.kwh),
      (Size.Medium,  Traditional,   24.kwh),
      (Size.Large,   Traditional,   32.kwh),
      (Size.Small,   SmartWorker,   10.kwh),
      (Size.Medium,  SmartWorker,   15.kwh),
      (Size.Large,   SmartWorker,   20.kwh),
      (Size.Small,   Vacant,        2.kwh),
      (Size.Medium,  Vacant,        3.kwh),
      (Size.Large,   Vacant,        4.kwh)
    )

    forAll(testCases) { (size, occupancy, expectedEnergy) =>
      val result = calculateConsume(size, occupancy, testHour)
      result shouldBe Deficit(expectedEnergy)
    }
  }

}
