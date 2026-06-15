package org.gridsim.core.behaviour.shaping

/**
 * A [[DemandShaper]] that returns the mean value unchanged.
 *
 * Useful for deterministic testing or when no stochasticity is required.
 */
case class IdentityShaper() extends DemandShaper:
  override def shape(mean: Double, variance: Double): Double = mean