package org.gridsim.core.validation

import cats.data.ValidatedNec
import cats.implicits.*
import org.gridsim.core.model.*
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.house.{House, HouseState}
import org.gridsim.core.validation.Validator.*

object HouseValidator:

  def validate(pair: (House, HouseState))(using
    componentVal: Validator[(GridEntity, GridEntityState)]
  ): ValidatedNec[DomainError, (House, HouseState)] =
    val (entity, state) = pair

    val componentStateMap = state.componentStates.map(s => s.entityId -> s).toMap
    val componentResults =
      entity.components.map { c =>
        componentStateMap.get(c.id) match
          case Some(s) => componentVal.validate((c, s)).map(_ => c)
          case None    => DomainError.InvalidId("Component", c.id).invalidNec
      }
    val componentsValidation =
      componentResults.foldLeft(List.empty[GridEntity].validNec[DomainError]) { (acc, result) =>
        (acc, result).mapN(_ :+ _)
      }

    (
      entity.id.mustBeValid("House Id"),
      componentsValidation
      ).mapN((_, _) => pair)
