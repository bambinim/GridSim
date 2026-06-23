package org.gridsim.dsl.grid.entities

import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner
import org.gridsim.dsl.grid.entities.SolarArrayBuilder.*
import org.gridsim.core.common.kw
import org.gridsim.core.common.GeographicPoint
import org.gridsim.core.model.SolarPanelPhysics

import scala.language.postfixOps
import org.gridsim.core.model.SolarPanel

@RunWith(classOf[JUnitRunner])
class SolarArrayBuilderSpec extends AnyFlatSpec with Matchers:
  "A SolarArrayBuilder" should "be empty on creation" in:
    val array = solarArray
    array.id shouldBe None
    array.power shouldBe None
    array.location shouldBe None
    array.surface shouldBe None
    array.efficiency shouldBe None
    array.physics shouldBe SolarPanelPhysics.Standard

  it should "change only id when id function is called" in:
    val array = solarArray id "solarArray1"
    array.id shouldBe Some("solarArray1")
    array.power shouldBe None
    array.location shouldBe None
    array.surface shouldBe None
    array.efficiency shouldBe None
    array.physics shouldBe SolarPanelPhysics.Standard

  it should "change only power when installedPower function is called" in:
    val array = solarArray installedPower 10.kw
    array.id shouldBe None
    array.power shouldBe Some(10.kw)
    array.location shouldBe None
    array.surface shouldBe None
    array.efficiency shouldBe None
    array.physics shouldBe SolarPanelPhysics.Standard

  it should "change only location when location function is called" in:
    val array = solarArray location (0.0, 0.0)
    array.id shouldBe None
    array.power shouldBe None
    array.location shouldBe Some(GeographicPoint(0.0, 0.0))
    array.surface shouldBe None
    array.efficiency shouldBe None
    array.physics shouldBe SolarPanelPhysics.Standard

  it should "change only surface when surface function is called" in:
    val array = solarArray surface 10.0
    array.id shouldBe None
    array.power shouldBe None
    array.location shouldBe None
    array.surface shouldBe Some(10.0)
    array.efficiency shouldBe None
    array.physics shouldBe SolarPanelPhysics.Standard

  it should "change only efficiency when efficiency function is called" in:
    val array = solarArray efficiency 0.5
    array.id shouldBe None
    array.power shouldBe None
    array.location shouldBe None
    array.surface shouldBe None
    array.efficiency shouldBe Some(0.5)
    array.physics shouldBe SolarPanelPhysics.Standard

  it should "build a valid solar array with its state" in:
    val b =
      solarArray id "s1" installedPower 10.kw location (
        0.0,
        0.0
      ) surface 10.0 efficiency 0.97
    b.build().isValid shouldBe true

  it should "use a random UUID when id is not set" in:
    val b =
      solarArray installedPower 10.kw location (
        0.0,
        0.0
      ) surface 10.0 efficiency 0.97
    b.build().isValid shouldBe true

  it should "fail when power is not set" in:
    val b =
      solarArray id "s1" location (0.0, 0.0) surface 10.0 efficiency 0.97
    b.build().isValid shouldBe false

  it should "fail when location is not set" in:
    val b =
      solarArray id "s1" installedPower 10.kw surface 10.0 efficiency 0.97
    b.build().isValid shouldBe false

  it should "fail when surface is not set" in:
    val b =
      solarArray id "s1" installedPower 10.kw location (
        0.0,
        0.0
      ) efficiency 0.97
    b.build().isValid shouldBe false

  it should "fail when efficiency is not set" in:
    val b =
      solarArray id "s1" installedPower 10.kw location (0.0, 0.0) surface 10.0
    b.build().isValid shouldBe false
