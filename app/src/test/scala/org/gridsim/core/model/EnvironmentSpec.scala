package org.gridsim.core.model

import org.gridsim.core.common.{GeographicPoint, Irradiance, SimulationTime, toDouble}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

@RunWith(classOf[JUnitRunner])
class EnvironmentSpec extends AnyFlatSpec with Matchers:

  // Weather
  "Environment.weather" should "return zero irradiance at night" in:
    val env = Environment(SimulationTime(0, 0, 2, 0, 0))
    val w = env.weather(GeographicPoint(0, 0))

    w.irradiance shouldBe Irradiance.Zero

  it should "return non-zero irradiance during daytime" in :
    val env = Environment(SimulationTime(0, 0, 12, 0, 0))
    val w = env.weather(GeographicPoint(0, 0))

    w.irradiance.toDouble should be > 0.0

  it should "return consistent weather for same time" in :
    val t = SimulationTime(0, 10, 12, 0, 0)

    val env1 = Environment(t)
    val env2 = Environment(t)

    env1.weather(GeographicPoint(1, 1)) shouldBe env2.weather(GeographicPoint(1, 1))

  // Advance
  "Environment.advance" should "increase simulation time by delta minutes" in :
    val env = Environment(SimulationTime(0, 0, 0, 0, 0))

    val next = env.advance(FiniteDuration.apply(60, TimeUnit.MINUTES))

    next.time.hour shouldBe 1
    next.time.minute shouldBe 0

  it should "handle day rollover correctly" in :
    val env = Environment(SimulationTime(0, 0, 23, 30, 0))

    val next = env.advance(FiniteDuration.apply(60, TimeUnit.MINUTES))

    next.time.day shouldBe 1
    next.time.hour shouldBe 0

  // Immutability
  it should "return a new Environment instance on advance" in :
    val env = Environment(SimulationTime(0, 0, 0, 0, 0))
    val next = env.advance(FiniteDuration.apply(10, TimeUnit.MINUTES))

    env.time shouldBe SimulationTime(0, 0, 0, 0, 0)
    next.time should not be env.time
