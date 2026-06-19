package org.gridsim.core.behaviour

import org.gridsim.core.behaviour.EnergyExchanger
import org.gridsim.core.behaviour.SolarPanelStrategy.produce
import org.gridsim.core.common.{Energy, Flow, Irradiance, Power, kw, toDouble}
import org.gridsim.core.common.Flow.{Surplus, balanced}
import org.gridsim.core.model.{Environment, SolarPanel, SolarPanelPhysics, SolarPanelSpecification, SolarPanelState}

import cats.syntax.all.*

import scala.concurrent.duration.FiniteDuration

/**
 * Strategy pattern for PV electrical production calculation.
 *
 * Implementations define how a panel state and a measured irradiance
 * combine to yield an output power (and thus an energy surplus for the tick).
 */
trait SolarPanelStrategy:
  /**
   * Computes the energy produced during one simulation tick.
   *
   * @param state      Current panel state.
   * @param irradiance Incident irradiance (W/m²) during the tick.
   * @param specification       Physical characteristics of the array.
   * @param delta      Duration of the tick.
   * @return Updated [[SolarPanelState]] and the generated [[Flow]].
   */
  def produce(state: SolarPanelState, irradiance: Irradiance, specification: SolarPanelSpecification)(using delta: FiniteDuration): (SolarPanelState, Flow[Energy])

object SolarPanelStrategy:
  def forPhysics(physics: SolarPanelPhysics): SolarPanelStrategy = physics match
    case SolarPanelPhysics.Standard => StandardSolarPanelStrategy

  extension (state: SolarPanelState)
    def produce(irradiance: Irradiance, specification: SolarPanelSpecification)(using delta: FiniteDuration, strategy: SolarPanelStrategy): (SolarPanelState, Flow[Energy]) =
      strategy.produce(state, irradiance, specification)

/**
 * Linear STC-based irradiance physics.
 *
 * Output power = irradiance (W/m²) × area (m²) × efficiency / 1000  [kW]
 *
 * Capped at [[SolarPanelSpecification.peakPower]] to respect inverter limits.
 * When irradiance is 0 (night / overcast) no energy is produced.
 */
object StandardSolarPanelStrategy extends SolarPanelStrategy:
  def produce(
               state: SolarPanelState,
               irradiance: Irradiance,
               specification: SolarPanelSpecification
             )(using delta: FiniteDuration): (SolarPanelState, Flow[Energy]) =
    val rawKw = (irradiance.toDouble * specification.areaSqm * specification.efficiency) / 1000.0
    val output = specification.peakPower.min(rawKw.kw)
    val energy = output.toEnergy

    val nextState = state.copy(output)
    val flow = if energy > Energy.Zero then Surplus(energy) else balanced

    (nextState, flow)

/**
 * Logic implementation for the [[SolarPanel]] entity.
 *
 * A PV panel is a pure producer: it converts the incoming flow into a
 * combined flow (production + whatever was already in the pipeline).
 *
 * - If the pipeline carries a [[Flow.Surplus]] the panel's output is added to it.
 * - If the pipeline carries a [[Flow.Deficit]] the panel's production reduces it
 *   (or flips it to surplus when output exceeds the deficit).
 * - The panel never absorbs energy, so it never changes a deficit to a larger deficit.
 */
object SolarPanelLogic:
  given EnergyExchanger[SolarPanelState, SolarPanel] with
    def exchange(
                  state: SolarPanelState,
                  panel: SolarPanel,
                  flow: Flow[Energy],
                  environment: Environment
                )(using delta: FiniteDuration): (SolarPanelState, Flow[Energy]) =
      given SolarPanelStrategy = SolarPanelStrategy.forPhysics(panel.physics)

      val weather = environment.weather(panel.location)
      val (nextState, produced) = state.produce(weather.irradiance, panel.specification)

      // Combine the panel's production with the current pipeline flow.
      // Flow.+ already handles all sign combinations correctly.
      val residue = flow + produced

      (nextState, residue)
