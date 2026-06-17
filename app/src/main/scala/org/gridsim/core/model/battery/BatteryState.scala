package org.gridsim.core.model.battery

import cats.syntax.all.*
import org.gridsim.core.common.Energy
import org.gridsim.core.model.GridState
import org.gridsim.core.model.storage.StorageState

case class BatteryState(entityId: String, currentCharge: Energy) extends StorageState
