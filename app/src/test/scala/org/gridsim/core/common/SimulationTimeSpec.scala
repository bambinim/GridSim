package org.gridsim.core.common

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SimulationTimeSpec extends AnyFlatSpec with Matchers:

  "SimulationTime.apply" should "create a valid time with all fields" in:
    val t = SimulationTime(1, 10, 12, 30, 45)
    t.year shouldBe 1
    t.day shouldBe 10
    t.hour shouldBe 12
    t.minute shouldBe 30
    t.second shouldBe 45

  it should "allow zero time" in:
    val t = SimulationTime.Zero
    t.year shouldBe 0
    t.day shouldBe 0
    t.hour shouldBe 0
    t.minute shouldBe 0
    t.second shouldBe 0

  it should "reject invalid day values" in:
    an [IllegalArgumentException] should be thrownBy SimulationTime(0, -1, 0, 0, 0)
    an [IllegalArgumentException] should be thrownBy SimulationTime(0, 365, 0, 0, 0)

  it should "reject invalid hour values" in:
    an [IllegalArgumentException] should be thrownBy SimulationTime(0, 0, -1, 0, 0)
    an [IllegalArgumentException] should be thrownBy SimulationTime(0, 0, 24, 0, 0)

  it should "reject invalid minute values" in:
    an [IllegalArgumentException] should be thrownBy SimulationTime(0, 0, 0, -1, 0)
    an [IllegalArgumentException] should be thrownBy SimulationTime(0, 0, 0, 60, 0)

  it should "reject invalid second values" in:
    an [IllegalArgumentException] should be thrownBy SimulationTime(0, 0, 0, 0, -1)
    an [IllegalArgumentException] should be thrownBy SimulationTime(0, 0, 0, 0, 60)

  "totalSeconds" should "be consistent with totalMinutes" in:
    val t = SimulationTime(2, 3, 4, 5, 6)
    t.totalSeconds shouldBe t.totalMinutes * 60 + 6

  "plusSeconds" should "add seconds correctly without overflow issues" in:
    val t = SimulationTime(0, 0, 0, 0, 0).plusSeconds(90)
    t.minute shouldBe 1
    t.second shouldBe 30

  it should "roll over days correctly" in:
    val t = SimulationTime(0, 0, 23, 59, 50).plusSeconds(20)
    t.day shouldBe 1
    t.hour shouldBe 0
    t.minute shouldBe 0
    t.second shouldBe 10

  "plusMinutes" should "advance time by whole minutes" in:
    val t = SimulationTime.Zero.plusMinutes(90)
    t.hour shouldBe 1
    t.minute shouldBe 30
    t.second shouldBe 0

  "fromSeconds" should "reconstruct time correctly" in:
    val original = SimulationTime(1, 10, 12, 30, 45)
    val rebuilt = SimulationTime.fromSeconds(original.totalSeconds)

    rebuilt.year shouldBe original.year
    rebuilt.day shouldBe original.day
    rebuilt.hour shouldBe original.hour
    rebuilt.minute shouldBe original.minute
    rebuilt.second shouldBe original.second

  it should "normalize overflow seconds into higher units" in:
    val t = SimulationTime.fromSeconds(90) // 1m 30s
    t.hour shouldBe 0
    t.minute shouldBe 1
    t.second shouldBe 30

  "totalMinutes" should "ignore seconds correctly" in:
    val t = SimulationTime(0, 0, 0, 1, 59)
    t.totalMinutes shouldBe 1
