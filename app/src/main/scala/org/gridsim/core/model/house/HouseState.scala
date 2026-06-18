package org.gridsim.core.model.house

import org.gridsim.core.model.{GridEntityState}

/**
 * Dynamic state for a house and each of its installed components.
 *
 * The order is expected to correspond to the component list on [[House]], while
 * validation also checks identity matches by component id.
 */
case class HouseState(
 entityId: String,
 componentStates: List[GridEntityState]
) extends GridEntityState
