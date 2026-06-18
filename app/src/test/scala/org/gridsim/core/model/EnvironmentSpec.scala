package org.gridsim.core.model

import org.gridsim.core.common.{GeographicPoint, Irradiance, toDouble}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class EnvironmentSpec extends AnyFlatSpec with Matchers:

  // Weather
  "Environment.weather" should "return zero irradiance at night" in:
    val env = Environment(2.days)
    val w = env.weather(GeographicPoint(0, 0))

    w.irradiance shouldBe Irradiance.Zero

  it should "return non-zero irradiance during daytime" in :
    val env = Environment(12.hours)
    val w = env.weather(GeographicPoint(0, 0))

    w.irradiance.toDouble should be > 0.0

  it should "normalize the hour of day after multiple days" in :
    Environment(2.days).hourOfDay shouldBe 0
    Environment(2.days + 11.hours).hourOfDay shouldBe 11

  it should "return consistent weather for same time" in :
    val t = 10.days + 12.hours

    val env1 = Environment(t)
    val env2 = Environment(t)

    env1.weather(GeographicPoint(1, 1)) shouldBe env2.weather(GeographicPoint(1, 1))

  // Advance
  "Environment.advance" should "increase simulation time by delta minutes" in :
    val env = Environment(0.seconds)

    val next = env.advance(60.minutes)

    next.time.toDays shouldBe 0
    next.time.toHours shouldBe 1
    next.time.toMinutes shouldBe 60

  it should "handle day rollover correctly" in :
    val env = Environment(23.hours + 30.minutes)

    val next = env.advance(60.minutes)

    next.time.toDays shouldBe 1
    next.time.toHours shouldBe 24
    next.time.toMinutes shouldBe 24 * 60 + 30

  // Immutability
  it should "return a new Environment instance on advance" in :
    val env = Environment(0.seconds)
    val next = env.advance(10.minutes)

    env.time shouldBe FiniteDuration(0, TimeUnit.SECONDS)
    next.time should not be env.time
