package org.gridsim.core.model

import cats.data.ValidatedNec
import cats.implicits.*
import org.gridsim.core.model.battery.Battery

enum Size(val multiplier: Double):
  case Small  extends Size(1.0)
  case Medium extends Size(1.5)
  case Large  extends Size(2.0)

enum Occupancy:
  case Traditional, SmartWorker, Vacant

sealed trait House extends GridEntity:
  def id: String
  def size: Size
  def occupancy: Occupancy

case class BaseHouse private[model] (id: String, size: Size, occupancy: Occupancy) extends House

case class HouseWithBattery (id: String, size: Size, occupancy: Occupancy, battery: Battery) extends House

object House:
  type ValidationResult[A] = ValidatedNec[String, A]

  def makeBaseHouse(id: String, size: Size, occupancy: Occupancy): ValidationResult[BaseHouse] =
    validateId(id).map(vId => BaseHouse(vId, size, occupancy))
    
  def makeHouseWithBattery(id: String, size: Size, occupancy: Occupancy, battery: Battery): ValidationResult[HouseWithBattery] =
    validateId(id).map(vId => HouseWithBattery(vId, size, occupancy, battery))

  private def validateId(str: String): ValidationResult[String] =
    if str.length >= 3 then str.validNec
    else "Error: the id must have at least 3 characters".invalidNec
