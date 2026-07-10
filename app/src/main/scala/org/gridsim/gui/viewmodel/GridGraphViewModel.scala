package org.gridsim.gui.viewmodel

import org.gridsim.core.model.network.GridGraph
import scalafx.beans.property.ObjectProperty
import org.gridsim.gui.model.Selection
import org.gridsim.core.common.Flow
import org.gridsim.core.common.{Power, Energy}
import com.brunomnsilva.smartgraph.graph.Graph
import org.gridsim.core.model.GridEntity
import com.brunomnsilva.smartgraph.graph.Vertex
import com.brunomnsilva.smartgraph.graph.Edge
import java.{util => ju}
import com.brunomnsilva.smartgraph.graph.GraphEdgeList
import org.gridsim.core.model.network.{CableConnections, Cable}
import scala.concurrent.duration.FiniteDuration

class GridGraphViewModel(
    tickDelta: FiniteDuration,
    graph: GridGraph,
    selection: ObjectProperty[Selection]
):
  private var cableLoads: Map[Cable, Energy] = Map.empty
  private var entityFlows: Map[String, Flow[Energy]] = Map.empty
  private[gui] val uiGraph: Graph[String, String] = graph

  def nodeClicked(entityId: String): Unit =
    graph.nodes.find(_.id == entityId).foreach { ent =>
      selection.value = Selection.SelectedNode(ent)
    }

  def edgeClicked(from: String, to: String): Unit =
    selection.value = Selection.SelectedCable(CableConnections(from, to))

  def entityFlow(id: String): Option[Flow[Energy]] = entityFlows.get(id)

  def isExternalGrid(id: String): Boolean =
    import org.gridsim.core.model.network.ExternalGrid
    graph.nodes.find(_.id == id).exists(_.isInstanceOf[ExternalGrid])

  var onUpdate: () => Unit = () => ()

  def update(
      entityFlows: Map[String, Flow[Energy]],
      cableLoads: Map[Cable, Energy]
  ): Unit =
    this.entityFlows = entityFlows
    this.cableLoads = cableLoads
    onUpdate()

  def overloadedConnections(): Iterable[CableConnections] =
    cableLoads.filter(_.isOverloaded(tickDelta)).map(_._1.connections)

extension (g: GridGraph)
  def deduplicatedConnections: Iterable[CableConnections] =
    g.cables.map(_.connections).toSet

extension (ce: (Cable, Energy))
  def isOverloaded(timeDelta: FiniteDuration): Boolean =
    ce._1.maxCapacity.toDouble <= ce._2.instantPower(timeDelta).toDouble.abs

given Conversion[GridGraph, Graph[String, String]] with
  override def apply(gridGraph: GridGraph): Graph[String, String] =
    val graph = new GraphEdgeList[String, String]()
    val vertices = gridGraph.nodes
      .map(entity => (entity.id, graph.insertVertex(entity.id)))
      .toMap
    for (connections <- gridGraph.deduplicatedConnections) {
      val source = vertices(connections.n1)
      val target = vertices(connections.n2)
      graph.insertEdge(source, target, connections.hashCode().toHexString)
    }
    graph
