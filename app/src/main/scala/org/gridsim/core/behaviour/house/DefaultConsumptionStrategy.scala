package org.gridsim.core.behaviour.house

import org.gridsim.core.common.Power

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
    0L  -> ConsumptionBand(Power(0.2), 0.05),
    1L  -> ConsumptionBand(Power(0.15), 0.05),
    2L  -> ConsumptionBand(Power(0.15), 0.02),
    3L  -> ConsumptionBand(Power(0.15), 0.02),
    4L  -> ConsumptionBand(Power(0.2), 0.03),
    5L  -> ConsumptionBand(Power(0.3), 0.05),
    6L  -> ConsumptionBand(Power(0.6), 0.1),
    7L  -> ConsumptionBand(Power(1.2), 0.2),
    8L  -> ConsumptionBand(Power(1.0), 0.15),
    9L  -> ConsumptionBand(Power(0.6), 0.1),
    10L -> ConsumptionBand(Power(0.5), 0.1),
    11L -> ConsumptionBand(Power(0.5), 0.1),
    12L -> ConsumptionBand(Power(0.8), 0.1),
    13L -> ConsumptionBand(Power(0.6), 0.1),
    14L -> ConsumptionBand(Power(0.5), 0.1),
    15L -> ConsumptionBand(Power(0.5), 0.1),
    16L -> ConsumptionBand(Power(0.6), 0.1),
    17L -> ConsumptionBand(Power(0.8), 0.15),
    18L -> ConsumptionBand(Power(2.8), 0.4),
    19L -> ConsumptionBand(Power(3.0), 0.5),
    20L -> ConsumptionBand(Power(2.5), 0.3),
    21L -> ConsumptionBand(Power(2.0), 0.2),
    22L -> ConsumptionBand(Power(1.2), 0.15),
    23L -> ConsumptionBand(Power(0.4), 0.1)))
