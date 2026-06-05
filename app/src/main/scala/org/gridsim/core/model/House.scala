package org.gridsim.core.model

import cats.data.ValidatedNec
import cats.implicits.*
import org.gridsim.core.behaviour.BatteryBehaviour
import org.gridsim.core.common.Units.{Energy, Flow}
import org.gridsim.core.model.battery.Battery
import cats.data.State
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.validation.{HouseValidator, Validator}
import org.gridsim.core.validation.Validator.*

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
  def makeHouse(id: String, size: Size, occupancy: Occupancy, components: List[HouseComponent] = Nil): ValidatedNec[DomainError, House] =
    House(id, size, occupancy, components).validate

  given Validator[House] = HouseValidator


