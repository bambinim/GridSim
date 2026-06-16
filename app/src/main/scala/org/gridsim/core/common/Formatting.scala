package org.gridsim.core.common

import java.util.Locale

object Formatting:
  private val locale = Locale.US

  extension (d: Double)
    def show2: String =
      String.format(locale, "%.2f", d)
