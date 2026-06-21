package org.gridsim.core.validation

import cats.data.ValidatedNec
import cats.implicits.{catsSyntaxTuple2Semigroupal, catsSyntaxTuple3Semigroupal}
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.{SolarPanel, SolarPanelState, SolarPanelWithState}
import org.gridsim.core.validation.Validator.{mustBeInRange, mustBePositive, mustBeInRangeStartExclusive}

object SolarPanelValidator:

  given Validator[SolarPanelWithState] with
    def validate(panelWithState: SolarPanelWithState): ValidatedNec[DomainError, SolarPanelWithState] =
      (
        validatePanel(panelWithState.panel),
        validateState(panelWithState.panel, panelWithState.state)
      ).mapN((_, _) => panelWithState)

    private def validatePanel(panel: SolarPanel): ValidatedNec[DomainError, SolarPanel] =
      (
        panel.maxProduction.toDouble.mustBePositive("Peak Power"),
        panel.areaSqm.mustBePositive("Area"),
        panel.efficiency.mustBeInRangeStartExclusive("Efficiency", 0.0, 1.0)
      ).mapN((_, _, _) => panel)

    private def validateState(panel: SolarPanel, state: SolarPanelState): ValidatedNec[DomainError, SolarPanelState] =
      state.currentProduction.toDouble
        .mustBeInRange("Last Output Power", 0.0, panel.maxProduction.toDouble)
        .map(_ => state)
