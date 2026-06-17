package org.gridsim.core.model.house

import org.gridsim.core.model.{Producer, Storage}

case class HouseState[F[_]](producers: F[Producer], storages: F[Storage])
