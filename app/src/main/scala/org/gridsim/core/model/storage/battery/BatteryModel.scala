package org.gridsim.core.model.storage.battery

import org.gridsim.core.behaviour.storage.battery.BatteryStrategy

/**
 * Represents the specific model or technology of a battery.
 * Used to dispatch the appropriate [[BatteryStrategy]].
 */
enum BatteryModel:
  /** Standard physical model for typical batteries. */
  case Standard
