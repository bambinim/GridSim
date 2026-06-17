package org.gridsim.core.behaviour.shaping

import org.gridsim.core.common.StochasticGenerator
import org.gridsim.core.common.StochasticGenerator.given


/**
 * A [[DemandShaper]] that applies Gaussian (Normal) noise.
 *
 * It uses a [[StochasticGenerator]] to sample from a distribution
 * defined by N(mean, variance).
 *
 * @param gen The generator providing the underlying entropy.
 */
case class GaussianShaper(gen: StochasticGenerator) extends DemandShaper:
  override def shape(mean: Double, variance: Double): Double =
    mean + gen.nextGaussian(0.0, variance)