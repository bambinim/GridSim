package org.gridsim.core.common

import scala.util.Random

class StochasticGenerator(private val rng: Random):
  def nextGaussian(mean: Double, stdDev: Double): Double =
    mean + rng.nextGaussian() * stdDev

  def nextDouble(): Double =
    rng.nextDouble()
    
  def nextSeed(): Long =
    rng.nextLong()

object StochasticGenerator:
  
  def apply(): StochasticGenerator = new StochasticGenerator(new Random())

  def fromSeed(seed: Long): StochasticGenerator = new StochasticGenerator(new Random(seed))

  given StochasticGenerator = apply()

