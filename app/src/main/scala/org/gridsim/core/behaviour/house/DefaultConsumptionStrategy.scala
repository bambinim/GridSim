package org.gridsim.core.behaviour.house

import org.gridsim.core.common.Units.Power


/**
 * A implementation of [[ConsumptionStrategy]] based on a 24-hour profile map.
 *
 * This class provides a simple data-driven way to define consumption patterns
 * by mapping each hour to a specific power band.
 *
 * @param profile A mapping from hour (0-23) to its corresponding [[ConsumptionBand]].
 */
case class DefaultConsumptionStrategy(profile: Map[Int, ConsumptionBand]) extends ConsumptionStrategy:
  override def getBand(h: Int): ConsumptionBand =
    profile.getOrElse(h, ConsumptionBand(Power(0.5), 0.1))

object DefaultConsumptionStrategy:
  /** A standard residential consumption profile with morning and evening peaks. */
  val traditionalProfile = new DefaultConsumptionStrategy(Map(
    0  -> ConsumptionBand(Power(0.2), 0.05),
    1  -> ConsumptionBand(Power(0.15), 0.05),
    2  -> ConsumptionBand(Power(0.15), 0.02),
    3  -> ConsumptionBand(Power(0.15), 0.02),
    4  -> ConsumptionBand(Power(0.2), 0.03),
    5  -> ConsumptionBand(Power(0.3), 0.05),
    6  -> ConsumptionBand(Power(0.6), 0.1),
    7  -> ConsumptionBand(Power(1.2), 0.2),
    8  -> ConsumptionBand(Power(1.0), 0.15),
    9  -> ConsumptionBand(Power(0.6), 0.1),
    10 -> ConsumptionBand(Power(0.5), 0.1),
    11 -> ConsumptionBand(Power(0.5), 0.1),
    12 -> ConsumptionBand(Power(0.8), 0.1),
    13 -> ConsumptionBand(Power(0.6), 0.1),
    14 -> ConsumptionBand(Power(0.5), 0.1),
    15 -> ConsumptionBand(Power(0.5), 0.1),
    16 -> ConsumptionBand(Power(0.6), 0.1),
    17 -> ConsumptionBand(Power(0.8), 0.15),
    18 -> ConsumptionBand(Power(2.8), 0.4),
    19 -> ConsumptionBand(Power(3.0), 0.5),
    20 -> ConsumptionBand(Power(2.5), 0.3),
    21 -> ConsumptionBand(Power(2.0), 0.2),
    22 -> ConsumptionBand(Power(1.2), 0.15),
    23 -> ConsumptionBand(Power(0.4), 0.1)))
