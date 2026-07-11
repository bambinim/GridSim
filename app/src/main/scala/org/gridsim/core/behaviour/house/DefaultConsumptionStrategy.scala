package org.gridsim.core.behaviour.house

import org.gridsim.core.common.Power
import org.gridsim.core.common.kw

/**
 * A implementation of [[ConsumptionStrategy]] based on a 24-hour profile map.
 *
 * This class provides a simple data-driven way to define consumption patterns
 * by mapping each hour to a specific power band.
 *
 * @param profile A mapping from hour (0-23) to its corresponding [[ConsumptionBand]].
 */
case class DefaultConsumptionStrategy(profile: Map[Long, ConsumptionBand]) extends ConsumptionStrategy:
  override def getBand(h: Long): ConsumptionBand =
    profile.getOrElse(h, ConsumptionBand(Power(0.5), 0.1))

object DefaultConsumptionStrategy:
  /** A standard residential consumption profile with morning and evening peaks. */
  val traditionalProfile = new DefaultConsumptionStrategy(Map(
    0L  -> ConsumptionBand(0.2.kw, 0.05),
    1L  -> ConsumptionBand(0.15.kw, 0.05),
    2L  -> ConsumptionBand(0.15.kw, 0.02),
    3L  -> ConsumptionBand(0.15.kw, 0.02),
    4L  -> ConsumptionBand(0.2.kw, 0.03),
    5L  -> ConsumptionBand(0.3.kw, 0.05),
    6L  -> ConsumptionBand(0.6.kw, 0.1),
    7L  -> ConsumptionBand(1.2.kw, 0.2),
    8L  -> ConsumptionBand(1.0.kw, 0.15),
    9L  -> ConsumptionBand(0.6.kw, 0.1),
    10L -> ConsumptionBand(0.5.kw, 0.1),
    11L -> ConsumptionBand(0.5.kw, 0.1),
    12L -> ConsumptionBand(0.8.kw, 0.1),
    13L -> ConsumptionBand(0.6.kw, 0.1),
    14L -> ConsumptionBand(0.5.kw, 0.1),
    15L -> ConsumptionBand(0.5.kw, 0.1),
    16L -> ConsumptionBand(0.6.kw, 0.1),
    17L -> ConsumptionBand(0.8.kw, 0.15),
    18L -> ConsumptionBand(2.8.kw, 0.4),
    19L -> ConsumptionBand(3.0.kw, 0.5),
    20L -> ConsumptionBand(2.5.kw, 0.3),
    21L -> ConsumptionBand(2.0.kw, 0.2),
    22L -> ConsumptionBand(1.2.kw, 0.15),
    23L -> ConsumptionBand(0.4.kw, 0.1)))

  val commercialProfile = new DefaultConsumptionStrategy(Map(
    0L -> ConsumptionBand(0.5.kw, 0.1),
    1L -> ConsumptionBand(0.5.kw, 0.1),
    2L -> ConsumptionBand(0.5.kw, 0.1),
    3L -> ConsumptionBand(0.5.kw, 0.1),
    4L -> ConsumptionBand(0.6.kw, 0.1),
    5L -> ConsumptionBand(1.0.kw, 0.2),
    6L -> ConsumptionBand(2.5.kw, 0.5),
    7L -> ConsumptionBand(8.0.kw, 1.5),
    8L -> ConsumptionBand(12.0.kw, 2.0),
    9L -> ConsumptionBand(15.0.kw, 2.5),
    10L -> ConsumptionBand(15.0.kw, 2.5),
    11L -> ConsumptionBand(15.0.kw, 2.5),
    12L -> ConsumptionBand(12.0.kw, 2.0),
    13L -> ConsumptionBand(14.0.kw, 2.5),
    14L -> ConsumptionBand(15.0.kw, 2.5),
    15L -> ConsumptionBand(15.0.kw, 2.5),
    16L -> ConsumptionBand(13.0.kw, 2.0),
    17L -> ConsumptionBand(10.0.kw, 1.5),
    18L -> ConsumptionBand(6.0.kw, 1.0),
    19L -> ConsumptionBand(3.0.kw, 0.5),
    20L -> ConsumptionBand(2.0.kw, 0.3),
    21L -> ConsumptionBand(1.5.kw, 0.2),
    22L -> ConsumptionBand(1.0.kw, 0.15),
    23L -> ConsumptionBand(0.6.kw, 0.1)
  ))

  val ecoProfile = new DefaultConsumptionStrategy(Map(
    0L -> ConsumptionBand(0.1.kw, 0.02),
    1L -> ConsumptionBand(0.08.kw, 0.02),
    2L -> ConsumptionBand(0.08.kw, 0.01),
    3L -> ConsumptionBand(0.08.kw, 0.01),
    4L -> ConsumptionBand(0.1.kw, 0.02),
    5L -> ConsumptionBand(0.15.kw, 0.02),
    6L -> ConsumptionBand(0.3.kw, 0.05),
    7L -> ConsumptionBand(0.6.kw, 0.1),
    8L -> ConsumptionBand(0.5.kw, 0.1),
    9L -> ConsumptionBand(0.3.kw, 0.05),
    10L -> ConsumptionBand(0.25.kw, 0.05),
    11L -> ConsumptionBand(0.25.kw, 0.05),
    12L -> ConsumptionBand(0.4.kw, 0.05),
    13L -> ConsumptionBand(0.3.kw, 0.05),
    14L -> ConsumptionBand(0.25.kw, 0.05),
    15L -> ConsumptionBand(0.25.kw, 0.05),
    16L -> ConsumptionBand(0.3.kw, 0.05),
    17L -> ConsumptionBand(0.4.kw, 0.08),
    18L -> ConsumptionBand(1.4.kw, 0.2),
    19L -> ConsumptionBand(1.5.kw, 0.25),
    20L -> ConsumptionBand(1.2.kw, 0.15),
    21L -> ConsumptionBand(1.0.kw, 0.1),
    22L -> ConsumptionBand(0.6.kw, 0.08),
    23L -> ConsumptionBand(0.2.kw, 0.05)
  ))
