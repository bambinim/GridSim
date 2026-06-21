package org.gridsim.core.model.storage.battery

/**
 * Represents the specific model or technology of a battery.
 * Used to dispatch the appropriate [[BatteryStrategy]].
 */
enum BatteryModel:
  /** Standard physical model for typical batteries. */
  case Standard
