package org.gridsim.core.simulation

trait SimulationEngine:
    def step(state: SimulationState): SimulationState
