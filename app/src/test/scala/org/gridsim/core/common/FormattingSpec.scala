package org.gridsim.core.common

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.gridsim.core.common.Formatting.*
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FormattingSpec extends AnyFlatSpec with Matchers:

  "show2" should "format a whole number with two decimal places" in:
    100.0.show2 shouldBe "100.00"

  it should "format a value with one decimal place by padding" in:
    3.1.show2 shouldBe "3.10"

  it should "format a value with exactly two decimal places unchanged" in:
    3.14.show2 shouldBe "3.14"

  it should "round half-up on the third decimal place" in:
    3.145.show2 shouldBe "3.15"
    3.144.show2 shouldBe "3.14"

  it should "format zero correctly" in:
    0.0.show2 shouldBe "0.00"

  it should "format negative values correctly" in:
    -3.14.show2 shouldBe "-3.14"
    -0.005.show2 shouldBe "-0.01"

  it should "use a dot as decimal separator regardless of system locale" in:
    1.5.show2 shouldBe "1.50"
    1.5.show2 should not contain ","
