package org.gridsim.core.validation

import cats.data.ValidatedNec
import cats.syntax.all.*
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.{SolarPanel, SolarPanelState}
import org.gridsim.core.validation.Validator.{mustBeInRange, mustBePositive, mustBeInRangeStartExclusive, mustBeInRangeEndExclusive}

object SolarPanelValidator:

  given Validator[(SolarPanel, SolarPanelState)] with
    def validate(pair: (SolarPanel, SolarPanelState)): ValidatedNec[DomainError, (SolarPanel, SolarPanelState)] =
      val (panel, state) = pair
      (
        validatePanel(panel),
        validateState(panel, state)
      ).mapN((_, _) => pair)
       .leftMap(_.map(err => DomainError.EntityError(panel.id, err)))

    private def validatePanel(panel: SolarPanel): ValidatedNec[DomainError, SolarPanel] =
      (
        panel.maxProduction.toDouble.mustBePositive("Peak Power"),
        panel.areaSqm.mustBePositive("Area"),
        panel.efficiency.mustBeInRangeStartExclusive("Efficiency", 0.0, 1.0)
      ).mapN((_, _, _) => panel)

    private def validateState(panel: SolarPanel, state: SolarPanelState): ValidatedNec[DomainError, SolarPanelState] =
      state.efficiency
        .mustBeInRange("Efficiency", 0.0, panel.efficiency)
        .map(_ => state)
