package org.gridsim.dsl.grid

import org.gridsim.core.common.{Energy, Power}
import org.gridsim.core.model.GridEntity
import org.gridsim.core.model.network.{CableConnections, Cable}

private[dsl] class TopologyBuilderContext:
  var cables: List[Cable] = List.empty

private[dsl] case class RightConnectedCable(
    maxCapacity: Power,
    rightEntity: String
)

import scala.annotation.targetName

object Topology:

  extension (p: Power)
    infix def -->(rightEntity: String): RightConnectedCable =
      RightConnectedCable(p, rightEntity)

    infix def -->(rightEntity: GridEntity): RightConnectedCable =
      RightConnectedCable(p, rightEntity.id)

  trait Connectable[T]:
    def idOf(t: T): String

  object Connectable:
    given Connectable[String] with
      def idOf(s: String): String = s
    given [E <: GridEntity]: Connectable[E] with
      def idOf(e: E): String = e.id

  extension [T: Connectable](left: T)
    infix def <--(rightCable: RightConnectedCable)(using
        ctx: Option[TopologyBuilderContext] = None
    ): Cable =
      val cable = Cable(
        CableConnections(
          summon[Connectable[T]].idOf(left),
          rightCable.rightEntity
        ),
        rightCable.maxCapacity
      )
      ctx.map(c => c.cables = c.cables :+ cable)
      cable
