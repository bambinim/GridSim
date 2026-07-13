package org.gridsim.core.observability

import org.gridsim.core.model.Environment
import org.gridsim.core.model.GridEntityState
import org.gridsim.core.common.Energy
import org.gridsim.core.model.network.Cable
import org.gridsim.core.simulation.SimulationState
import cats.Monad
import cats.syntax.all.*

import scala.concurrent.duration.FiniteDuration

/** Represents a subscriber that listens to a specific type of simulation data.
  *
  * @tparam F
  *   the effect type in which the observer's callback will execute.
  * @param dataType
  *   the specific class of [[SimulationData]] this observer is interested in.
  * @param onUpdate
  *   the callback function that processes the received simulation data.
  */
case class Observer[F[_]](
    dataType: Class[_ <: SimulationData],
    onUpdate: SimulationData => F[Unit]
)

object Observer:

  /** Creates a type-safe [[Observer]] for a specific subtype of
    * [[SimulationData]].
    *
    * This smart constructor infers the `dataType` automatically using a
    * `ClassTag` and casts the received data so the callback can operate on the
    * specific type `T`.
    *
    * @tparam F
    *   the effect type for the callback.
    * @tparam T
    *   the specific [[SimulationData]] subtype to observe.
    * @param onUpdate
    *   a function that handles updates of type `T`.
    * @param tag
    *   the implicit class tag used to capture the runtime class of `T`.
    * @return
    *   a new [[Observer]] instance configured for the specified type.
    */
  def apply[F[_], T <: SimulationData](
      onUpdate: T => F[Unit]
  )(using tag: scala.reflect.ClassTag[T]): Observer[F] =
    Observer(
      tag.runtimeClass.asInstanceOf[Class[_ <: SimulationData]],
      data => onUpdate(data.asInstanceOf[T])
    )

/** A component responsible for distributing the current [[SimulationState]] to
  * all registered observers.
  *
  * @tparam F
  *   the effect type in which the dispatch operations occur.
  */
trait DataDispatcher[F[_]]:

  /** Extracts the necessary data from the simulation state and publishes it to
    * the appropriate topics or observers.
    *
    * @param state
    *   the current overall state of the simulation at the end of a tick.
    * @param delta
    *   the current time duration of each simulation step.
    * @return
    *   an effect `F[Unit]` representing the completion of the dispatch process.
    */
  def dispatch(state: SimulationState, delta: FiniteDuration): F[Unit]
