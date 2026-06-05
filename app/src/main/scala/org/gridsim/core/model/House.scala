package org.gridsim.core.model

import cats.data.ValidatedNec
import cats.implicits.*
import org.gridsim.core.behaviour.BatteryBehaviour
import org.gridsim.core.common.Units.{Energy, Flow}
import org.gridsim.core.model.battery.Battery
import cats.data.State

enum Size(val multiplier: Double):
  case Small  extends Size(1.0)
  case Medium extends Size(1.5)
  case Large  extends Size(2.0)

enum Occupancy:
  case Traditional, SmartWorker, Vacant

enum HouseComponent:
  case BatteryComponent(battery: Battery)

case class House(
  id: String,
  size: Size,
  occupancy: Occupancy,
  components: List[HouseComponent] = Nil
) extends GridEntity

object House:
  type ValidationResult[A] = ValidatedNec[String, A]

  def makeHouse(id: String, size: Size, occupancy: Occupancy, components: List[HouseComponent] = Nil): ValidationResult[House] =
    validateId(id).map(vId => House(vId, size, occupancy, components))

  private def validateId(str: String): ValidationResult[String] =
    if str.length >= 3 then str.validNec
    else "Error: the id must have at least 3 characters".invalidNec
