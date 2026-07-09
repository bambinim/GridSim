package org.gridsim.core.statistics

import cats.syntax.monoid.*
import org.gridsim.core.common.Energy.*
import org.gridsim.core.common.kwh
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SimulationStatisticsSpec extends AnyFlatSpec with Matchers:

  "SimulationStatistics.empty" should "be the identity element for combine" in:
    val stats = SimulationStatistics(3L, 10.0.kwh, 4.0.kwh, 6.0.kwh, 2.0.kwh)
    (stats |+| SimulationStatistics.empty) shouldBe stats
    (SimulationStatistics.empty |+| stats) shouldBe stats

  it should "sum ticks/totals while keeping the max for peaks" in:
    val a = SimulationStatistics(1L, 2.0.kwh, 1.0.kwh, 2.0.kwh, 1.0.kwh)
    val b = SimulationStatistics(1L, 1.0.kwh, 5.0.kwh, 1.0.kwh, 5.0.kwh)

    val combined = a |+| b
    combined.ticks shouldBe 2L
    combined.totalImported.toDouble shouldBe 3.0
    combined.totalExported.toDouble shouldBe 6.0
    combined.peakImport.toDouble shouldBe 2.0
    combined.peakExport.toDouble shouldBe 5.0

  "averageNetFlow" should "be zero when no ticks have been recorded" in:
    SimulationStatistics.empty.averageNetFlow shouldBe 0.0

  it should "be the mean net flow across all ticks" in:
    val stats = SimulationStatistics(4L, 4.0.kwh, 12.0.kwh, 0.0.kwh, 0.0.kwh)
    stats.averageNetFlow shouldBe 2.0 // (12 - 4) / 4
