package org.gridsim.core.model.house

import cats.{Alternative, Traverse}
import cats.data.ValidatedNec
import cats.implicits.*
import org.gridsim.core.behaviour.house.{ConsumptionStrategy, DefaultConsumptionStrategy}
import org.gridsim.core.model.*
import org.gridsim.core.model.house.HouseState
import org.gridsim.core.model.{GridEntity, Producer, Storage}
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.validation.Validator
import org.gridsim.core.validation.Validator.*
import org.gridsim.core.validation.HouseValidator
import org.gridsim.core.validation.HouseComponentValidator.given

/**
 * A House is a complex [[GridEntity]] that aggregates multiple components.
 * It can only contain entities that are marked with [[CanBeInHouse]].
 *
 * @tparam F The container type for components.
 */
case class House[F[_]] private[core](
  id: String,
  state: HouseState[F],
  strategy: ConsumptionStrategy = DefaultConsumptionStrategy.traditionalProfile
) extends GridEntity:
  def producers: F[Producer] = state.producers

  def storages: F[Storage] = state.storages

object House:
  /**
   * Smart constructor for a generic House.
   * Ensures that the [[House]] entity is valid upon instantiation.
   *
   * @return A [[ValidatedNec]] containing the house or accumulated [[DomainError]]s.
   */
  def makeHouse[F[_]: Traverse](id: String, producers: F[Producer], storages: F[Storage]): ValidatedNec[DomainError, House[F]] =
    val state = HouseState(producers,storages)
    House(id, state).validate

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
