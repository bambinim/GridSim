package org.gridsim.gui.model

import scala.concurrent.duration.{DurationInt, FiniteDuration}

/**
 * A unit an amount of simulation tick duration can be expressed in:
 * seconds/minutes/hours/days map to an exact [[FiniteDuration]].
 * Months and years have no fixed length in reality (a month is 28-31 days),
 * so they're offered as documented approximations (30 and 365 days respectively),
 * acceptable for a tick delta, which is a single constant applied every
 * step of the whole simulation.
 */
enum TickDurationUnit(val label: String, val toDuration: Int => FiniteDuration):
  case Seconds extends TickDurationUnit("Seconds", n => n.seconds)
  case Minutes extends TickDurationUnit("Minutes", n => n.minutes)
  case Hours extends TickDurationUnit("Hours", n => n.hours)
  case Days extends TickDurationUnit("Days", n => n.days)
//  case Months extends TickDurationUnit("Months (30 days)", n => (n * 30).days)
//  case Years extends TickDurationUnit("Years (365 days)", n => (n * 365).days)
