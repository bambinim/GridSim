package org.gridsim.core.common

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

import org.gridsim.core.common.*
import org.gridsim.core.common.Flow.{surplus, deficit, balanced}

@RunWith(classOf[JUnitRunner])
class FlowSpec extends AnyFlatSpec with Matchers {

  "Flow" should "correctly calculate numeric values for Energy flows" in {
    surplus(10.0.kwh).value shouldBe 10.0
    deficit(5.0.kwh).value shouldBe -5.0
    balanced.value shouldBe 0.0
  }

  it should "support combining flows" in {
    (surplus(10.0.kwh) + deficit(5.0.kwh)) shouldBe surplus(5.0.kwh)
    (surplus(5.0.kwh) + deficit(10.0.kwh)) shouldBe deficit(5.0.kwh)
    (surplus(5.0.kwh) + balanced) shouldBe surplus(5.0.kwh)
  }
}
