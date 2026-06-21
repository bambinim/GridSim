package org.gridsim.core.common

import cats.Show
import Formatting.*

object Temperatures:

  trait TemperatureUnit
  trait Celsius extends TemperatureUnit
  trait Kelvin extends TemperatureUnit
  trait Fahrenheit extends TemperatureUnit

  opaque type Temperature[U <: TemperatureUnit] = Double
  opaque type AnyTemperature = Temperature[Celsius]

  object Temperature:
    private[Temperatures] def unsafe[U <: TemperatureUnit](value: Double): Temperature[U] = value

    /**
     * Smart constructor for [[Temperature[Celsius]].
     * @param value temperature in degrees Celsius
     * @throws IllegalArgumentException if value is below absolute zero (-273.15°C)
     */
    def celsius(value: Double): Temperature[Celsius] =
      require(value >= Celsius.AbsoluteZero, s"$value°C is below absolute zero (${Celsius.AbsoluteZero}°C)")
      value

    /**
     * Smart constructor for [[Temperature[Kelvin]].
     * @param value temperature in Kelvin
     * @throws IllegalArgumentException if value is below absolute zero (0.0 K)
     */
    def kelvin(value: Double): Temperature[Kelvin] =
      require(value >= Kelvin.AbsoluteZero, s"${value}K is below absolute zero (${Kelvin.AbsoluteZero} K)")
      value

    /**
     * Smart constructor for [[Temperature[Fahrenheit]].
     * @param value temperature in degrees Fahrenheit
     * @throws IllegalArgumentException if value is below absolute zero (-459.67°F)
     */
    def fahrenheit(value: Double): Temperature[Fahrenheit] =
      require(value >= Fahrenheit.AbsoluteZero, s"$value°F is below absolute zero (${Fahrenheit.AbsoluteZero}°F)")
      value

    /**
     * Internal helper that delegates validation to the [[TempValidator]] type class instance
     * for unit [[U]]. Used by [[+]] and [[-]] to re-validate the result of arithmetic
     * without hardcoding the absolute zero floor for each unit.
     * @throws IllegalArgumentException (via the validator) if the value is out of range
     */
    private[Temperatures] def validated[U <: TemperatureUnit](value: Double)(using v: TempValidator[U]): Temperature[U] =
      v.validate(value)

    /** Type class that abstracts over per unit validation logic. */
    trait TempValidator[U <: TemperatureUnit]:
      def validate(value: Double): Temperature[U]

    given TempValidator[Celsius] with
      def validate(v: Double): Temperature[Celsius] = celsius(v)
    given TempValidator[Kelvin] with
      def validate(v: Double): Temperature[Kelvin] = kelvin(v)
    given TempValidator[Fahrenheit] with
      def validate(v: Double): Temperature[Fahrenheit] = fahrenheit(v)

    given showCelsius: Show[Temperature[Celsius]] = Show.show(t => s"${t.show2}°C")
    given showKelvin: Show[Temperature[Kelvin]] = Show.show(t => s"${t.show2} K")
    given showFahrenheit: Show[Temperature[Fahrenheit]] = Show.show(t => s"${t.show2}°F")
  end Temperature

  extension [U <: TemperatureUnit](t: Temperature[U])
    def toDouble: Double = t

    /** Signed difference between this temperature and [[other]] in the same unit. */
    def delta(other: Temperature[U]): Double = t - other
    def ===(other: Temperature[U]): Boolean = t == other
    def >(other: Temperature[U]): Boolean = t > other
    def <(other: Temperature[U]): Boolean = t < other
  end extension

  // Arithmetic extensions (require a TempValidator)
  extension [U <: TemperatureUnit : Temperature.TempValidator](t: Temperature[U])
    def +(delta: Double): Temperature[U] = Temperature.validated[U](t + delta)
    def -(delta: Double): Temperature[U] = Temperature.validated[U](t - delta)
  end extension

  private def celsiusToKelvin(t: Temperature[Celsius]): Temperature[Kelvin] =
    Temperature.unsafe(t + 273.15)
  private def celsiusToFahrenheit(t: Temperature[Celsius]): Temperature[Fahrenheit] =
    Temperature.unsafe(t * 9.0 / 5.0 + 32.0)

  object Celsius:
    val AbsoluteZero = -273.15

    extension (t: Temperature[Celsius])
      def toKelvin: Temperature[Kelvin] = celsiusToKelvin(t)
      def toFahrenheit: Temperature[Fahrenheit] = celsiusToFahrenheit(t)
      def toAny: AnyTemperature = t
    end extension
  end Celsius

  private def kelvinToCelsius(t: Temperature[Kelvin]): Temperature[Celsius] =
    Temperature.unsafe(t - 273.15)

  object Kelvin:
    val AbsoluteZero = 0.0

    extension (t: Temperature[Kelvin])
      def toCelsius: Temperature[Celsius] = kelvinToCelsius(t)
      def toFahrenheit: Temperature[Fahrenheit] = celsiusToFahrenheit(kelvinToCelsius(t))
      def toAny: AnyTemperature = kelvinToCelsius(t)
    end extension
  end Kelvin

  private def fahrenheitToCelsius(t: Temperature[Fahrenheit]): Temperature[Celsius] =
    Temperature.unsafe((t - 32.0) * 5.0 / 9.0)

  object Fahrenheit:
    val AbsoluteZero = -459.67

    extension (t: Temperature[Fahrenheit])
      def toCelsius: Temperature[Celsius] = fahrenheitToCelsius(t)
      def toKelvin: Temperature[Kelvin] = celsiusToKelvin(fahrenheitToCelsius(t))
      def toAny: AnyTemperature = fahrenheitToCelsius(t)
    end extension
  end Fahrenheit

  extension (t: AnyTemperature)
    def toCelsius: Temperature[Celsius] = t
    def toKelvin: Temperature[Kelvin] = celsiusToKelvin(t)
    def toFahrenheit: Temperature[Fahrenheit] = celsiusToFahrenheit(t)
  end extension
end Temperatures
