package org.gridsim.dsl.simulation

import org.gridsim.dsl.Builder
import org.gridsim.core.simulation.{SimulationModel, SimulationState}
import org.gridsim.dsl.DSLError
import cats.data.ValidatedNec
import org.gridsim.dsl.GridEntityBuilder
import org.gridsim.core.model.network.Cable
import org.gridsim.core.model.{GridEntity, GridEntityState}
import org.gridsim.dsl.grid.TopologyBuilderContext
import org.gridsim.dsl.grid.entities.{HouseBuilder, SolarArrayBuilder}

import cats.syntax.all.*
import org.gridsim.core.model.network.{ExternalGrid, GridGraph}
import org.gridsim.core.model.Environment
import scala.concurrent.duration.*
import org.gridsim.dsl.DSLBuilderError

case class SimulationBuilder(
    entitiesBuilders: List[GridEntityBuilder[GridEntity, GridEntityState]],
    topologyBlock: Option[
      (TopologyBuilderContext, List[GridEntity]) ?=> Unit
    ],
    tickDelta: Option[FiniteDuration]
) extends Builder[(SimulationModel, SimulationState)]:

  override def build()
      : ValidatedNec[DSLError, (SimulationModel, SimulationState)] =
    val entities = entitiesBuilders.traverse(_.build())
    val topBlk =
      topologyBlock.toValidNec(DSLBuilderError.MissingBlock("topology"))
    val delta = tickDelta.toValidNec(DSLBuilderError.MissingField("tick"))
    (entities, topBlk, delta).tupled.andThen { case (ent, tb, dt) =>
      val topCtx = new TopologyBuilderContext()
      tb(using topCtx, ent.map(_._1))
      val model = SimulationModel(
        GridGraph(
          ent.map(_._1) ++ List(ExternalGrid(SimulationBuilder.EG)),
          topCtx.cables
        )
      )
      val state = SimulationState(
        Environment(0.seconds),
        ent.map(e => e._1.id -> e._2).toMap,
        delta = dt
      )
      (model, state).validNec
    }

private[dsl] class SimulationBuilderContext:
  private[dsl] var entitiesBuilders: List[
    GridEntityBuilder[GridEntity, GridEntityState]
  ] = List.empty
  private[dsl] var topologyBlock
      : Option[(TopologyBuilderContext, List[GridEntity]) ?=> Unit] =
    None
  private[dsl] var tickDelta: Option[FiniteDuration] = None

private[dsl] class EntityBuilderContext:
  private[dsl] var builders: List[
    GridEntityBuilder[GridEntity, GridEntityState]
  ] = List.empty

object SimulationBuilder:

  infix def simulation(
      block: SimulationBuilderContext ?=> Unit
  ): SimulationBuilder =
    val ctx = new SimulationBuilderContext()
    block(using ctx)
    SimulationBuilder(ctx.entitiesBuilders, ctx.topologyBlock, ctx.tickDelta)

  infix def tick(delta: FiniteDuration)(using
      ctx: SimulationBuilderContext
  ): Unit = ctx.tickDelta = Some(delta)

  infix def entities(
      block: EntityBuilderContext ?=> Unit
  )(using simCtx: SimulationBuilderContext): Unit =
    val ctx = new EntityBuilderContext()
    block(using ctx)
    simCtx.entitiesBuilders = ctx.builders

  infix def house(block: HouseBuilder.HouseBuilderContext ?=> Unit)(using
      ctx: EntityBuilderContext
  ): Unit =
    val house = HouseBuilder.house(block)
    ctx.builders = ctx.builders ++ List(house)

  infix def solarPowerPlant(builder: SolarArrayBuilder)(using
      ctx: EntityBuilderContext
  ): Unit =
    ctx.builders = ctx.builders ++ List(builder)

  infix def topology(
      block: (TopologyBuilderContext, List[GridEntity]) ?=> Unit
  )(using ctx: SimulationBuilderContext): Unit =
    ctx.topologyBlock = Some(block)

  infix def E(index: Integer)(using entitiesList: List[GridEntity]): String =
    entitiesList(index).id

  infix def EG: String = "__external__grid__"
