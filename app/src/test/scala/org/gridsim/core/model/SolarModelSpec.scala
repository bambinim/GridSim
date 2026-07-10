package org.gridsim.core.model

import org.gridsim.core.common.{Irradiance, toDouble, wm2}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SolarModelSpec extends AnyFlatSpec with Matchers:

  private val tolerance = 0.5 // degrees or hours

  // solarDeclinationDeg

  "solarDeclinationDeg" should "be close to +23.45° at the summer solstice (day 172)" in:
    SolarModel.solarDeclinationDeg(172) shouldBe 23.45 +- tolerance

  it should "be close to -23.45° at the winter solstice (day 355)" in:
    SolarModel.solarDeclinationDeg(355) shouldBe -23.45 +- tolerance

  it should "be close to 0° at the spring equinox (day ~81)" in:
    SolarModel.solarDeclinationDeg(81) shouldBe 0.0 +- tolerance

  // dayLengthHours

  "dayLengthHours" should "be approximately 12 h at the equator year-round" in:
    val dec = SolarModel.solarDeclinationDeg(172) // summer solstice
    SolarModel.dayLengthHours(0.0, dec) shouldBe 12.0 +- 0.1

  it should "return 24 h during polar day" in:
    // Arctic, summer solstice: polar day
    SolarModel.dayLengthHours(80.0, 23.45) shouldBe 24.0

  it should "return 0 h during polar night" in:
    // Arctic, winter solstice: polar night
    SolarModel.dayLengthHours(80.0, -23.45) shouldBe 0.0

  it should "produce longer days in summer than winter at mid-latitudes" in:
    val summerDec = SolarModel.solarDeclinationDeg(172)
    val winterDec = SolarModel.solarDeclinationDeg(355)
    val lat = 45.0
    SolarModel.dayLengthHours(lat, summerDec) should be > SolarModel.dayLengthHours(lat, winterDec)

  // sunriseSunset

  "sunriseSunset" should "be symmetric around 12:00" in:
    val (rise, set) = SolarModel.sunriseSunset(45.0, 10.0)
    rise + set shouldBe 24.0 +- 0.001

  // noonElevationDeg

  "noonElevationDeg" should "be 90° at the equator on an equinox" in:
    SolarModel.noonElevationDeg(0.0, 0.0) shouldBe 90.0 +- 0.01

  it should "be 90° at the tropic on the corresponding solstice" in:
    SolarModel.noonElevationDeg(23.45, 23.45) shouldBe 90.0 +- 0.01

  it should "never be negative" in:
    SolarModel.noonElevationDeg(80.0, -23.45) should be >= 0.0

  // clearSkyIrradiance

  "clearSkyIrradiance" should "be 1000 W/m² when the sun is directly overhead" in:
    SolarModel.clearSkyIrradiance(90.0).toDouble shouldBe 1000.0 +- 1.0

  it should "be lower when the sun is at a low elevation" in:
    SolarModel.clearSkyIrradiance(30.0).toDouble should be < 1000.0

  // irradianceAt

  "irradianceAt" should "return zero before sunrise" in:
    SolarModel.irradianceAt(4.0, 6.0, 18.0, 800.0.wm2) shouldBe Irradiance.Zero

  it should "return zero after sunset" in:
    SolarModel.irradianceAt(20.0, 6.0, 18.0, 800.0.wm2) shouldBe Irradiance.Zero

  it should "peak at solar noon" in:
    val peak = 800.0.wm2
    val noon = SolarModel.irradianceAt(12.0, 6.0, 18.0, peak)
    val morning = SolarModel.irradianceAt(9.0, 6.0, 18.0, peak)
    noon.toDouble should be > morning.toDouble

  // localSolarHour

  "localSolarHour" should "return the same hour at longitude 0°" in:
    SolarModel.localSolarHour(12.0, 0.0) shouldBe 12.0 +- 0.001

  it should "shift by 1 h per 15° of longitude" in:
    SolarModel.localSolarHour(12.0, 15.0) shouldBe 13.0 +- 0.001

  it should "wrap around at 24 h" in:
    val result = SolarModel.localSolarHour(23.0, 30.0)
    result shouldBe 1.0 +- 0.001

  // seasonalTemperatureOffsetC

  "seasonalTemperatureOffsetC" should "be near zero at the equator" in:
    SolarModel.seasonalTemperatureOffsetC(0.0, 23.45) shouldBe 0.0 +- 0.01

  it should "be positive in northern summer (positive declination, positive latitude)" in:
    SolarModel.seasonalTemperatureOffsetC(45.0, 23.45) should be > 0.0

  it should "be negative in northern winter (negative declination, positive latitude)" in:
    SolarModel.seasonalTemperatureOffsetC(45.0, -23.45) should be < 0.0

  // dailyTemperatureOffsetC

  "dailyTemperatureOffsetC" should "be warmest in mid-afternoon and coldest before dawn" in:
    val afternoon = SolarModel.dailyTemperatureOffsetC(15.0)
    val predawn = SolarModel.dailyTemperatureOffsetC(3.0)
    afternoon should be > predawn
