package org.gridsim.core.model

import cats.data.ValidatedNec
import org.gridsim.core.common.{GeographicPoint, Power}
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.{Producer, ProducerState}
import org.gridsim.core.validation.Validator
import org.gridsim.core.validation.Validator.validate
import org.gridsim.core.validation.SolarPanelValidator.given

/** Physical models available for Photovoltaic production calculation. */
enum SolarPanelPhysics:
  case Standard

/** Runtime state of a photovoltaic panel. */
case class SolarPanelState(entityId: String, currentProduction: Power = Power.Zero) extends ProducerState

/** A photovoltaic panel array that converts solar irradiance into electrical energy. */
trait SolarPanel extends Producer:
  def physics: SolarPanelPhysics
  def location: GeographicPoint
  def maxProduction: Power
  /** Total panel area (m²) */
  def areaSqm: Double
  /** Panel efficiency (0.0–1.0) */
  def efficiency: Double

private case class SolarPanelImpl(
                                   id: String,
                                   physics: SolarPanelPhysics,
                                   location: GeographicPoint,
                                   maxProduction: Power,
                                   areaSqm: Double,
                                   efficiency: Double
) extends SolarPanel

case class SolarPanelWithState(panel: SolarPanel, state: SolarPanelState)
  extends GridEntityWithState[SolarPanel, SolarPanelState](panel, state)

object SolarPanel:
  def apply(
             id: String,
             location: GeographicPoint,
             maxProduction: Power,
             areaSqm: Double,
             efficiency: Double,
             state: SolarPanelState,
             physics: SolarPanelPhysics = SolarPanelPhysics.Standard
      )(using Validator[SolarPanelWithState]): ValidatedNec[DomainError, SolarPanelWithState] =
    SolarPanelWithState(
      SolarPanelImpl(id, physics, location, maxProduction, areaSqm, efficiency).asInstanceOf[SolarPanel],
      state
    ).validate
