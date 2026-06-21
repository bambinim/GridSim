package org.gridsim.core.model.storage.battery

import cats.data.ValidatedNec
import org.gridsim.core.common.{Energy, Power}
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.storage.Storage
import org.gridsim.core.validation.Validator
import org.gridsim.core.validation.Validator.validate

case class Battery(
  id: String,
  model: BatteryModel = BatteryModel.Standard,
  maxCapacity: Energy,
  maxPowerCharge: Power,
  maxPowerDischarge: Power,
  minSoC: Double = 0.2
) extends Storage

object Battery:
  def make(
    entity: Battery,
    state: BatteryState
  )(using Validator[(Battery, BatteryState)]): ValidatedNec[DomainError, (Battery, BatteryState)] =
    (entity, state).validate
