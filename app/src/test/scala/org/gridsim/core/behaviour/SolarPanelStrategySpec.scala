package org.gridsim.core.behaviour

import org.gridsim.core.common.{Flow, Irradiance, Power, kw, kwh, wm2}
import org.gridsim.core.common.Flow.balanced
import org.gridsim.core.model.{SolarPanel, SolarPanelPhysics, SolarPanelState}
import org.gridsim.core.common.GeographicPoint
import org.gridsim.core.behaviour.producer.{SolarPanelStrategy, StandardSolarPanelStrategy}
import org.gridsim.core.behaviour.producer.SolarPanelStrategy.produce
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class SolarPanelStrategySpec extends AnyFlatSpec with Matchers with TableDrivenPropertyChecks:

  import org.gridsim.core.validation.SolarPanelValidator.given

  private val location = GeographicPoint(44.3, 11.7)
  
  private val (panel, state) =
    SolarPanel(
      id = "panel-01",
      location = location,
      maxProduction = 5.0.kw,
      areaSqm = 20.0,
      efficiency = 0.20
    ).toOption.get

  given FiniteDuration = 1.hour

  "StandardSolarPanelStrategy" should "produce zero energy at zero irradiance" in:
    val (_, flow) = StandardSolarPanelStrategy.produce(state, panel, Irradiance.Zero)
    flow shouldBe balanced

  it should "produce energy proportional to irradiance, area, and efficiency" in:
    // rawKw = 1000 W/m² × 20 m² × 0.20 / 1000 = 4.0 kW → 4.0 kWh over 1 h
    val (_, flow) = StandardSolarPanelStrategy.produce(state, panel, 1000.0.wm2)
    flow shouldBe Flow.Surplus(4.0.kwh)

  it should "cap output at peak power when irradiance is very high" in:
    // rawKw = 2000 W/m² × 20 m² × 0.20 / 1000 = 8.0 kW, capped at 5 kW
    val (_, flow) = StandardSolarPanelStrategy.produce(state, panel, 2000.0.wm2)
    flow shouldBe Flow.Surplus(5.0.kwh)

  private val cases = Table(
    ("irradiance W/m²", "expectedKwh"),
    (0.0,    0.0), // night
    (250.0,  1.0), // low light → 250 × 20 × 0.20 / 1000 = 1.0 kW
    (500.0,  2.0), // mid
    (1000.0, 4.0), // STC
    (1500.0, 5.0), // above STC, capped at peak
  )

  it should "handle a range of irradiance values correctly" in:
    forAll(cases) { (irr, expectedKwh) =>
      val (_, flow) = StandardSolarPanelStrategy.produce(state, panel, irr.wm2)
      if expectedKwh == 0.0 then
        flow shouldBe balanced
      else
        flow shouldBe Flow.Surplus(expectedKwh.kwh)
    }

  "SolarPanelStrategy.forPhysics" should "return StandardSolarPanelStrategy for Standard physics" in:
    SolarPanelStrategy.forPhysics(SolarPanelPhysics.Standard) shouldBe StandardSolarPanelStrategy

  "SolarPanelState.produce extension" should "delegate to the given strategy" in:
    given SolarPanelStrategy = StandardSolarPanelStrategy
    val (nextState, flow) = state.produce(panel, 1000.0.wm2)
    flow shouldBe Flow.Surplus(4.0.kwh)
