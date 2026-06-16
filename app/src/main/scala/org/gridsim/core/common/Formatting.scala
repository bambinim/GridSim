package org.gridsim.core.common

import java.util.Locale

/**
 * Global formatting utilities used throughout the simulation.
 *
 * Provides extension methods for rendering values in a consistent,
 * locale-independent format. All formatting uses [[locale.US]]
 * to ensure stable output regardless of the system locale.
 */
object Formatting:
  private val locale = Locale.US

  extension (d: Double)
    /** Formats the number with exactly two decimal places. */
    def show2: String =
      String.format(locale, "%.2f", d)
