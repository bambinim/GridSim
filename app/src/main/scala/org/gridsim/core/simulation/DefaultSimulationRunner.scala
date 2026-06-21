package org.gridsim.core.simulation

import org.gridsim.core.simulation.SimulationRunnerState.{PAUSED, RUNNING}

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import scala.concurrent.duration.FiniteDuration

/**
 * Lifecycle state of a [[SimulationRunner]].
 */
enum SimulationRunnerState:
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
 * @param tickInterval real time elapsed between scheduled simulation ticks
 */
final case class DefaultSimulationRunner(engine: SimulationEngine, state: SimulationState, tickInterval: FiniteDuration) extends SimulationRunner:
  
  private val stateRef = AtomicReference[SimulationState](state)
  private val simulationRunnerStateRef = AtomicReference[SimulationRunnerState](PAUSED)

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
  def simulationRunnerState: SimulationRunnerState = simulationRunnerStateRef.get()

  private val scheduler: ScheduledExecutorService =
    Executors.newSingleThreadScheduledExecutor()

  /**
   * Starts scheduling simulation ticks.
   *
   * A tick is scheduled immediately and then repeated every `tickInterval`.
   * The compare-and-set prevents a paused-to-running transition from being
   * applied more than once concurrently.
   */
  override def start(): Unit =
    if simulationRunnerStateRef.compareAndSet(PAUSED, RUNNING) then
      val task = new Runnable {
        override def run(): Unit = {
          if simulationRunnerStateRef.get() == RUNNING then
            stepOnce()
        }
      }
      scheduler.scheduleAtFixedRate(
        task,
        0L,
        tickInterval.toMillis,
        TimeUnit.MILLISECONDS
      )

  /**
   * Pauses scheduled execution.
   *
   * Already scheduled tasks may still wake up, but they will not advance the
   * simulation while the runner state is [[SimulationRunnerState.PAUSED]].
   */
  override def pause(): Unit =
    if simulationRunnerStateRef.get() == RUNNING then
      simulationRunnerStateRef.set(PAUSED)

  /**
   * Stops the scheduler and releases its thread.
   *
   * After shutdown, the current scheduler cannot be started again.
   */
  override def stop(): Unit = 
    scheduler.shutdown()

  /**
   * Resumes periodic execution by delegating to [[start]].
   */
  override def resume(): Unit =
    start()

  /**
   * Advances the current state by one engine tick.
   *
   * @return the updated simulation state
   */
  override def stepOnce(): SimulationState =
    stateRef.updateAndGet(current => engine.step(current))
