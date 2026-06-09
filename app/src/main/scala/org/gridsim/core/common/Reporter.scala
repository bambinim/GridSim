package org.gridsim.core.common

import cats.{Foldable, Monad, Show}
import cats.syntax.all.*

/**
 * An abstraction for handling output reporting.
 *
 * @tparam F The effect type.
 * @tparam A The type of items being reported.
 */
trait Reporter[F[_], A]:
  /**
   * Reports a single item.
   */
  def report(item: A): F[Unit]

  /**
   * Report a collection of item, folding them sequentially.
   */
  def reportAll[G[_]: Foldable](items: G[A]): F[Unit]

object Reporter:
  /**
   * Define a reporter
   *
   * @param printer A function defining how to handle the string.
   * @tparam F The effect type
   * @tparam A The type of items, wich must have a [[Show]] instance.
   * @return A [[Reporter]] instance that prints formatted items using the provided printer.
   */
  def console[F[_]: Monad, A: Show](printer: String => F[Unit]): Reporter[F, A] = new Reporter[F, A]:
    def report(item: A): F[Unit] =
      printer(item.show)

    def reportAll[G[_] : Foldable](items: G[A]): F[Unit] =
      items.traverse_(item => report(item))


