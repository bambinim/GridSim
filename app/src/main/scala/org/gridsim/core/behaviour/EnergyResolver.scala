package org.gridsim.core.behaviour

import cats.data.State
import org.gridsim.core.common.Units.*
import org.gridsim.core.common.Units.Flow.Balanced
import org.gridsim.core.model.*
import org.gridsim.core.model.battery.Battery
import org.gridsim.core.behaviour.battery.BatteryLogic.given

import scala.concurrent.duration.FiniteDuration

/**
 * Defines the contract for resolving energy flows across domain entities.
 */
trait EnergyResolver[T]:
  def resolve(orchestrator: T, env: Environment)(using delta: FiniteDuration): (T, Flow[Energy])

object EnergyResolver:
  extension [A](node: A)(using resolver: EnergyResolver[A])
    def resolve(env: Environment)(using delta: FiniteDuration): (A, Flow[Energy]) =
      resolver.resolve(node, env)

