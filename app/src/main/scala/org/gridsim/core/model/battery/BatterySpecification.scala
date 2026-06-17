package org.gridsim.core.model.battery

import cats.syntax.all.*
import org.gridsim.core.common.{Energy, Power}
import org.gridsim.core.model.StorageSpecification


case class BatterySpecification(capacity: Energy, maxPowerCharge: Power, maxPowerDischarge: Power, minSoC: Double = 0.1) extends StorageSpecification
