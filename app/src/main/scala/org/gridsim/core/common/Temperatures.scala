package org.gridsim.core.common

import cats.Show

import scala.annotation.targetName

object Temperatures:

  /** Marker trait for temperature units. */
  trait TemperatureUnit

  /** Represents the Celsius (°C) temperature unit. */
  trait Celsius extends TemperatureUnit
  /** Represents the Kelvin (K) temperature unit. */
  trait Kelvin extends TemperatureUnit
  /** Represents the Fahrenheit (°F) temperature unit. */
  trait Fahrenheit extends TemperatureUnit

  /**
   * A type-safe temperature value parameterized by its unit [[U]].
   * Backed by a raw [[Double]] at runtime (opaque — no boxing overhead).
   * Cannot be constructed directly; use [[Temperature.celsius]], [[Temperature.kelvin]],
   * or [[Temperature.fahrenheit]] smart constructors.
   */
  opaque type Temperature[U <: TemperatureUnit] = Double

  object Temperature:
    /**
     * Bypasses validation to construct a [[Temperature[U]] directly from a raw Double.
     * Used internally for unit conversions that are guaranteed to produce valid values
     * (e.g. Celsius → Kelvin always yields a non-negative Kelvin value).
     * Not accessible outside [[Temperatures]].
     */
    private[Temperatures] def unsafe[U <: TemperatureUnit](value: Double): Temperature[U] = value

    /**
     * Smart constructor for [[Temperature[Celsius]].
     * @param value temperature in degrees Celsius
     * @throws IllegalArgumentException if value is below absolute zero (-273.15°C)
     */
    def celsius(value: Double): Temperature[Celsius] =
      require(value >= -273.15, s"$value°C is below absolute zero (-273.15°C)")
      value

    /**
     * Smart constructor for [[Temperature[Kelvin]].
     * @param value temperature in Kelvin
     * @throws IllegalArgumentException if value is negative (below absolute zero)
     */
    def kelvin(value: Double): Temperature[Kelvin] =
      require(value >= 0.0, s"${value}K is below absolute zero (0 K)")
      value

    /**
     * Smart constructor for [[Temperature[Fahrenheit]].
     * @param value temperature in degrees Fahrenheit
     * @throws IllegalArgumentException if value is below absolute zero (-459.67°F)
     */
    def fahrenheit(value: Double): Temperature[Fahrenheit] =
      require(value >= -459.67, s"$value°F is below absolute zero (-459.67°F)")
      value

    /**
     * Internal helper that delegates validation to the [[TempValidator]] type class instance
     * for unit [[U]]. Used by [[+]] and [[-]] to re-validate the result of arithmetic
     * without hardcoding the absolute zero floor for each unit.
     * @throws IllegalArgumentException (via the validator) if the value is out of range
     */
    private[Temperatures] def validated[U <: TemperatureUnit](value: Double)(using v: TempValidator[U]): Temperature[U] =
      v.validate(value)

    /**
     * Type class that abstracts over per-unit validation logic.
     * Each instance knows the absolute zero floor for its unit and delegates
     * to the corresponding smart constructor.
     */
    trait TempValidator[U <: TemperatureUnit]:
      /** Validates [[value]] for unit [[U]], throwing if out of range. */
      def validate(value: Double): Temperature[U]

    /** [[TempValidator]] instance for Celsius: delegates to [[celsius]]. */
    given TempValidator[Celsius] with
      def validate(v: Double): Temperature[Celsius] = celsius(v)

    /** [[TempValidator]] instance for Kelvin: delegates to [[kelvin]]. */
    given TempValidator[Kelvin] with
      def validate(v: Double): Temperature[Kelvin] = kelvin(v)

    /** [[TempValidator]] instance for Fahrenheit: delegates to [[fahrenheit]]. */
    given TempValidator[Fahrenheit] with
      def validate(v: Double): Temperature[Fahrenheit] = fahrenheit(v)

    /** Cats [[Show]] instance for Celsius — formats as "%.2f°C". */
    given showCelsius: Show[Temperature[Celsius]] = Show.show(t => f"${t.value}%.2f°C")

    /** Cats [[Show]] instance for Kelvin — formats as "%.2f K". */
    given showKelvin: Show[Temperature[Kelvin]] = Show.show(t => f"${t.value}%.2f K")

    /** Cats [[Show]] instance for Fahrenheit — formats as "%.2f°F". */
    given showFahrenheit: Show[Temperature[Fahrenheit]] = Show.show(t => f"${t.value}%.2f°F")

  // Generic extensions (all units)

  extension [U <: TemperatureUnit](t: Temperature[U])
    /** Unwraps the underlying [[Double]] value. */
    def value: Double = t

    /**
     * Signed difference between this temperature and [[other]] in the same unit.
     * Positive if this is warmer, negative if cooler.
     * For unsigned magnitude use [[math.abs]] on the result.
     */
    def delta(other: Temperature[U]): Double = t.value - other.value

    /** Returns true if this temperature is exactly equal to [[other]]. */
    def ===(other: Temperature[U]): Boolean = t.value == other.value

    /** Returns true if this temperature is strictly greater than [[other]]. */
    def >(other: Temperature[U]): Boolean = t.value > other.value

    /** Returns true if this temperature is strictly less than [[other]]. */
    def <(other: Temperature[U]): Boolean = t.value < other.value

  // Arithmetic extensions (units with a TempValidator)

  extension [U <: TemperatureUnit : Temperature.TempValidator](t: Temperature[U])
    /**
     * Adds a delta (in the same unit) to this temperature.
     * Re-validates the result — throws [[IllegalArgumentException]] if the
     * resulting value would fall below absolute zero for unit [[U]].
     */
    def +(delta: Double): Temperature[U] = Temperature.validated[U](t.value + delta)

    /**
     * Subtracts a delta (in the same unit) from this temperature.
     * Re-validates the result — throws [[IllegalArgumentException]] if the
     * resulting value would fall below absolute zero for unit [[U]].
     */
    def -(delta: Double): Temperature[U] = Temperature.validated[U](t.value - delta)

  extension (t: Temperature[Celsius])
    /**
     * Converts this Celsius temperature to Kelvin.
     * Formula: K = °C + 273.15
     * Uses [[unsafe]] since a valid Celsius value always yields a valid Kelvin value.
     */
    @targetName("Celsius2Kelvin")
    def toKelvin: Temperature[Kelvin] = Temperature.unsafe(t.value + 273.15)

    /**
     * Converts this Celsius temperature to Fahrenheit.
     * Formula: °F = °C × 9/5 + 32
     * Uses [[unsafe]] since a valid Celsius value always yields a valid Fahrenheit value.
     */
    @targetName("Celsius2Fahrenheit")
    def toFahrenheit: Temperature[Fahrenheit] = Temperature.unsafe(t.value * 9.0 / 5.0 + 32.0)

  extension (t: Temperature[Kelvin])
    /**
     * Converts this Kelvin temperature to Celsius.
     * Formula: °C = K - 273.15
     * Uses [[unsafe]] since a valid Kelvin value always yields a valid Celsius value.
     */
    @targetName("Kelvin2Celsius")
    def toCelsius: Temperature[Celsius] = Temperature.unsafe(t.value - 273.15)

    /**
     * Converts this Kelvin temperature to Fahrenheit.
     * Formula: °F = (K - 273.15) × 9/5 + 32  (inlined to avoid cross-extension chaining issues)
     * Uses [[unsafe]] since a valid Kelvin value always yields a valid Fahrenheit value.
     */
    @targetName("Kelvin2Fahrenheit")
    def toFahrenheit: Temperature[Fahrenheit] = Temperature.unsafe((t.value - 273.15) * 9.0 / 5.0 + 32.0)

  extension (t: Temperature[Fahrenheit])
    /**
     * Converts this Fahrenheit temperature to Celsius.
     * Formula: °C = (°F - 32) × 5/9
     * Uses [[unsafe]] since a valid Fahrenheit value always yields a valid Celsius value.
     */
    @targetName("Fahrenheit2Celsius")
    def toCelsius: Temperature[Celsius] = Temperature.unsafe((t.value - 32.0) * 5.0 / 9.0)

    /**
     * Converts this Fahrenheit temperature to Kelvin.
     * Formula: K = (°F - 32) × 5/9 + 273.15  (inlined to avoid cross-extension chaining issues)
     * Uses [[unsafe]] since a valid Fahrenheit value always yields a valid Kelvin value.
     */
    @targetName("Fahrenheit2Kelvin")
    def toKelvin: Temperature[Kelvin] = Temperature.unsafe((t.value - 32.0) * 5.0 / 9.0 + 273.15)
