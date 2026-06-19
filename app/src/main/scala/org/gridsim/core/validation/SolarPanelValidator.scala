package org.gridsim.core.validation

import cats.data.ValidatedNec
import cats.implicits.{catsSyntaxTuple2Semigroupal, catsSyntaxTuple3Semigroupal, catsSyntaxTuple4Semigroupal}
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.{SolarPanel, SolarPanelSpecification, SolarPanelState}
import org.gridsim.core.validation.Validator.{mustBeInRange, mustBePositive}

object SolarPanelValidator:
  given Validator[SolarPanel] with
    def validate(panel: SolarPanel): ValidatedNec[DomainError, SolarPanel] =
      (
        validateSpec(panel.specification),
        validateState(panel.state, panel.specification)
      ).mapN((_, _) => panel)

    private def validateState(state: SolarPanelState, specification: SolarPanelSpecification): ValidatedNec[DomainError, SolarPanelState] =
      state.outputPower.toDouble
        .mustBeInRange(
          "Last Output Power",
          0.0,
          specification.peakPower.toDouble
        ).map(_ => state)

    private def validateSpec(specification: SolarPanelSpecification): ValidatedNec[DomainError, SolarPanelSpecification] =
      (
        specification.peakPower.toDouble.mustBePositive("Peak Power"),
        specification.areaSqm.mustBePositive("Area"),
        specification.efficiency.mustBeInRange("Efficiency", 0.0, 1.0),
        specification.efficiency.mustBePositive("Efficiency")
      ).mapN((_, _, _, _) => specification)
