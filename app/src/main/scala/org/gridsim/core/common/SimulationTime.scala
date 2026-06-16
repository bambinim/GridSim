package org.gridsim.core.common

/** Represents a time within the simulation. */
trait SimulationTime:

  /** year simulation year (0-based) */
  def year: Int
  /** day of the year [0, 364] */
  def day: Int
  /** hour of the day [0, 23] */
  def hour: Int
  /** minute of the hour [0, 59] */
  def minute: Int
  /** second of the minute [0, 59] */
  def second: Int

  def totalMinutes: Long
  def totalSeconds: Long

  def plusMinutes(n: Int): SimulationTime
  def plusSeconds(n: Int): SimulationTime

private final case class SimulationTimeImpl(year: Int, day: Int, hour: Int, minute: Int, second: Int) extends SimulationTime:
  require(day >= 0 && day <= 364, s"day out of range: $day")
  require(hour >= 0 && hour <= 23,  s"hour out of range: $hour")
  require(minute >= 0 && minute <= 59,  s"minute out of range: $minute")
  require(second >= 0 && second <= 59, s"second out of range: $second")

  /** Total number of minutes elapsed since the start of the simulation. */
  override def totalMinutes: Long =
    year.toLong * 365 * 24 * 60 +
    day.toLong * 24 * 60 +
    hour.toLong * 60 +
    minute.toLong

  override def totalSeconds: Long = totalMinutes * 60 + second.toLong

  def plusMinutes(n: Int): SimulationTime =
    SimulationTime.fromSeconds(totalSeconds + n * 60)

  def plusSeconds(n: Int): SimulationTime =
    SimulationTime.fromSeconds(totalSeconds + n)

object SimulationTime:
  val Zero: SimulationTime = SimulationTime(0, 0, 0, 0)

  def apply(year: Int, day: Int, hour: Int, minute: Int): SimulationTime =
    SimulationTimeImpl(year, day, hour, minute, 0)

  def apply(year: Int, day: Int, hour: Int, minute: Int, second: Int): SimulationTime =
    SimulationTimeImpl(year, day, hour, minute, second)

  def fromSeconds(totalSeconds: Long): SimulationTime =
    val s = (totalSeconds % 60).toInt
    val m = ((totalSeconds / 60) % 60).toInt
    val h = ((totalSeconds / 3600) % 24).toInt
    val d = ((totalSeconds / 86400) % 365).toInt
    val y = (totalSeconds / (86400 * 365)).toInt
    SimulationTime(y, d, h, m, s)
