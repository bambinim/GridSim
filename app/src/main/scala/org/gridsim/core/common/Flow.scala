package org.gridsim.core.common

import cats.Show
import cats.syntax.all.toShow
import org.gridsim.core.common.Energy.toFlow

/** Represents a flow of energy (kWh). */
enum Flow[+A]:
  case Surplus(amount: A)
  case Deficit(amount: A)
  case Balanced

object Flow:
  val balanced: Flow[Energy] = Flow.Balanced

  def surplus(amount: Energy): Flow[Energy] = Flow.Surplus(amount.abs)

  def deficit(amount: Energy): Flow[Energy] = Flow.Deficit(amount.abs)

  given [A: Show]: Show[Flow[A]] = Show.show:
    case Flow.Surplus(amount) => s"Surplus(${amount.show})"
    case Flow.Deficit(amount) => s"Deficit(${amount.show})"
    case Flow.Balanced => "Balanced"

  extension (f: Flow[Energy])
    def value: Double = f match
      case Flow.Surplus(e) => e.toDouble
      case Flow.Deficit(e) => -e.toDouble
      case Flow.Balanced   => 0.0

    def +(o: Flow[Energy]): Flow[Energy] =
      (f.value + o.value).kwh.toFlow
