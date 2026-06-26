package org.gridsim.dsl.grid.entities

import cats.data.ValidatedNec
import org.gridsim.dsl.GridEntityBuilder
import org.gridsim.core.behaviour.house.ConsumptionStrategy
import org.gridsim.core.model.house.{House, HouseState}
import org.gridsim.core.model.{GridEntity, GridEntityState}
import cats.Show
import cats.syntax.all._
import org.gridsim.core.model.storage.Storage
import org.gridsim.core.model.storage.StorageState
import org.gridsim.dsl.DSLBuilderError
import org.gridsim.core.validation.HouseValidator.validate
import org.gridsim.core.validation.HouseComponentValidator.componentValidator

case class HouseBuilder(
    private[dsl] val id: Option[String],
    private[dsl] val consumptionStrategy: Option[ConsumptionStrategy],
    private[dsl] val otherEntities: List[
      GridEntityBuilder[GridEntity, GridEntityState]
    ],
    private[dsl] val storages: List[GridEntityBuilder[Storage, StorageState]]
) extends GridEntityBuilder[House, HouseState]:
  import org.gridsim.dsl.DSLError
  override def build(): ValidatedNec[DSLError, (House, HouseState)] = {
    val validatedStrategy = consumptionStrategy.toValidNec(
      DSLBuilderError.MissingField("consumptionStrategy")
    )
    val components = (storages ++ otherEntities).traverse(_.build())

    (validatedStrategy, components).tupled.andThen {
      case (strategy, components) =>
        val entity = House(
          id.getOrElse(java.util.UUID.randomUUID().toString),
          components.map(_._1),
          strategy
        )
        val state = HouseState(entity.id, components.map(_._2))
        validate((entity, state))
    }
  }

object HouseBuilder:
  class HouseBuilderContext:
    var entityId: Option[String] = None
    var consumptionStrategy: Option[ConsumptionStrategy] = None
    var otherEntities: List[GridEntityBuilder[GridEntity, GridEntityState]] =
      List.empty
    var storages: List[GridEntityBuilder[Storage, StorageState]] = List.empty

  def house(block: HouseBuilderContext ?=> Unit): HouseBuilder =
    val ctx = new HouseBuilderContext()
    block(using ctx)
    HouseBuilder(
      ctx.entityId,
      ctx.consumptionStrategy,
      ctx.otherEntities,
      ctx.storages
    )

  def id(id: String)(using ctx: HouseBuilderContext): Unit =
    ctx.entityId = Some(id)

  def consumptionStrategy(strategy: ConsumptionStrategy)(using
      ctx: HouseBuilderContext
  ): Unit =
    ctx.consumptionStrategy = Some(strategy)

  def contains[E <: GridEntity, S <: GridEntityState](
      builders: GridEntityBuilder[E, S]*
  )(using ctx: HouseBuilderContext): Unit =
    ctx.otherEntities = builders
      .map(_.asInstanceOf[GridEntityBuilder[GridEntity, GridEntityState]])
      .toList

  def energyStorageSystems[E <: Storage, S <: StorageState](
      storages: GridEntityBuilder[E, S]*
  )(using ctx: HouseBuilderContext): Unit =
    ctx.storages = storages
      .map(_.asInstanceOf[GridEntityBuilder[Storage, StorageState]])
      .toList
