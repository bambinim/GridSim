package org.gridsim.core.model.battery

import org.gridsim.core.behaviour.battery.BatteryStrategy

/**
 * Represents the specific model or technology of a battery.
 * Used to dispatch the appropriate [[BatteryStrategy]].
 */
enum BatteryModel:
  /** Standard physical model for typical batteries. */
  case Standard
