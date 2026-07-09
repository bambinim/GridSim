package org.gridsim.core.statistics

import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner
import scala.concurrent.duration.DurationInt

@RunWith(classOf[JUnitRunner])
class NetFlowHistorySpecs extends AnyFlatSpec with Matchers:

  private def sampleAt(tickSeconds: Int, value: Double): NetFlowSample =
    NetFlowSample(tickSeconds.seconds, value)

  "NetFlowHistory" should "retain samples in order up to capacity" in :
    val h = (1 to 3).foldLeft(NetFlowHistory.empty(5))((h, i) => h.record(sampleAt(i, i.toDouble)))
    h.samples.map(_.netFlowKwh) shouldBe Vector(1.0, 2.0, 3.0)

  it should "drop the oldest sample once capacity is exceeded" in :
    val h = (1 to 7).foldLeft(NetFlowHistory.empty(5))((h, i) => h.record(sampleAt(i, i.toDouble)))
    h.samples.map(_.netFlowKwh) shouldBe Vector(3.0, 4.0, 5.0, 6.0, 7.0)

  it should "preserve the real simulation tick, not buffer position" in :
    val h = (1 to 7).foldLeft(NetFlowHistory.empty(5))((h, i) => h.record(sampleAt(i, i.toDouble)))
    h.samples.map(_.tick) shouldBe Vector(3.seconds, 4.seconds, 5.seconds, 6.seconds, 7.seconds)
