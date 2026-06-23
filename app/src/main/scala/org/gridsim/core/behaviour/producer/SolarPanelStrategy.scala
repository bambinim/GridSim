package org.gridsim.core.behaviour.producer

import cats.syntax.all.*
import org.gridsim.core.common.Flow.{Surplus, balanced}
import org.gridsim.core.common.*
import org.gridsim.core.model.*

import scala.concurrent.duration.FiniteDuration

/** Strategy pattern for photovoltaic electrical production calculation. */
trait SolarPanelStrategy extends ProducerStrategy[SolarPanelState, SolarPanel, Irradiance]:
  def produce(state: SolarPanelState, panel: SolarPanel, irradiance: Irradiance)(using delta: FiniteDuration): (SolarPanelState, Flow[Energy])

object SolarPanelStrategy:
  def forPhysics(physics: SolarPanelPhysics): SolarPanelStrategy = physics match
    case SolarPanelPhysics.Standard => StandardSolarPanelStrategy

  extension (state: SolarPanelState)
    def produce(panel: SolarPanel, irradiance: Irradiance)(using delta: FiniteDuration, strategy: SolarPanelStrategy): (SolarPanelState, Flow[Energy]) =
      strategy.produce(state, panel, irradiance)

/**
 * Linear STC-based irradiance physics.
 *
 * Output power = irradiance (W/m²) × area (m²) × efficiency / 1000  [kW]
 *
 * Capped at [[SolarPanel.maxProduction]] to respect inverter limits.
 * When irradiance is 0 (night / overcast) no energy is produced.
 */
object StandardSolarPanelStrategy extends SolarPanelStrategy:
  def produce(state: SolarPanelState, panel: SolarPanel, irradiance: Irradiance)(using delta: FiniteDuration): (SolarPanelState, Flow[Energy]) =
    val rawKw = (irradiance.toDouble * panel.areaSqm * panel.efficiency) / 1000.0
    val production = panel.maxProduction.min(rawKw.kw)
    val energy = production.toEnergy

    val nextState = state.copy(efficiency = panel.efficiency)
    val flow = if energy > Energy.Zero then Surplus(energy) else balanced

    (nextState, flow)
