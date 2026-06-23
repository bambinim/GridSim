package org.gridsim.dsl.grid.entities

import cats.data.ValidatedNec
import cats.implicits._
import org.gridsim.core.common.{Energy, Power}
import org.gridsim.core.model.{GridEntity, GridEntityState}
import org.gridsim.core.model.storage.battery.{
  Battery,
  BatteryModel,
  BatteryState
}
import org.gridsim.dsl.Builder
import cats.Show
import org.gridsim.dsl.DSLError
import org.gridsim.core.validation.Validator.validate
import org.gridsim.core.validation.BatteryValidator.given
import org.gridsim.dsl.DSLBuilderError

case class BatteryBuilder(
    private[dsl] val id: Option[String],
    private[dsl] val model: BatteryModel,
    private[dsl] val maxCapacity: Option[Energy],
    private[dsl] val maxPowerCharge: Option[Power],
    private[dsl] val maxPowerDischarge: Option[Power],
    private[dsl] val minSoC: Option[Double]
) extends Builder[Battery, BatteryState]:

  override def build(): ValidatedNec[DSLError, (Battery, BatteryState)] =
    (
      maxCapacity.toValidNec(DSLBuilderError.MissingField("maxCapacity")),
      maxPowerCharge.toValidNec(DSLBuilderError.MissingField("maxChargePower")),
      maxPowerDischarge.toValidNec(
        DSLBuilderError.MissingField("maxDischargePower")
      ),
      minSoC.toValidNec(DSLBuilderError.MissingField("minSoC"))
    ).mapN((capacity, maxChargePower, maxDischargePower, minSoC) =>
      val newId = id.getOrElse(java.util.UUID.randomUUID().toString)
      (
        Battery(
          newId,
          model,
          capacity,
          maxChargePower,
          maxDischargePower,
          minSoC
        ),
        BatteryState(newId, Energy.Zero)
      )
    ).andThen(validate(_))

object BatteryBuilder:
  def battery: BatteryBuilder =
    BatteryBuilder(None, BatteryModel.Standard, None, None, None, None)

  extension (b: BatteryBuilder)
    infix def id(id: String): BatteryBuilder = b.copy(id = Some(id))
    infix def capacity(capacity: Energy): BatteryBuilder =
      b.copy(maxCapacity = Some(capacity))
    infix def maxChargingPower(maxChargePower: Power): BatteryBuilder =
      b.copy(maxPowerCharge = Some(maxChargePower))
    infix def maxDischargingPower(maxDischargePower: Power): BatteryBuilder =
      b.copy(maxPowerDischarge = Some(maxDischargePower))
    infix def minSoC(minSoC: Double): BatteryBuilder =
      b.copy(minSoC = Some(minSoC))
