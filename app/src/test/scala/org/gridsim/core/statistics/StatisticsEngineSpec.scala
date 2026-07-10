package org.gridsim.core.statistics

import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import java.time.LocalDateTime

@RunWith(classOf[JUnitRunner])
class StatisticsEngineSpec extends AnyFlatSpec with Matchers:

  private val tickCountFold: Fold[Int, FlowStatistic] =
    Fold.unfold[Int, Long, FlowStatistic](0L)((n, _) => n + 1)(n => FlowStatistic.empty.copy(ticks = n))

  private val sampleCountFold: Fold[Int, NetFlowHistoryStatistic] =
    Fold.unfold[Int, NetFlowHistoryStatistic, NetFlowHistoryStatistic](
      NetFlowHistoryStatistic.empty(capacity = 10)
    )((history, in) => history.record(NetFlowSample(LocalDateTime.MIN.plusSeconds(in.toLong), in.toDouble)))(identity)

  "StatisticsEngine.build" should "start every registered key at its own fold's initial extracted value" in:
    val engine = StatisticsEngine.build(List(
      Registration(StatKey.SimStats, tickCountFold),
      Registration(StatKey.NetFlowHist, sampleCountFold)
    ))
    val board = engine.extract(engine.initial)
    board.get(StatKey.SimStats).ticks shouldBe 0L
    board.get(StatKey.NetFlowHist).samples shouldBe Vector.empty

  it should "step every registered fold independently on each input" in:
    val engine = StatisticsEngine.build(List(
      Registration(StatKey.SimStats, tickCountFold),
      Registration(StatKey.NetFlowHist, sampleCountFold)
    ))
    val finalState = List(1, 2, 3).foldLeft(engine.initial)(engine.step)
    val board = engine.extract(finalState)
    board.get(StatKey.SimStats).ticks shouldBe 3L
    board.get(StatKey.NetFlowHist).samples.map(_.netFlowKwh) shouldBe Vector(1.0, 2.0, 3.0)

  it should "keep each registration's accumulator type intact — no cross-talk from the internal casts" in:
    val engine = StatisticsEngine.build(List(
      Registration(StatKey.SimStats, tickCountFold),
      Registration(StatKey.NetFlowHist, sampleCountFold)
    ))
    val board = engine.extract(List(1).foldLeft(engine.initial)(engine.step))
    board.get(StatKey.SimStats) shouldBe a[FlowStatistic]
    board.get(StatKey.NetFlowHist) shouldBe a[NetFlowHistoryStatistic]

  it should "leave an existing registration's results unaffected by adding another one" in:
    val soloEngine = StatisticsEngine.build(List(Registration(StatKey.SimStats, tickCountFold)))
    val pairEngine = StatisticsEngine.build(List(
      Registration(StatKey.SimStats, tickCountFold),
      Registration(StatKey.NetFlowHist, sampleCountFold)
    ))
    val soloBoard = soloEngine.extract(List(1, 2).foldLeft(soloEngine.initial)(soloEngine.step))
    val pairBoard = pairEngine.extract(List(1, 2).foldLeft(pairEngine.initial)(pairEngine.step))
    soloBoard.get(StatKey.SimStats) shouldBe pairBoard.get(StatKey.SimStats)

  it should "throw when a key wasn't included in the registration list" in:
    val partialEngine = StatisticsEngine.build(List(Registration(StatKey.NetFlowHist, sampleCountFold)))
    val board = partialEngine.extract(partialEngine.initial)
    an[NoSuchElementException] should be thrownBy board.get(StatKey.SimStats)

  "StatsBoard.fromMap" should "expose exactly the values stored under each key" in:
    val board = StatsBoard.fromMap(Map(
      StatKey.SimStats -> FlowStatistic.empty.copy(ticks = 7L),
      StatKey.NetFlowHist -> NetFlowHistoryStatistic.empty(5)
    ))
    board.get(StatKey.SimStats).ticks shouldBe 7L
    board.get(StatKey.NetFlowHist).samples shouldBe Vector.empty
