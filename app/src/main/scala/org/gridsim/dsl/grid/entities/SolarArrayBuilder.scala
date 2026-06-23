package org.gridsim.dsl.grid.entities

import org.gridsim.core.common.{GeographicPoint, Power}
import org.gridsim.dsl.Builder
import org.gridsim.core.model.{
  GridEntity,
  GridEntityState,
  SolarPanelPhysics,
  SolarPanel
}
import cats.data.ValidatedNec
import cats.syntax.all.*
import cats.Show
import org.gridsim.dsl.DSLBuilderError
import org.gridsim.core.validation.SolarPanelValidator.given
import org.gridsim.core.model.SolarPanelState

case class SolarArrayBuilder private (
    private[dsl] val id: Option[String],
    private[dsl] val power: Option[Power],
    private[dsl] val location: Option[GeographicPoint],
    private[dsl] val surface: Option[Double],
    private[dsl] val efficiency: Option[Double],
    private[dsl] val physics: SolarPanelPhysics
) extends Builder[SolarPanel, SolarPanelState]:

  import org.gridsim.dsl.DSLError
  override def build(): ValidatedNec[DSLError, (SolarPanel, SolarPanelState)] =
    (
      power.toValidNec(DSLBuilderError.MissingField("power")),
      location.toValidNec(DSLBuilderError.MissingField("location")),
      surface.toValidNec(DSLBuilderError.MissingField("surface")),
      efficiency.toValidNec(DSLBuilderError.MissingField("efficiency"))
    ).mapN((p, l, s, e) =>
      (id.getOrElse(java.util.UUID.randomUUID().toString), p, l, s, e)
    ).andThen { case (i, p, l, s, e) =>
      SolarPanel(i, l, p, s, e, physics = physics)
    }

object SolarArrayBuilder:

  def solarArray: SolarArrayBuilder =
    SolarArrayBuilder(None, None, None, None, None, SolarPanelPhysics.Standard)

  extension (b: SolarArrayBuilder)
    infix def id(id: String): SolarArrayBuilder = b.copy(id = Some(id))
    infix def installedPower(p: Power): SolarArrayBuilder =
      b.copy(power = Some(p))
    infix def location(loc: (Double, Double)): SolarArrayBuilder =
      b.copy(location = Some(GeographicPoint(loc._1, loc._2)))
    infix def surface(area: Double): SolarArrayBuilder =
      b.copy(surface = Some(area))
    infix def efficiency(eff: Double): SolarArrayBuilder =
      b.copy(efficiency = Some(eff))
    infix def physics(phys: SolarPanelPhysics): SolarArrayBuilder =
      b.copy(physics = phys)
