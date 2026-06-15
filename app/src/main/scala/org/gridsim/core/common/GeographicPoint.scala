package org.gridsim.core.common

trait GeographicPoint:
  def latitude: Double
  def longitude: Double

private case class GeographicPointImpl(latitude: Double, longitude: Double) extends GeographicPoint

object GeographicPoint:
  def apply(latitude: Double, longitude: Double): GeographicPoint =
    require(latitude >= -90.0 && latitude <= 90.0, s"Invalid latitude: $latitude")
    require(longitude >= -180.0 && longitude <= 180.0, s"Invalid longitude: $longitude")
    GeographicPointImpl(latitude, longitude)
