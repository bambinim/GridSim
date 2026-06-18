package org.gridsim.core.model.house

import cats.data.ValidatedNec
import cats.implicits.*
import org.gridsim.core.behaviour.house.{ConsumptionStrategy, DefaultConsumptionStrategy}
import org.gridsim.core.model.*
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.validation.Validator
import org.gridsim.core.validation.Validator.*
import org.gridsim.core.validation.HouseValidator
import org.gridsim.core.validation.HouseComponentValidator.componentValidator

/**
 * Static configuration of a house and the grid components installed in it.
 *
 * Components are generic grid entities so the house can contain storage today
 * and producer entities such as solar panels when their evolution is added.
 */
case class House(
  id: String,
  components: List[GridEntity],
  strategy: ConsumptionStrategy = DefaultConsumptionStrategy.traditionalProfile
) extends GridEntity

object House:
  /**
   * Helper to validate a house entity-state pair.
   */
  def make(
     entity: House,
     state: HouseState
   ): ValidatedNec[DomainError, (House, HouseState)] =
    (entity, state).validate

  /**
   * Given instance to allow House entities to be validated recursively.
   */
  given Validator[(House, HouseState)] with
    def validate(pair: (House, HouseState)): ValidatedNec[DomainError, (House, HouseState)] =
      HouseValidator.validate(pair)
