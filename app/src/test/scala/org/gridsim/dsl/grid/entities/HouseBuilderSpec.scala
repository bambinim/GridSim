package org.gridsim.dsl.grid.entities

import org.gridsim.dsl.grid.entities.HouseBuilder.*
import org.gridsim.dsl.grid.entities.SolarArrayBuilder.solarArray
import org.gridsim.dsl.grid.entities.BatteryBuilder.battery
import org.gridsim.core.common.{kw, kwh}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner
import org.gridsim.core.model.house.House
import org.gridsim.core.model.house.HouseState
import cats.syntax.contravariantSemigroupal

@RunWith(classOf[JUnitRunner])
class HouseBuilderSpec extends AnyFlatSpec with Matchers:

  case class Static(mean: Double, variance: Double)
      extends org.gridsim.core.behaviour.house.ConsumptionStrategy:
    override def getBand(h: Long) = org.gridsim.core.behaviour.house
      .ConsumptionBand(org.gridsim.core.common.Power(mean), variance)

  val exampleBatteryBuilder =
    battery id "b1" capacity 100.kwh maxChargingPower 100.kw maxDischargingPower 100.kw minSoC 0.15

  val exampleSolarArrayBuilder =
    solarArray id "sa1" installedPower 5.kw efficiency 0.97 location (
      1.0,
      1.0
    ) surface 100.0

  "A HouseBuilder" should "be empty by default" in:
    val h = house {}

    h.id shouldBe None
    h.consumptionStrategy shouldBe None
    h.otherEntities shouldBe List.empty
    h.storages shouldBe List.empty

  it should "set only id when id is set" in:
    val h = house:
      id("h1")

    h.id shouldBe Some("h1")
    h.consumptionStrategy shouldBe None
    h.otherEntities shouldBe List.empty
    h.storages shouldBe List.empty

  it should "set only consumption strategy when consumption strategy is set" in:
    val h = house:
      consumptionStrategy(Static(230.0, 3200.0))

    h.id shouldBe None
    h.consumptionStrategy shouldBe Some(Static(230.0, 3200.0))
    h.otherEntities shouldBe List.empty
    h.storages shouldBe List.empty

  it should "set only other entities when calling contains" in:
    val h = house:
      contains(exampleSolarArrayBuilder)

    h.id shouldBe None
    h.consumptionStrategy shouldBe None
    h.otherEntities shouldBe List(exampleSolarArrayBuilder)
    h.storages shouldBe List.empty

  it should "set only storages when storages is set" in:
    val h = house:
      energyStorageSystems(exampleBatteryBuilder)

    h.id shouldBe None
    h.consumptionStrategy shouldBe None
    h.otherEntities shouldBe List.empty
    h.storages shouldBe List(exampleBatteryBuilder)

  it should "be valid when no storages or other entities are present" in:
    val b = house:
      id("hs1")
      consumptionStrategy(Static(230.0, 3200.0))
    val v = b.build()
    v.isValid shouldBe true
    val h = v.getOrElse(null)
    h._1 shouldBe House("hs1", List.empty, Static(230.0, 3200.0))
    h._2 shouldBe HouseState("hs1", List.empty)

  it should "build a valid house with additional components when specified" in:
    val b = house:
      id("hs1")
      consumptionStrategy(Static(230.0, 3200.0))
      contains(exampleSolarArrayBuilder)
    val v = b.build()
    v.isValid shouldBe true
    val h = v.getOrElse(null)
    h._1 shouldBe House(
      "hs1",
      List(exampleSolarArrayBuilder.build().getOrElse(null)._1),
      Static(230.0, 3200.0)
    )
    h._2 shouldBe HouseState(
      "hs1",
      List(exampleSolarArrayBuilder.build().getOrElse(null)._2)
    )

  it should "build a valid house with an energy storage system" in:
    val b = house:
      id("hs1")
      consumptionStrategy(Static(230.0, 3200.0))
      energyStorageSystems(exampleBatteryBuilder)
    val v = b.build()
    v.isValid shouldBe true
    val h = v.getOrElse(null)
    h._1 shouldBe House(
      "hs1",
      List(exampleBatteryBuilder.build().getOrElse(null)._1),
      Static(230.0, 3200.0)
    )
    h._2 shouldBe HouseState(
      "hs1",
      List(exampleBatteryBuilder.build().getOrElse(null)._2)
    )
