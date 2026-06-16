package org.gridsim.core.model.battery

import cats.syntax.all.*
import org.gridsim.core.common.{Energy, Power}


case class BatterySpecification(capacity: Energy, maxPowerCharge: Power, maxPowerDischarge: Power, minSoC: Double = 0.1)
