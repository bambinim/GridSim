package org.gridsim.core.behaviour.house

import org.gridsim.core.behaviour.house.ConsumptionResolver.*
import org.gridsim.core.common.Flow.Deficit
import org.gridsim.core.common.Energy.*
import org.gridsim.core.common.*
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

  given delta: FiniteDuration = 2.hour


  "ConsumptionStrategy" should "return a Deficit flow for peak hours" in {
    val strategy: ConsumptionStrategy = DefaultConsumptionStrategy.traditionalProfile
    val testHour = 18
    given resolver: ConsumptionResolver = StochasticConsumptionResolver()

    val result = resolver.resolve(testHour, strategy)

    result should matchPattern { case Flow.Deficit(_) => }
  }

}
