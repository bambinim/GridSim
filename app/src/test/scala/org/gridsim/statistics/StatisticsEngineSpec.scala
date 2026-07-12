package org.gridsim.statistics

import org.gridsim.statistics.{FlowStatistic, Fold, NetFlowHistoryStatistic, NetFlowSample, Registration, StatKey, StatisticsEngine, StatsBoard}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import java.time.LocalDateTime

@RunWith(classOf[JUnitRunner])
class StatisticsEngineSpec extends AnyFlatSpec with Matchers:

  private val tickCountFold: Fold[Int, FlowStatistic] =
    Fold.unfold[Int, Long, FlowStatistic](0L)((n, _) => n + 1)(n => FlowStatistic.empty.copy(samples = n))

  private val sampleCountFold: Fold[Int, NetFlowHistoryStatistic] =
    Fold.unfold[Int, NetFlowHistoryStatistic, NetFlowHistoryStatistic](
      NetFlowHistoryStatistic.empty(capacity = 10)
    )((history, in) => history.record(NetFlowSample(LocalDateTime.MIN.plusSeconds(in.toLong), in.toDouble)))(identity)

  "StatisticsEngine.build" should "start every registered key at its own fold's initial extracted value" in:
    val engine = StatisticsEngine.build(List(
      Registration(StatKey.FlowStatKey, tickCountFold),
      Registration(StatKey.NetFlowHistoryStatKey, sampleCountFold)
    ))
    val board = engine.extract(engine.initial)
    board.get(StatKey.FlowStatKey).samples shouldBe 0L
    board.get(StatKey.NetFlowHistoryStatKey).samples shouldBe Vector.empty

  it should "step every registered fold independently on each input" in:
    val engine = StatisticsEngine.build(List(
      Registration(StatKey.FlowStatKey, tickCountFold),
      Registration(StatKey.NetFlowHistoryStatKey, sampleCountFold)
    ))
    val finalState = List(1, 2, 3).foldLeft(engine.initial)(engine.step)
    val board = engine.extract(finalState)
    board.get(StatKey.FlowStatKey).samples shouldBe 3L
    board.get(StatKey.NetFlowHistoryStatKey).samples.map(_.netFlowKwh) shouldBe Vector(1.0, 2.0, 3.0)

  it should "keep each registration's accumulator type intact — no cross-talk from the internal casts" in:
    val engine = StatisticsEngine.build(List(
      Registration(StatKey.FlowStatKey, tickCountFold),
      Registration(StatKey.NetFlowHistoryStatKey, sampleCountFold)
    ))
    val board = engine.extract(List(1).foldLeft(engine.initial)(engine.step))
    board.get(StatKey.FlowStatKey) shouldBe a[FlowStatistic]
    board.get(StatKey.NetFlowHistoryStatKey) shouldBe a[NetFlowHistoryStatistic]

  it should "leave an existing registration's results unaffected by adding another one" in:
    val soloEngine = StatisticsEngine.build(List(Registration(StatKey.FlowStatKey, tickCountFold)))
    val pairEngine = StatisticsEngine.build(List(
      Registration(StatKey.FlowStatKey, tickCountFold),
      Registration(StatKey.NetFlowHistoryStatKey, sampleCountFold)
    ))
    val soloBoard = soloEngine.extract(List(1, 2).foldLeft(soloEngine.initial)(soloEngine.step))
    val pairBoard = pairEngine.extract(List(1, 2).foldLeft(pairEngine.initial)(pairEngine.step))
    soloBoard.get(StatKey.FlowStatKey) shouldBe pairBoard.get(StatKey.FlowStatKey)

  it should "throw when a key wasn't included in the registration list" in:
    val partialEngine = StatisticsEngine.build(List(Registration(StatKey.NetFlowHistoryStatKey, sampleCountFold)))
    val board = partialEngine.extract(partialEngine.initial)
    an[NoSuchElementException] should be thrownBy board.get(StatKey.FlowStatKey)

  "StatsBoard.fromMap" should "expose exactly the values stored under each key" in:
    val board = StatsBoard.fromMap(Map(
      StatKey.FlowStatKey -> FlowStatistic.empty.copy(samples = 7L),
      StatKey.NetFlowHistoryStatKey -> NetFlowHistoryStatistic.empty(5)
    ))
    board.get(StatKey.FlowStatKey).samples shouldBe 7L
    board.get(StatKey.NetFlowHistoryStatKey).samples shouldBe Vector.empty
