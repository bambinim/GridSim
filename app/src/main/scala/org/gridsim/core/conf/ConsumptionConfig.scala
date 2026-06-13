package org.gridsim.core.conf

import org.gridsim.core.common.Units.{Power, Energy}
import pureconfig._

/**
 * Configuration module to load the consume strategies parameter.
 */

/**
 * Configuration reader to map the Double values inside the .conf file in
 * the right type(Power or Energy)
 */
given ConfigReader[Power] = ConfigReader.doubleConfigReader.map(d => Power(d))
given ConfigReader[Energy] = ConfigReader.doubleConfigReader.map(d => Energy(d))

case class TraditionalConfig(
  morningPeakStart: Int, morningPeakEnd: Int,
  eveningPeakStart: Int, eveningPeakEnd: Int,
  peakPowerKw: Power, basePowerKw: Power
) derives ConfigReader

case class SmartWorkerConfig(
  workStart: Int, workEnd: Int,
  workPowerKw: Power, basePowerKw: Power
) derives ConfigReader

case class VacantConfig(basePowerKw: Power) derives ConfigReader
