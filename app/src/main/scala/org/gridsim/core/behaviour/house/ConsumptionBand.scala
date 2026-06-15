package org.gridsim.core.behaviour.house

import org.gridsim.core.common.Units.Power

/**
 * Represents a statistical consumption profile for a specific time window.
 *
 * It defines the expected power demand and its variability (standard deviation), 
 * allowing the simulation to generate stochastic energy flows using models 
 * like Gaussian distributions.
 *
 * @param meanPower The average power demand expected in this band.
 * @param variance  The standard deviation (in kW) for the stochastic generation.
 */
case class ConsumptionBand(meanPower: Power, variance: Double)
