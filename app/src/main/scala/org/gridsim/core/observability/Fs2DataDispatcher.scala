package org.gridsim.core.observability

import cats.effect.Concurrent
import cats.effect.syntax.all.*
import cats.syntax.all.*
import fs2.concurrent.Topic
import org.gridsim.core.simulation.SimulationState

import scala.concurrent.duration.FiniteDuration

/** An implementation of [[DataDispatcher]] backed by fs2 [[Topic]]s.
  *
  * It routes incoming [[SimulationState]] snapshots to specific FS2 topics
  * based on the type of data they carry, effectively decoupling the simulation
  * engine from the observers.
  *
  * @tparam F
  *   the effect type, must have a [[cats.effect.Concurrent]] instance in scope.
  * @param topics
  *   a map associating each [[SimulationData]] class type to its dedicated fs2
  *   [[Topic]].
  */
case class Fs2DataDispatcher[F[_]](
    topics: Map[Class[_ <: SimulationData], Topic[F, SimulationData]]
)(using Concurrent[F])
    extends DataDispatcher[F]:
  /** Dispatches the current simulation state to all active topics.
    *
    * This method leverages the `Sliceable` type class to dynamically extract
    * the relevant slices of data from the [[SimulationState]] and publishes
    * them to their respective topics. Observers subscribed to these topics will
    * receive the updates concurrently.
    *
    * @param state
    *   the complete simulation state at the current tick.
    * @throws RuntimeException
    *   if a registered topic's data type cannot be sliced from the state
    *   (unhandled type).
    * @return
    *   an effect `F[Unit]` representing the successful publication of all data
    *   slices.
    */
  override def dispatch(state: SimulationState, delta: FiniteDuration): F[Unit] =
    topics.toList.traverse_ { case (cls, topic) =>
      val tag = scala.reflect.ClassTag[SimulationData](
        cls.asInstanceOf[Class[SimulationData]]
      )
      val data = state.slice[SimulationData](delta)(using tag)
      topic.publish1(data).void
    }

object Fs2DataDispatcher:
  /** Creates an FS2-backed dispatcher and wires all provided observers to it.
    *
    * This smart constructor initializes the necessary fs2 [[Topic]]s for each
    * [[SimulationData]] type and spawns background fibers for each [[Observer]]
    * to continuously listen to their requested topics.
    *
    * @tparam F
    *   the effect type, must have a [[cats.effect.Concurrent]] instance to
    *   support topics and fibers.
    * @param observers
    *   the list of observers that want to subscribe to simulation events.
    * @return
    *   an effect `F` evaluating to the fully initialized [[Fs2DataDispatcher]].
    */
  def apply[F[_]: Concurrent](
      observers: List[Observer[F]]
  ): F[DataDispatcher[F]] = for {
    envTopic <- Topic[F, SimulationData]
    entityTopic <- Topic[F, SimulationData]
    entityFlowsTopic <- Topic[F, SimulationData]
    cableTopic <- Topic[F, SimulationData]
    snapshotTopic <- Topic[F, SimulationData]

    topics = Map[Class[_ <: SimulationData], Topic[F, SimulationData]](
      classOf[SimulationData.EnvironmentData] -> envTopic,
      classOf[SimulationData.EntityStatesData] -> entityTopic,
      classOf[SimulationData.EntityFlowsData] -> entityFlowsTopic,
      classOf[SimulationData.CableLoadsData] -> cableTopic,
      classOf[SimulationData.SimulationSnapshot] -> snapshotTopic
    )

    // Wire up each observer to its dedicated topic
    _ <- observers.traverse_ { obs =>
      topics.get(obs.dataType) match
        case Some(topic) =>
          topic
            .subscribe(100)
            .evalMap(obs.onUpdate)
            .compile
            .drain
            .start
            .void
        case None => Concurrent[F].unit
    }
  } yield Fs2DataDispatcher(topics)
