package org.gridsim.core.model

import org.gridsim.core.common.{GeographicPoint, Irradiance, toDouble}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class EnvironmentSpec extends AnyFlatSpec with Matchers:

  // startDateTime / currentDateTime

  "Environment" should "expose the startDateTime it was created with" in:
    val start = LocalDateTime.of(2025, 6, 21, 8, 0)
    val env = Environment(start)
    env.startDateTime shouldBe start

  it should "compute currentDateTime as startDateTime + time" in:
    val start = LocalDateTime.of(2025, 1, 15, 10, 0)
    val env = Environment(start, 3.hours)
    env.currentDateTime shouldBe LocalDateTime.of(2025, 1, 15, 13, 0)

  it should "preserve startDateTime after advance" in:
    val start = LocalDateTime.of(2025, 3, 20, 6, 0)
    val env = Environment(start, 0.seconds)
    val next = env.advance(30.minutes)
    next.startDateTime shouldBe start
    next.currentDateTime shouldBe LocalDateTime.of(2025, 3, 20, 6, 30)

  // Weather

  "Environment.weather" should "return zero irradiance at midnight" in:
    val env = Environment(LocalDateTime.of(2025, 6, 21, 0, 0))
    val w = env.weather(GeographicPoint(45.0, 11.0))
    w.irradiance shouldBe Irradiance.Zero

  it should "return non-zero irradiance at solar noon in summer" in:
    val env = Environment(LocalDateTime.of(2025, 6, 21, 12, 0))
    val w = env.weather(GeographicPoint(45.0, 11.0))
    w.irradiance.toDouble should be > 0.0

  it should "return consistent weather for the same inputs" in:
    val start = LocalDateTime.of(2025, 6, 21, 14, 0)
    val point = GeographicPoint(44.0, 11.0)
    val env1 = Environment(start)
    val env2 = Environment(start)
    env1.weather(point) shouldBe env2.weather(point)

  // hourOfDay

  "hourOfDay" should "normalize the hour of day after multiple days" in:
    Environment(2.days).hourOfDay shouldBe 0
    Environment(2.days + 11.hours).hourOfDay shouldBe 11

  // Advance

  "Environment.advance" should "increase simulation time by delta" in:
    val env = Environment(0.seconds)
    val next = env.advance(60.minutes)
    next.time.toHours shouldBe 1
    next.time.toMinutes shouldBe 60

  it should "handle day rollover correctly" in:
    val env = Environment(23.hours + 30.minutes)
    val next = env.advance(60.minutes)
    next.time.toDays shouldBe 1
    next.time.toHours shouldBe 24
    next.time.toMinutes shouldBe 24 * 60 + 30

  // Immutability

  "Environment" should "return a new instance on advance" in:
    val env = Environment(0.seconds)
    val next = env.advance(10.minutes)
    env.time shouldBe FiniteDuration(0, TimeUnit.SECONDS)
    next.time should not be env.time
