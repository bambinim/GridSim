package org.gridsim.core.model.battery

import cats.data.ValidatedNec
import cats.syntax.all.*
import org.gridsim.core.common.Units.{Energy, Power}
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.house.HouseComponent
import org.gridsim.core.validation.{BatteryValidator, Validator}
import org.gridsim.core.validation.Validator.validate

/**
 * A Battery is a [[HouseComponent]] capable of storing and releasing energy.
 * 
 * It is defined by a static specification (physical limits) and a dynamic state (current charge).
 * The battery's behavior is managed by the [[EnergyResolver]], which applies
 * charging and discharging logic based on available surplus or deficit.
 *
 * @param spec  The physical specifications of the battery.
 * @param state The current runtime state of the battery.
 */
case class Battery private[core](spec: BatterySpecification, state: BatteryState) extends HouseComponent

object Battery:
  def make(spec: BatterySpecification, state: BatteryState)(using Validator[Battery]): ValidatedNec[DomainError, Battery] =
    Battery(spec, state).validate

  given Validator[Battery] = BatteryValidator

extension (battery: Battery)
  def getBatteryLevel: Double =
    val charge = battery.state.currentCharge
    val capacity = battery.spec.capacity
    charge / capacity
