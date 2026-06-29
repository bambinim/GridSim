package org.gridsim.core.simulation

import org.gridsim.core.simulation.SimulationControllerState.{PAUSED, RUNNING}
import org.gridsim.core.simulation.scheduling.{ScheduledTask, Scheduler}

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.duration.FiniteDuration

final case class SimulationSnapshot(
 state: SimulationState,
 controllerState: SimulationControllerState
)

/**
 * Lifecycle state of a [[SimulationRunner]].
 */
enum SimulationControllerState:
  /** The runner is executing scheduled simulation ticks. */
  case RUNNING

  /** The runner keeps its current state but skips scheduled ticks. */
  case PAUSED

/**
 * Default scheduled implementation of [[SimulationRunner]].
 *
 * The runner keeps the current [[SimulationState]] in an atomic reference and
 * periodically advances it by invoking [[SimulationEngine.step]]. The engine
 * remains responsible only for the pure transition from one snapshot to the
 * next; this class owns scheduling and lifecycle control.
 *
 * @param engine pure simulation engine used to compute each tick
 * @param state initial simulation snapshot
 * @param scheduler the entity responsible to schedule the step task.
 * @param interval real time elapsed between scheduled simulation ticks
 */
final case class DefaultSimulationController(
  engine: SimulationEngine,
  state: SimulationState,
  scheduler: Scheduler,
  interval: FiniteDuration
) extends SimulationController:

  private val stateRef = AtomicReference[SimulationState](state)
  private val simulationControllerStateRef = AtomicReference[SimulationControllerState](PAUSED)
  private val activeTaskRef = AtomicReference[Option[ScheduledTask]](None)

  /**
   * Returns the latest simulation snapshot.
   *
   * @return the state currently stored by the runner
   */
  def currentState: SimulationState = stateRef.get()

  /**
   * Returns the current lifecycle state.
   *
   * @return [[SimulationRunnerState.RUNNING]] when ticks are active,
   *         [[SimulationRunnerState.PAUSED]] otherwise
   */
  def simulationControllerState: SimulationControllerState = simulationControllerStateRef.get()

  /**
   * Starts scheduling simulation ticks.
   *
   * A tick is scheduled immediately and then repeated every `tickInterval`.
   * The compare-and-set prevents a paused-to-running transition from being
   * applied more than once concurrently.
   */
  override def start(): Unit =
    if simulationControllerStateRef.compareAndSet(PAUSED, RUNNING) then
      val task = scheduler.schedule(
        () => {
          if simulationControllerStateRef.get() == RUNNING then
            stepOnce()
        },
        interval
      )
      activeTaskRef.set(Some(task))
      notifyListeners()

  /**
   * Pauses scheduled execution.
   *
   * Already scheduled tasks may still wake up, but they will not advance the
   * simulation while the runner state is [[SimulationRunnerState.PAUSED]].
   */
  override def pause(): Unit =
    if simulationControllerStateRef.get() == RUNNING then
      simulationControllerStateRef.set(PAUSED)
      cancelActiveTask()
      notifyListeners()

  /**
   * Stops the scheduler and releases its thread.
   *
   * After shutdown, the current scheduler cannot be started again.
   */
  override def stop(): Unit =
    simulationControllerStateRef.set(PAUSED)
    cancelActiveTask()
    scheduler.stop()
    notifyListeners()

  /**
   * Resumes periodic execution by delegating to [[start]].
   */
  override def resume(): Unit =
    start()

  private val listeners =
    new CopyOnWriteArrayList[SimulationSnapshot => Unit]()

  def addStateListener(listener: SimulationSnapshot => Unit): Unit =
    listeners.add(listener)

  private def notifyListeners(): Unit =
    val snapshot = SimulationSnapshot(
      currentState,
      simulationControllerState
    )

    val it = listeners.iterator()
    while it.hasNext do
      it.next()(snapshot)

  /**
   * Advances the current state by one engine tick.
   *
   * @return the updated simulation state
   */
  override def stepOnce(): SimulationState =
    val newState = stateRef.updateAndGet(current => engine.step(current))
    notifyListeners()
    newState

  private def cancelActiveTask(): Unit =
    activeTaskRef.getAndSet(None).foreach(_.cancel())
