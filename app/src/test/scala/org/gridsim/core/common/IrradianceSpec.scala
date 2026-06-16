package org.gridsim.core.common

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IrradianceSpec extends AnyFlatSpec with Matchers:

  "Irradiance.apply" should "create a valid irradiance value from a non-negative double" in:
    val i = Irradiance(100.0)
    i.toDouble shouldBe 100.0

  it should "allow zero as a valid value" in:
    Irradiance(0.0).toDouble shouldBe 0.0

  it should "reject negative values" in:
    an [IllegalArgumentException] should be thrownBy Irradiance(-1.0)

  "Zero" should "represent 0 irradiance" in:
    Irradiance.Zero.toDouble shouldBe 0.0

  "<" should "compare irradiance values correctly" in:
    Irradiance(10.0) < Irradiance(20.0) shouldBe true
    Irradiance(20.0) < Irradiance(10.0) shouldBe false

  ">" should "compare irradiance values correctly" in:
    Irradiance(20.0) > Irradiance(10.0) shouldBe true
    Irradiance(10.0) > Irradiance(20.0) shouldBe false

  "*" should "scale irradiance correctly" in:
    (Irradiance(100.0) * 0.5).toDouble shouldBe 50.0
    (Irradiance(200.0) * 2.0).toDouble shouldBe 400.0

  "Double.wm2" should "convert a Double into Irradiance" in:
    val i: Irradiance = 120.0.wm2
    i.toDouble shouldBe 120.0

  "Show[Irradiance]" should "format irradiance with unit W/m²" in:
    import cats.syntax.show.*
    Irradiance(100.0).show shouldBe "100.0 W/m²"
