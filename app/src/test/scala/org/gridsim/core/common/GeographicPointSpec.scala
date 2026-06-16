package org.gridsim.core.common

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GeographicPointSpec extends AnyFlatSpec with Matchers:

  // Construction
  "GeographicPoint" should "accept valid latitude and longitude" in:
    noException should be thrownBy GeographicPoint(0.0, 0.0)
    noException should be thrownBy GeographicPoint(-90.0, -180.0)
    noException should be thrownBy GeographicPoint(90.0, 180.0)
    noException should be thrownBy GeographicPoint(45.0, 90.0)

  it should "reject latitude below -90" in:
    an[IllegalArgumentException] should be thrownBy GeographicPoint(-90.01, 0.0)
    an[IllegalArgumentException] should be thrownBy GeographicPoint(-180.0, 0.0)

  it should "reject latitude above 90" in:
    an[IllegalArgumentException] should be thrownBy GeographicPoint(90.01, 0.0)
    an[IllegalArgumentException] should be thrownBy GeographicPoint(180.0, 0.0)

  it should "reject longitude below -180" in:
    an[IllegalArgumentException] should be thrownBy GeographicPoint(0.0, -180.01)
    an[IllegalArgumentException] should be thrownBy GeographicPoint(0.0, -360.0)

  it should "reject longitude above 180" in:
    an[IllegalArgumentException] should be thrownBy GeographicPoint(0.0, 180.01)
    an[IllegalArgumentException] should be thrownBy GeographicPoint(0.0, 360.0)

  // Value exposure
  it should "expose its latitude and longitude values" in:
    val point = GeographicPoint(45.0, 90.0)
    point.latitude shouldBe 45.0
    point.longitude shouldBe 90.0

  it should "preserve boundary values exactly" in:
    val sw = GeographicPoint(-90.0, -180.0)
    sw.latitude shouldBe -90.0
    sw.longitude shouldBe -180.0

    val ne = GeographicPoint(90.0, 180.0)
    ne.latitude shouldBe 90.0
    ne.longitude shouldBe 180.0

  // Equality
  it should "equal another point with the same coordinates" in:
    GeographicPoint(45.0, 90.0) shouldBe GeographicPoint(45.0, 90.0)

  it should "not equal a point with different coordinates" in:
    GeographicPoint(45.0, 90.0) should not be GeographicPoint(45.0, 91.0)
    GeographicPoint(45.0, 90.0) should not be GeographicPoint(46.0, 90.0)