package org.gridsim.core.model

import org.gridsim.core.common.{Irradiance, toDouble, wm2}

/**
 * Pure astronomical/solar formulas used to derive weather from a
 * [[GeographicPoint]] and a day-of-year / hour-of-day.
 *
 * Kept separate from [[Environment]].
 *
 * All angles are in degrees.
 */
private[model] object SolarModel:

  private val ClearSkyPeakIrradiance = 1000.0 // W/m², sun directly overhead, clear sky

  /**
   * Solar declination: the angle between the sun's rays and the equatorial
   * plane, which is what actually causes seasons. Uses Cooper's equation,
   * a standard approximation accurate to within ~1°.
   *
   * Ranges from -23.45° (Southern hemisphere summer solstice) to +23.45°
   * (Northern hemisphere summer solstice).
   *
   * @param dayOfYear 1-365
   */
  def solarDeclinationDeg(dayOfYear: Int): Double =
    23.45 * math.sin(math.toRadians(360.0 / 365.0 * (284 + dayOfYear)))

  /**
   * Length of daylight at a given latitude and declination, in hours.
   *
   * Clamped at the poles: returns 24 during polar day and 0 during polar
   * night, where the underlying formula would otherwise be undefined
   * (its `arcos` argument falls outside [-1, 1]).
   */
  def dayLengthHours(latitudeDeg: Double, declinationDeg: Double): Double =
    val arcosHourAngle = -math.tan(math.toRadians(latitudeDeg)) * math.tan(math.toRadians(declinationDeg))
    if arcosHourAngle <= -1.0 then 24.0 // polar day: sun never sets
    else if arcosHourAngle >= 1.0 then 0.0 // polar night: sun never rises
    else
      val hourAngleDeg = math.toDegrees(math.acos(arcosHourAngle))
      2.0 * hourAngleDeg / 15.0 // 15° of hour-angle per hour of Earth's rotation

  /** Sunrise/sunset as local solar hours, symmetric around solar noon (12:00). */
  def sunriseSunset(latitudeDeg: Double, declinationDeg: Double): (Double, Double) =
    val half = dayLengthHours(latitudeDeg, declinationDeg) / 2.0
    (12.0 - half, 12.0 + half)

  /** Sun's elevation above the horizon at solar noon, how "high" it gets that day. */
  def noonElevationDeg(latitudeDeg: Double, declinationDeg: Double): Double =
    math.max(0.0, 90.0 - math.abs(latitudeDeg - declinationDeg))

  /**
   * Clear-sky irradiance when the sun is at the given elevation.
   * Simplified model: intensity scales with sin(elevation) — the sun's
   * rays spread over more surface area (and pass through more atmosphere)
   * the lower they sit.
   */
  def clearSkyIrradiance(elevationDeg: Double): Irradiance =
    (ClearSkyPeakIrradiance * math.sin(math.toRadians(elevationDeg))).wm2

  /**
   * Irradiance at a given local solar hour, following a half-sine curve
   * between sunrise and sunset, peaking at `peak` at solar noon and zero
   * outside daylight hours.
   */
  def irradianceAt(localSolarHour: Double, sunrise: Double, sunset: Double, peak: Irradiance): Irradiance =
    if localSolarHour < sunrise || localSolarHour > sunset then Irradiance.Zero
    else
      val dayLength = sunset - sunrise
      val normalized = (localSolarHour - sunrise) / dayLength // 0 at sunrise, 1 at sunset
      val shape = math.pow(math.sin(math.Pi * normalized), 2)
      (peak.toDouble * shape).wm2

  /**
   * Converts a global simulation hour (assumed to be solar time at
   * longitude 0°) into local solar time at the given longitude.
   * Every 15° of longitude shifts solar noon by one hour.
   */
  def localSolarHour(utcHour: Double, longitudeDeg: Double): Double =
    Math.floorMod(((utcHour + longitudeDeg / 15.0) * 3600).toLong, 24L * 3600).toDouble / 3600.0

  /**
   * Seasonal temperature offset, driven by declination but scaled by
   * latitude: seasons are mild near the equator and extreme near the
   * poles. Hemisphere is handled naturally — south of the equator, a
   * positive (Northern-summer) declination corresponds to local winter.
   */
  def seasonalTemperatureOffsetC(latitudeDeg: Double, declinationDeg: Double): Double =
    val amplitude = math.min(25.0, math.abs(latitudeDeg) * 0.5)
    val hemisphereSign = math.signum(latitudeDeg) match
      case 0.0 => 1.0
      case s   => s
    amplitude * hemisphereSign * (declinationDeg / 23.45)

  /** Diurnal temperature offset: warmest mid-afternoon, coldest before dawn. */
  def dailyTemperatureOffsetC(localSolarHour: Double): Double =
    5.0 * math.sin(math.toRadians((localSolarHour - 6.0) / 24.0 * 360.0))
