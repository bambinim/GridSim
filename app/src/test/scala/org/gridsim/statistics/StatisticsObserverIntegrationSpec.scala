package org.gridsim.statistics

import cats.effect.{IO, Ref}
import cats.effect.unsafe.implicits.global
import cats.syntax.monoid.*
import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.common.Energy.*
import org.gridsim.core.common.kwh
import org.gridsim.core.model.Environment
import org.gridsim.core.observability.SimulationData.EntityFlowsData
import org.gridsim.core.observability.{Fs2DataDispatcher, Observer, SimulationData}
import org.gridsim.core.simulation.SimulationState
import org.gridsim.statistics.{FlowSampler, FlowStatistic}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class StatisticsObserverIntegrationSpec extends AnyFlatSpec with Matchers:

  private def stateWithFlow(flow: Flow[Energy]): SimulationState =
    SimulationState(
      environment = Environment(1.minute),
      entityStates = Map.empty,
      entityFlows = Map("house" -> flow),
      cableLoads = Map.empty
    )

  "A statistics-accumulating observer" should "fold every dispatched tick into a running total" in:
    val testIO = for {
      statsRef <- Ref.of[IO, FlowStatistic](FlowStatistic.empty)
      obs = Observer[IO, SimulationData.SimulationSnapshot] { snapshot =>
        statsRef.update(_ |+| FlowSampler.sample(EntityFlowsData(snapshot.entityFlows)))
      }
      dispatcher <- Fs2DataDispatcher[IO](List(obs))
      _ <- IO.sleep(500.millis) // let the subscriber fiber start, same as ObservabilitySpec

      _ <- dispatcher.dispatch(stateWithFlow(Flow.Surplus(5.0.kwh)), 15.minutes)
      _ <- dispatcher.dispatch(stateWithFlow(Flow.Deficit(2.0.kwh)), 15.minutes)
      _ <- dispatcher.dispatch(stateWithFlow(Flow.Surplus(9.0.kwh)), 15.minutes)
      _ <- IO.sleep(200.millis) // let the last update settle

      result <- statsRef.get
    } yield result

    val stats = testIO.unsafeRunSync()
    stats.samples shouldBe 3L
    stats.totalExported.toDouble shouldBe 14.0
    stats.totalImported.toDouble shouldBe 2.0
    stats.peakExport.toDouble shouldBe 9.0
    stats.peakImport.toDouble shouldBe 2.0
