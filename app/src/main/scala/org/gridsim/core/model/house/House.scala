package org.gridsim.core.model.house

import cats.data.{State, ValidatedNec}
import cats.Traverse
import cats.implicits.*
import org.gridsim.core.behaviour.BatteryBehaviour
import org.gridsim.core.common.Units.{Energy, Flow}
import org.gridsim.core.model.*
import org.gridsim.core.model.battery.Battery
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.validation.Validator.*
import org.gridsim.core.validation.HouseComponentValidator.given
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
case class House[F[_]](
  id: String,
  size: Size,
  occupancy: Occupancy,
  components: F[HouseComponent]
) extends GridEntity

object House:
  def makeHouse[F[_]: Traverse](id: String, size: Size, occupancy: Occupancy, components: F[HouseComponent]): ValidatedNec[DomainError, House[F]] =
    House(id, size, occupancy, components).validate


  def makeEmptyHouse(id: String, size: Size, occupancy: Occupancy): ValidatedNec[DomainError, House[List]] =
    makeHouse[List](id, size, occupancy, List.empty)

  given [F[_] : Traverse](using Validator[HouseComponent]): Validator[House[F]] with
    def validate(h: House[F]): ValidatedNec[DomainError, House[F]] =
      HouseValidator.validate(h)


