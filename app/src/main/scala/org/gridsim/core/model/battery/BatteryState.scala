package org.gridsim.core.model.battery

import cats.syntax.all.*
import org.gridsim.core.common.Energy
import org.gridsim.core.model.StorageState

case class BatteryState(currentCharge: Energy) extends StorageState
