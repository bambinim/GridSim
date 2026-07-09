package org.gridsim.gui.viewmodel

import org.gridsim.core.model.network.GridGraph
import scalafx.beans.property.ObjectProperty
import org.gridsim.gui.model.Selection
import org.gridsim.core.common.Flow
import org.gridsim.core.common.Power
import com.brunomnsilva.smartgraph.graph.Graph
import org.gridsim.core.model.GridEntity
import com.brunomnsilva.smartgraph.graph.Vertex
import com.brunomnsilva.smartgraph.graph.Edge
import java.{util => ju}
import com.brunomnsilva.smartgraph.graph.GraphEdgeList
import org.gridsim.core.model.network.CableConnections

class GridGraphViewModel(
    graph: GridGraph,
    selection: ObjectProperty[Selection]
):
  private var cableFlows: Map[String, Flow[Power]] = Map.empty
  private var entityFlows: Map[String, Flow[Power]] = Map.empty
  private[gui] val uiGraph: Graph[String, String] = graph

  def nodeClicked(entityId: String): Unit =
    for {
      ent <- graph.nodes.find(_.id == entityId)
    } yield selection.setValue(Selection.SelectedNode(ent))

  def edgeClicked(from: String, to: String): Unit =
    () // TODO: implement cable selection

  def update(
      entityFlows: Map[String, Flow[Power]],
      cableFlows: Map[String, Flow[Power]]
  ): Unit =
    this.entityFlows = entityFlows
    this.cableFlows = cableFlows

extension (g: GridGraph)
  def deduplicatedConnections: Iterable[CableConnections] =
    g.cables.map(_.connections).toSet

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
