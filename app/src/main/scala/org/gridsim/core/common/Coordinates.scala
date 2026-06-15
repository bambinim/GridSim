package org.gridsim.core.common

object Coordinates:

  case class GeographicPoint(latitude: Double, longitude: Double)

  trait Coordinate[T]:
    def latitude(point: T): Double
    def longitude(point: T): Double

    extension (t: T)
      def distanceTo(other: T)(using Coordinate[T]): Double =
        val dLat = math.toRadians(latitude(other) - latitude(t))
        val dLon = math.toRadians(longitude(other) - longitude(t))
        val a = math.sin(dLat / 2) * math.sin(dLat / 2) +
          math.cos(math.toRadians(latitude(t))) *
            math.cos(math.toRadians(latitude(other))) *
            math.sin(dLon / 2) * math.sin(dLon / 2)
        6371 * 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a)) // km

  given Coordinate[GeographicPoint] with
    def latitude(point: GeographicPoint): Double = point.latitude
    def longitude(point: GeographicPoint): Double = point.longitude
