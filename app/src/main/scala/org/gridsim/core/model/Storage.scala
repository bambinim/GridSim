package org.gridsim.core.model

import org.gridsim.core.common.Energy
import cats.syntax.order.*

/** Base traits for all storage systems. */
trait StorageSpecification:
  def capacity: Energy

trait StorageState:
  def currentCharge: Energy
/**
 * Represents a generic abstraction for any energy storage device
 * (e.g., chemical batteries, thermal buffers, hydrogen tanks).
 *
 * It extends [[GridEntity]] to allow seamless integration as an active node or
 * component within the simulation environment. Concrete implementations are expected
 * to encapsulate their specific physical constraints while exposing a unified
 * energy-based view to the grid.
 */
trait Storage extends GridEntity:
  def spec: StorageSpecification
  def state: StorageState
  /**
   * Returns the amount of energy currently stored inside the device.
   *
   * @return An [[Energy]] value representing the current state of charge.
   */
  def currentCharge: Energy = state.currentCharge

  /**
   * Returns the nominal maximum energy capacity supported by the device.
   * This defines the upper physical limit for the storage medium.
   *
   * @return An [[Energy]] value representing the maximum storage boundary.
   */
  def maxCapacity: Energy = spec.capacity

  /**
   * Calculates the current fill level of the storage device.
   *
   * @return A [[Double]] value ranging from `0.0` (empty) to `100.0` (fully charged).
   */
  def percentage: Double =
    if maxCapacity > Energy.Zero then (currentCharge / maxCapacity) else 0.0
