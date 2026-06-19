package org.gridsim.core.model

import cats.data.ValidatedNec
import org.gridsim.core.common.{GeographicPoint, Power}
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.{Producer, ProducerState}
import org.gridsim.core.validation.Validator
import org.gridsim.core.validation.Validator.validate

/**
 * Enumerates the physical models available for Photovoltaic production calculation.
 *
 * Used to dispatch the appropriate [[PhotovoltaicStrategy]].
 */
enum SolarPanelPhysics:
  case Standard

/**
 * Physical specification of a photovoltaic panel array.
 *
 * @param peakPower  Rated peak power under standard test conditions (kW).
 * @param areaSqm    Total panel area (m²). Used together with [[efficiency]]
 *                   for the irradiance-to-power conversion.
 * @param efficiency Panel efficiency (0.0–1.0), typically 0.15–0.22 for silicon.
 */
case class SolarPanelSpecification(peakPower: Power, areaSqm: Double, efficiency: Double)

/**
 * Runtime state of a photovoltaic panel array.
 *
 * @param outputPower Instantaneous power produced at the last tick (kW),
 *               kept for reporting and diagnostics.
 */
case class SolarPanelState(outputPower: Power) extends ProducerState

/**
 * A photovoltaic panel array that converts solar irradiance into electrical energy.
 *
 * It extends [[Producer]] so it can sit inside a [[House]]'s producer list and
 * participate in the energy-resolution pipeline via [[EnergyExchanger]].
 */
trait SolarPanel extends Producer:
  override def state: SolarPanelState
  def location: GeographicPoint
  def specification: SolarPanelSpecification
  def physics: SolarPanelPhysics

  def withState(state: SolarPanelState): SolarPanel

private case class SolarPanelImpl(
                                   id: String,
                                   state: SolarPanelState,
                                   location: GeographicPoint,
                                   specification: SolarPanelSpecification,
                                   physics: SolarPanelPhysics
) extends SolarPanel:

  override def withState(state: SolarPanelState): SolarPanel =
    copy(state = state)

object SolarPanel:
  def apply(
             id: String,
             state: SolarPanelState = SolarPanelState(Power.Zero),
             location: GeographicPoint,
             spec: SolarPanelSpecification,
             physics: SolarPanelPhysics = SolarPanelPhysics.Standard
      )(using Validator[SolarPanel]): ValidatedNec[DomainError, SolarPanel] =
    SolarPanelImpl(id, state, location, spec, physics).asInstanceOf[SolarPanel].validate
