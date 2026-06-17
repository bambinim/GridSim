package org.gridsim.core.behaviour.shaping

import org.gridsim.core.common.StochasticGenerator


/**
 * Defines the contract for shaping energy demand by applying noise or transformations.
 */
trait DemandShaper:
  /**
   * Transforms a mean value and its variance into a final shaped value.
   *
   * @param mean     The average or base value.
   * @param variance The statistical variance or range for the transformation.
   * @return The resulting shaped value.
   */
  def shape(mean: Double, variance: Double): Double

object DemandShaper:
  /** Default shaper using a Gaussian (Normal) distribution. */
  given default: DemandShaper = GaussianShaper(StochasticGenerator())
  
  