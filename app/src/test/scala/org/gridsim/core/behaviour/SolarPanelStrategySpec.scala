package org.gridsim.core.behaviour

import org.gridsim.core.common.{Energy, Flow, Irradiance, Power, kw, kwh, wm2}
import org.gridsim.core.common.Flow.{Balanced, Deficit, Surplus, balanced}
import org.gridsim.core.model.{SolarPanelPhysics, SolarPanelSpecification, SolarPanelState}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.junit.JUnitRunner
import org.gridsim.core.behaviour.SolarPanelLogic.given
import org.gridsim.core.behaviour.SolarPanelStrategy.produce

import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class SolarPanelStrategySpec extends AnyFlatSpec with Matchers with TableDrivenPropertyChecks:

  private val spec = SolarPanelSpecification(peakPower = 5.0.kw, areaSqm = 20.0, efficiency = 0.20)
  private val initialState = SolarPanelState(Power.Zero)

  given FiniteDuration = 1.hour

  // StandardSolarPanelStrategy

  "StandardSolarPanelStrategy" should "produce zero energy at zero irradiance" in:
    val (_, flow) = StandardSolarPanelStrategy.produce(initialState, Irradiance.Zero, spec)
    flow shouldBe balanced

  it should "produce energy proportional to irradiance, area, and efficiency" in:
    // rawKw = 1000 W/m² × 20 m² × 0.20 / 1000 = 4.0 kW → 4.0 kWh over 1 h
    val (_, flow) = StandardSolarPanelStrategy.produce(initialState, 1000.0.wm2, spec)
    flow shouldBe Flow.Surplus(4.0.kwh)

  it should "cap output at peak power when irradiance is very high" in:
    // rawKw = 2000 W/m² × 20 m² × 0.20 / 1000 = 8.0 kW, but peakPower = 5 kW → capped
    val (_, flow) = StandardSolarPanelStrategy.produce(initialState, 2000.0.wm2, spec)
    flow shouldBe Flow.Surplus(5.0.kwh)

  it should "update the state's outputPower after production" in:
    val (nextState, _) = StandardSolarPanelStrategy.produce(initialState, 1000.0.wm2, spec)
    nextState.outputPower.toDouble shouldBe 4.0

  it should "return Balanced flow and zero outputPower at night (irradiance = 0)" in:
    val (nextState, flow) = StandardSolarPanelStrategy.produce(initialState, Irradiance.Zero, spec)
    flow shouldBe balanced
    nextState.outputPower shouldBe Power.Zero

  // Table-driven: various irradiance levels

  private val cases = Table(
    ("irradiance W/m²", "expectedKwh"),
    (0.0,   0.0),   // night
    (250.0, 1.0),   // low light → 250 × 20 × 0.20 / 1000 = 1.0 kW
    (500.0, 2.0),   // mid
    (1000.0, 4.0),  // STC
    (1500.0, 5.0),  // above STC but capped at peak
  )

  it should "handle a range of irradiance values correctly" in:
    forAll(cases) { (irr, expectedKwh) =>
      val (_, flow) = StandardSolarPanelStrategy.produce(initialState, irr.wm2, spec)
      if expectedKwh == 0.0 then
        flow shouldBe balanced
      else
        flow shouldBe Flow.Surplus(expectedKwh.kwh)
    }

  // SolarPanelStrategy.forPhysics dispatch

  "SolarPanelStrategy.forPhysics" should "return StandardSolarPanelStrategy for Standard physics" in:
    SolarPanelStrategy.forPhysics(SolarPanelPhysics.Standard) shouldBe StandardSolarPanelStrategy

  // Extension: SolarPanelState.produce

  "SolarPanelState.produce extension" should "delegate to the given strategy" in:
    given SolarPanelStrategy = StandardSolarPanelStrategy
    val (nextState, flow) = initialState.produce(1000.0.wm2, spec)
    nextState.outputPower.toDouble shouldBe 4.0
    flow shouldBe Flow.Surplus(4.0.kwh)
