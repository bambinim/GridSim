package org.gridsim.core.behaviour.storage.battery

import org.gridsim.core.behaviour.storage.StorageStrategy
import org.gridsim.core.common.*
import org.gridsim.core.common.Flow.*
import org.gridsim.core.model.storage.battery.{Battery, BatteryModel, BatteryState}

import scala.concurrent.duration.FiniteDuration

/**
 * Strategy pattern for battery energy processing.
 *
 * Implementations define how a battery state evolves when subjected to
 * an energy surplus (charging) or deficit (discharging).
 */
trait BatteryStrategy extends StorageStrategy[BatteryState, Battery]:
  extension (state: BatteryState)
    def charge(offered: Energy, b: Battery)(using delta: FiniteDuration): (BatteryState, Flow[Energy])
    
    def discharge(needed: Energy, b: Battery)(using delta: FiniteDuration): (BatteryState, Flow[Energy])

object BatteryStrategy:
  /**
   * Factory method to obtain the strategy associated with a specific [[BatteryModel]].
   */
  def forModel(model: BatteryModel): BatteryStrategy = model match
    case BatteryModel.Standard => StandardBatteryStrategy
    case null                     => StandardBatteryStrategy


