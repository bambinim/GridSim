package org.gridsim.core.common

import com.google.common.primitives.UnsignedLong

object Ticks:
  opaque type Tick = UnsignedLong

  object Tick:
    def start: Tick = UnsignedLong.ZERO

  extension (tick: Tick)
    def next: Tick = tick.plus(UnsignedLong.ONE)
