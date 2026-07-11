package org.gridsim.util

import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Global formatting utilities used throughout the simulation.
 *
 * Provides extension methods and reusable formatters for rendering values
 * in a consistent, locale-independent format.
 *
 * Numeric formatting uses [[Locale.US]] to ensure stable output regardless
 * of the system locale.
 */
object Formatting:
  private val locale = Locale.US

  /** Patterns used for dates. */
  val DatePattern = "yyyy-MM-dd"
  val DateTimePattern = "yyyy-MM-dd HH:mm:ss"

  val DateFormatting: DateTimeFormatter =
    DateTimeFormatter.ofPattern(DatePattern)
  val DateTimeFormatting: DateTimeFormatter =
    DateTimeFormatter.ofPattern(DateTimePattern)

  extension (d: Double)
    /** Formats the number with exactly two decimal places. */
    def show2: String =
      String.format(locale, "%.2f", d)
