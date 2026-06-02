package org.gridsim.model

import org.gridsim.model.GridEntity

enum Size:
  case Small, Medium, Large

enum Occupancy:
  case Traditional, SmartWorker, Vacant

sealed trait House extends GridEntity:
  def id: String
  def size: Size
  def occupancy: Occupancy

case class BaseHouse(id: String, size: Size, occupancy: Occupancy) extends House



