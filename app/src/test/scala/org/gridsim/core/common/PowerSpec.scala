package org.gridsim.core.common

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.Show
import cats.syntax.all.*
import org.gridsim.core.common.*
import org.gridsim.core.common.Power
import org.gridsim.core.common.Power.{given, *}
import scala.concurrent.duration.*
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PowerSpec extends AnyFlatSpec with Matchers {
  "Power" should "be correctly created and converted to Double" in {
    val e = 10.0.kw
    e.toDouble shouldBe 10.0
  }

  it should "support all primary operations" in {
    (10.0.kw + 15.0.kw).toDouble shouldBe 25.0
    (10.0.kw - 15.0.kw).toDouble shouldBe -5.0
    (10.0.kw * 2).toDouble shouldBe 20.0
  }

  it should "correctly convert to Energy given a duration" in {
    given tick: FiniteDuration = 30.minutes

    val e = 5.0.kw
    e.toEnergy.toDouble shouldBe 2.5
  }

  it should "combine multiple value" in {
    val l = List(5.0.kw, 10.0.kw, -8.0.kw)
    l.combineAll.toDouble shouldBe 7.0
  }

  it should "compare values" in {
    (5.0.kw > 3.0.kw) shouldBe true
  }

  it should "show correctly" in {
    val p = 5.0.kw
    showPower.show(p) shouldBe "5.00 kW"
  }
}
