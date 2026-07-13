package org.gridsim.core.observability

import org.gridsim.core.model.Environment
import org.gridsim.core.model.GridEntityState
import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.model.network.Cable
import org.gridsim.core.simulation.SimulationState
import scala.reflect.ClassTag

import scala.concurrent.duration.FiniteDuration

/** Algebraic Data Type representing discrete slices of simulation data. These
  * are emitted at the end of every simulation tick to be consumed by observers.
  */
enum SimulationData:
  /** Contains the updated simulation environment (e.g., time, external
    * conditions)
    */
  case EnvironmentData(environment: Environment)

  /** Contains the updated dynamic states of all grid entities */
  case EntityStatesData(states: Map[String, GridEntityState])

  case EntityFlowsData(flows: Map[String, Flow[Energy]])

  /** Contains the energy loads calculated for cables during the last tick */
  case CableLoadsData(loads: Map[Cable, Energy])

  /** A synchronized snapshot containing all simulation data for the current
    * tick. Useful for observers that need to process multiple data types at the
    * exact same time.
    */
  case SimulationSnapshot(
      environment: Environment,
      entityStates: Map[String, GridEntityState],
      entityFlows: Map[String, Flow[Energy]],
      cableLoads: Map[Cable, Energy],
      delta: FiniteDuration
  )

/** A type class providing the ability to slice a complex state object into
  * specific subsets of data.
  *
  * @tparam A
  *   the type of the state object that can be sliced.
  */
trait Sliceable[A]:
  /** Extracts a specific slice of data of type `T` from the state object.
    *
    * @tparam T
    *   the subtype of [[SimulationData]] to extract.
    * @param delta duration of each time step
    * @param tag
    *   the implicit class tag used to resolve the requested slice type at
    *   runtime.
    * @return
    *   the extracted data slice of type `T`.
    */
  extension (a: A) def slice[T <: SimulationData](delta: FiniteDuration)(using tag: ClassTag[T]): T

/** Provides the capability to slice a [[SimulationState]] into discrete
  * [[SimulationData]] events based on the requested type tag.
  */
given sliceableSimulationState: Sliceable[SimulationState] with
  extension (s: SimulationState)
    def slice[T <: SimulationData](delta: FiniteDuration)(using tag: ClassTag[T]): T =
      tag.runtimeClass match
        case c if c == classOf[SimulationData.EnvironmentData] =>
          SimulationData.EnvironmentData(s.environment).asInstanceOf[T]
        case c if c == classOf[SimulationData.EntityStatesData] =>
          SimulationData.EntityStatesData(s.entityStates).asInstanceOf[T]
        case c if c == classOf[SimulationData.EntityFlowsData] =>
          SimulationData.EntityFlowsData(s.entityFlows).asInstanceOf[T]
        case c if c == classOf[SimulationData.CableLoadsData] =>
          SimulationData.CableLoadsData(s.cableLoads).asInstanceOf[T]
        case c if c == classOf[SimulationData.SimulationSnapshot] =>
          SimulationData
            .SimulationSnapshot(s.environment, s.entityStates, s.entityFlows, s.cableLoads, delta)
            .asInstanceOf[T]
        case _ =>
          throw new RuntimeException(
            s"Unhandled SimulationData type: ${tag.runtimeClass.getName}"
          )
