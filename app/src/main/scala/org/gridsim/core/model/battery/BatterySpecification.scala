package org.gridsim.core.model.battery

import cats.syntax.all.*
import org.gridsim.core.common.Units.{Energy, Power}
import org.gridsim.core.model.battery.Battery.ValidationResult


case class BatterySpecification(capacity: Energy, maxPowerCharge: Power, maxPowerDischarge: Power, minSoC: Double = 0.1)

object BatterySpecification:
  def validate(spec: BatterySpecification): ValidationResult[BatterySpecification] =
    (
      validateEnergy(spec.capacity, "Capacity"),
      validatePower(spec.maxPowerCharge, "Max Power Charge"),
      validatePower(spec.maxPowerDischarge, "Max Power Discharge"),
      validatePercentuage(spec.minSoC, "MinSoC")
    ).mapN((c, mpc, mpd, msoc) => BatterySpecification(c, mpc, mpd, msoc))

  // TODO can be done better
  private def validateEnergy(v: Energy, field: String): ValidationResult[Energy] =
    if v.toDouble > 0 then v.validNec
    else s"$field must be greater than zero".invalidNec

  private def validatePower(v: Power, field: String): ValidationResult[Power] =
    if v.toDouble > 0 then v.validNec
    else s"$field must be greater than zero".invalidNec

  private def validatePercentuage(v: Double, field: String): ValidationResult[Double] =
    if v >= 0 && v <= 1 then v.validNec
    else s"$field must be a percentage".invalidNec
