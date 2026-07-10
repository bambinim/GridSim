package org.gridsim.core.statistics

import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import java.time.LocalDateTime

@RunWith(classOf[JUnitRunner])
class NetFlowHistoryStatisticSpec extends AnyFlatSpec with Matchers:

  private val start = LocalDateTime.of(2026, 1, 1, 12, 0)

  private def sampleAt(second: Int, value: Double): NetFlowSample =
    NetFlowSample(start.plusSeconds(second), value)

  "NetFlowHistory" should "retain samples in order up to capacity" in :
    val h = (1 to 3).foldLeft(NetFlowHistoryStatistic.empty(5))((h, i) => h.record(sampleAt(i, i.toDouble)))
    h.samples.map(_.netFlowKwh) shouldBe Vector(1.0, 2.0, 3.0)

  it should "drop the oldest sample once capacity is exceeded" in :
    val h = (1 to 7).foldLeft(NetFlowHistoryStatistic.empty(5))((h, i) => h.record(sampleAt(i, i.toDouble)))
    h.samples.map(_.netFlowKwh) shouldBe Vector(3.0, 4.0, 5.0, 6.0, 7.0)

  it should "preserve the real simulation tick, not buffer position" in :
    val h = (1 to 7).foldLeft(NetFlowHistoryStatistic.empty(5))((h, i) => h.record(sampleAt(i, i.toDouble)))
    h.samples.map(_.dateTime) shouldBe Vector(start.plusSeconds(3),
      start.plusSeconds(4), start.plusSeconds(5), start.plusSeconds(6), start.plusSeconds(7))
