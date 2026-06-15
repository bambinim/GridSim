package org.gridsim.core.model.house

import cats.{Alternative, Traverse}
import cats.data.ValidatedNec
import cats.implicits.*
import org.gridsim.core.behaviour.house.{ConsumptionStrategy, DefaultConsumptionStrategy}
import org.gridsim.core.common.Units.{Energy, Flow}
import org.gridsim.core.model.*
import org.gridsim.core.model.{GridEntity, Producer, Storage}
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.validation.Validator
import org.gridsim.core.validation.Validator.*
import org.gridsim.core.validation.HouseValidator
import org.gridsim.core.validation.HouseComponentValidator.given

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
 * A House is a complex [[GridEntity]] that aggregates multiple components.
 * It can only contain entities that are marked with [[CanBeInHouse]].
 *
 * @tparam F The container type for components.
 */
case class House[F[_]] private[core](
  id: String,
  producers: F[Producer],
  storages: F[Storage],
  strategy: ConsumptionStrategy = DefaultConsumptionStrategy.traditionalProfile
) extends GridEntity

object House:
  /**
   * Smart constructor for a generic House.
   * Ensures that the [[House]] entity is valid upon instantiation.
   *
   * @return A [[ValidatedNec]] containing the house or accumulated [[DomainError]]s.
   */
  def makeHouse[F[_]: Traverse](id: String, producers: F[Producer], storages: F[Storage]): ValidatedNec[DomainError, House[F]] =
    House(id, producers, storages).validate

  /**
   * Helper to instantiate a House with no components (defaults to List).
   */
  def makeEmptyHouse(id: String): ValidatedNec[DomainError, House[List]] =
    makeHouse[List](id, Nil, Nil)

  /**
   * Helper to instantiate a House with a collection of storages and no producers.
   */
  def makeHouseWithStorages[F[_]: Traverse : Alternative](id: String, storages: F[Storage]): ValidatedNec[DomainError, House[F]] =
    makeHouse[F](id, Alternative[F].empty, storages)


  /**
   * Given instance to allow House entities to be validated recursively.
   */
  given [F[_] : Traverse]: Validator[House[F]] with
    def validate(h: House[F]): ValidatedNec[DomainError, House[F]] =
      HouseValidator.validate(h)
