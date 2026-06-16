package org.gridsim.core.common

import scala.annotation.targetName

import org.gridsim.core.common.Energy.toFlow

enum Flow[+A]:
  case Surplus(amount: A)
  case Deficit(amount: A)
  case Balanced

object Flow:

  def surplus(amount: Energy): Flow[Energy] = Flow.Surplus(amount)

  def deficit(amount: Energy): Flow[Energy] = Flow.Deficit(amount)

  val balanced: Flow[Energy] = Flow.Balanced
  extension (f: Flow[Energy])
    def value: Double = f match
      case Flow.Surplus(e) => e.toDouble
      case Flow.Deficit(e) => -e.toDouble
      case Flow.Balanced   => 0.0

    @targetName("combineFlows")
    def +(o: Flow[Energy]): Flow[Energy] =
      (f.value + o.value).kwh.toFlow
