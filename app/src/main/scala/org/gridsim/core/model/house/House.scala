package org.gridsim.core.model.house

import cats.data.{State, ValidatedNec}
import cats.implicits.*
import org.gridsim.core.behaviour.BatteryBehaviour
import org.gridsim.core.common.Units.{Energy, Flow}
import org.gridsim.core.model.*
import org.gridsim.core.model.battery.Battery
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.validation.Validator.*
import org.gridsim.core.validation.{HouseValidator, Validator}

/**
 * Represents the size of a house, which acts as a multiplier for base energy consumption.
 */
enum Size(val multiplier: Double):
  case Small  extends Size(1.0)
  case Medium extends Size(1.5)
  case Large  extends Size(2.0)

/**
 * Represents the occupancy profile of a house, determining the base energy demand pattern.
 */
enum Occupancy:
  case Traditional, SmartWorker, Vacant

/**
 * A House is a complex [[GridEntity]] that can contain multiple [[HouseComponent]]s.
 * 
 * It resolves its internal energy balance by first calculating a base consumption
 * based on its size and occupancy, and then delegating the residue to its components.
 *
 * @param id        Unique identifier for the house.
 * @param size      The physical size/scale of the house.
 * @param occupancy The behavior pattern of the inhabitants.
 * @param components List of internal components (e.g., Batteries, Solar Panels).
 */
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


