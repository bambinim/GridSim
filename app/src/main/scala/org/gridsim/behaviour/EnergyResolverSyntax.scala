package org.gridsim.behaviour

import org.gridsim.common.Units.Energy
import org.gridsim.model.Environment

object EnergyResolverSyntax:

  extension [A](node: A)(using resolver: EnergyResolver[A])
    def solve(env: Environment): Energy =
      resolver.solve(node, env)

