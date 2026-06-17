package org.gridsim.core.model.storage

import org.gridsim.core.common.Energy
import org.gridsim.core.model.GridEntity
import org.gridsim.core.model.storage.{Storage, StorageState}

trait Storage extends GridEntity:
  def maxCapacity: Energy
