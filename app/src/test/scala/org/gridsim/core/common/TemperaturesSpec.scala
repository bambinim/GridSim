package org.gridsim.core.common

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.gridsim.core.common.Temperatures.*
import cats.syntax.show.*
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TemperaturesSpec extends AnyFlatSpec with Matchers:

  // Construction
  "Temperature.celsius" should "accept valid values" in:
    noException should be thrownBy Temperature.celsius(100.0)
    noException should be thrownBy Temperature.celsius(-273.15)

  it should "reject values below absolute zero" in:
    an[IllegalArgumentException] should be thrownBy Temperature.celsius(-273.16)
    an[IllegalArgumentException] should be thrownBy Temperature.celsius(-300.0)

  "Temperature.kelvin" should "accept valid values" in:
    noException should be thrownBy Temperature.kelvin(0.0)
    noException should be thrownBy Temperature.kelvin(373.15)

  it should "reject negative values" in:
    an[IllegalArgumentException] should be thrownBy Temperature.kelvin(-0.01)
    an[IllegalArgumentException] should be thrownBy Temperature.kelvin(-1.0)

  "Temperature.fahrenheit" should "accept valid values" in:
    noException should be thrownBy Temperature.fahrenheit(-459.67)
    noException should be thrownBy Temperature.fahrenheit(212.0)

  it should "reject values below absolute zero" in:
    an[IllegalArgumentException] should be thrownBy Temperature.fahrenheit(-459.68)
    an[IllegalArgumentException] should be thrownBy Temperature.fahrenheit(-500.0)

  // Value
  "Temperature" should "expose its underlying Double value" in:
    Temperature.celsius(100.0).toDouble shouldBe 100.0
    Temperature.kelvin(373.15).toDouble shouldBe 373.15
    Temperature.fahrenheit(212.0).toDouble shouldBe 212.0

  // Arithmetic
  it should "support addition of a delta" in:
    (Temperature.celsius(100.0) + 50.0).toDouble shouldBe 150.0

  it should "reject addition that goes below absolute zero" in:
    an[IllegalArgumentException] should be thrownBy (Temperature.celsius(-270.0) + (-10.0))

  it should "support subtraction of a delta" in:
    (Temperature.celsius(100.0) - 50.0).toDouble shouldBe 50.0

  it should "reject subtraction that goes below absolute zero" in:
    an[IllegalArgumentException] should be thrownBy (Temperature.kelvin(1.0) - 5.0)

  // Delta
  it should "compute positive delta between two temperatures of the same unit" in:
    Temperature.celsius(100.0).delta(Temperature.celsius(20.0)) shouldBe 80.0

  it should "compute negative delta between two temperatures of the same unit" in :
    Temperature.celsius(20.0).delta(Temperature.celsius(100.0)) shouldBe -80.0

  // Comparison
  it should "compare temperatures with ===" in:
    (Temperature.celsius(100.0) === Temperature.celsius(100.0)) shouldBe true

  it should "compare temperatures with >" in:
    (Temperature.celsius(100.0) > Temperature.celsius(20.0)) shouldBe true

  it should "compare temperatures with <" in:
    (Temperature.celsius(20.0) < Temperature.celsius(100.0)) shouldBe true

  // Conversions and Any type
  "Temperature[Celsius]" should "convert to Kelvin" in:
    Temperature.celsius(0.0).toAny.toKelvin.toDouble shouldBe 273.15
    Temperature.celsius(100.0).toKelvin.toDouble shouldBe 373.15

  it should "convert to Fahrenheit" in:
    Temperature.celsius(0.0).toAny.toFahrenheit.toDouble shouldBe 32.0
    Temperature.celsius(100.0).toFahrenheit.toDouble shouldBe 212.0

  "Temperature[Kelvin]" should "convert to Celsius" in:
    Temperature.kelvin(273.15).toAny.toCelsius.toDouble shouldBe 0.0
    Temperature.kelvin(373.15).toCelsius.toDouble shouldBe 100.0

  it should "convert to Fahrenheit" in:
    Temperature.kelvin(273.15).toAny.toFahrenheit.toDouble shouldBe 32.0
    Temperature.kelvin(373.15).toFahrenheit.toDouble shouldBe 212.0

  "Temperature[Fahrenheit]" should "convert to Celsius" in:
    Temperature.fahrenheit(32.0).toAny.toCelsius.toDouble shouldBe 0.0
    Temperature.fahrenheit(212.0).toCelsius.toDouble shouldBe 100.0

  it should "convert to Kelvin" in:
    Temperature.fahrenheit(32.0).toAny.toKelvin.toDouble shouldBe 273.15
    Temperature.fahrenheit(212.0).toKelvin.toDouble shouldBe 373.15

  // Show
  "Show" should "format Celsius correctly" in:
    Temperature.celsius(100.0).show shouldBe "100.00°C"

  it should "format Kelvin correctly" in:
    Temperature.kelvin(373.15).show shouldBe "373.15 K"

  it should "format Fahrenheit correctly" in:
    Temperature.fahrenheit(212.0).show shouldBe "212.00°F"

  // Round-trip
  "Conversions" should "round-trip Celsius -> Kelvin -> Celsius" in:
    val result = Temperature.celsius(100.0).toKelvin.toCelsius.toDouble
    math.round(result * 100) / 100.0 shouldBe 100.0

  it should "round-trip Celsius -> Fahrenheit -> Celsius" in:
    val result = Temperature.celsius(100.0).toFahrenheit.toCelsius.toDouble
    math.round(result * 100) / 100.0 shouldBe 100.0

  it should "round-trip Kelvin -> Fahrenheit -> Kelvin" in:
    val result = Temperature.kelvin(373.15).toFahrenheit.toKelvin.toDouble
    math.round(result * 100) / 100.0 shouldBe 373.15
