package org.gridsim.core.model.battery

import cats.data.ValidatedNec
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.house.HouseComponent
import org.gridsim.core.validation.Validator
import org.gridsim.core.validation.Validator.given
import org.gridsim.core.validation.Validator.validate
import org.gridsim.core.validation.BatteryValidator.given

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
  /**
   * Smart constructor for the [[Battery]] component.
   * By making the default constructor private and forcing creation through this method,
   * we guarantee that it is impossible to instantiate an invalid [[Battery]] in the system.
   */
  def make(spec: BatterySpecification, state: BatteryState)(using Validator[Battery]): ValidatedNec[DomainError, Battery] =
    Battery(spec, state).validate

extension (battery: Battery)
  /**
   * Calculates the current State of Charge (SoC) of the battery.
   */
  def getBatteryLevel: Double =
    val charge = battery.state.currentCharge
    val capacity = battery.spec.capacity
    charge / capacity
