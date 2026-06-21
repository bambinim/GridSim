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

case class BatteryBuilder(
    private[dsl] val id: Option[String],
    private[dsl] val model: BatteryModel,
    private[dsl] val maxCapacity: Option[Energy],
    private[dsl] val maxPowerCharge: Option[Power],
    private[dsl] val maxPowerDischarge: Option[Power],
    private[dsl] val minSoC: Option[Double]
) extends Builder[GridEntity, GridEntityState]:
  override def build(): ValidatedNec[String, (GridEntity, GridEntityState)] =
    (
      id.orElse(Some(java.util.UUID.randomUUID().toString))
        .toValidNec("Battery id is required"),
      maxCapacity.toValidNec("Battery capacity is required"),
      maxPowerCharge.toValidNec("Battery max charge power is required"),
      maxPowerDischarge.toValidNec("Battery max discharge power is required"),
      minSoC.toValidNec("Battery min SoC is required")
    ).mapN { (id, capacity, maxChargePower, maxDischargePower, minSoC) =>
      (
        Battery(id, model, capacity, maxChargePower, maxDischargePower, minSoC),
        BatteryState(id, Energy.Zero)
      )
    }

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
