package org.gridsim.common

import scala.annotation.targetName

object Units:

  opaque type Energy = Double
  opaque type Power = Double

  object Energy:
    def apply(v: Double): Energy = v

  object Power:
    def apply(v: Double): Power = v

  extension (e: Energy)
    @targetName("energyValue")
    def value: Double = e

  extension (p: Power)
    @targetName("powerValue")
    def value: Double = p
