package org.gridsim.core.model.battery

import cats.data.ValidatedNec
import org.gridsim.core.model.{Storage, GridEntity}
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.validation.Validator
import org.gridsim.core.validation.Validator.validate

/**
 * A Battery is an entity capable of storing and releasing energy.
 * It can be used both inside a house and as a standalone grid component.
 *
 * @param id    The unique identifier for the battery.
 * @param spec  The physical specifications of the battery.
 * @param state The current runtime state of the battery.
 * @param model The specific battery model determining its behaviour.
 */
case class Battery private[core](
  id: String,
  spec: BatterySpecification,
  state: BatteryState,
  model: BatteryModel = BatteryModel.Standard
) extends Storage

object Battery:
  /**
   * Smart constructor for the [[Battery]] component.
   * By making the default constructor private and forcing creation through this method,
   * we guarantee that it is impossible to instantiate an invalid [[Battery]] in the system.
   */
  def make(
    id: String,
    spec: BatterySpecification,
    state: BatteryState,
    model: BatteryModel = BatteryModel.Standard
  )(using Validator[Battery]): ValidatedNec[DomainError, Battery] =
    Battery(id, spec, state, model).validate

extension (battery: Battery)
  /**
   * Calculates the current State of Charge (SoC) of the battery.
   */
  def getBatteryLevel: Double =
    val charge = battery.state.currentCharge
    val capacity = battery.spec.capacity
    charge / capacity
