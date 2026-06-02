package org.gridsim.core.model.battery

import cats.data.ValidatedNec
import cats.syntax.all.*
import org.gridsim.core.common.Units.{Energy, Power}
import org.gridsim.core.model.battery.{Battery, BatterySpecification, BatteryState}

case class Battery private[model](spec: BatterySpecification, state: BatteryState)

object Battery:
  type ValidationResult[A] = ValidatedNec[String, A]

  def makeBattery(spec: BatterySpecification, state: BatteryState): ValidationResult[Battery] =
    (
      BatterySpecification.validate(spec),
      BatteryState.validate(state, spec)
    ).mapN((sp, st) => Battery(sp, st))
    
  
    
