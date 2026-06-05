package org.gridsim.core.model.battery

import cats.syntax.all.*
import org.gridsim.core.common.Units.Energy

case class BatteryState(currentCharge: Energy)
